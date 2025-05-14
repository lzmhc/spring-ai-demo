package com.lzmhc.service;

import cn.dev33.satoken.stp.StpUtil;
import com.lzmhc.dto.ChatRequest;
import com.lzmhc.mapper.mysql.entity.Document;
import com.lzmhc.service.LLM.LLMService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.mcp.AsyncMcpToolCallbackProvider;
import org.springframework.ai.model.Media;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AiChatService {
    @Autowired
    private VectorStore vectorStore;
    @Autowired
    private LLMService llmService;
    @Autowired
    private MinioService minioService;
    @Autowired
    private DocumentService documentService;
    @Value("classpath:prompt/RAG.txt")
    private Resource ragPromptResource;

    private final ToolCallbackProvider asyncMcpToolCallbackProvider;

    public AiChatService(ToolCallbackProvider asyncMcpToolCallbackProvider1){
        this.asyncMcpToolCallbackProvider = asyncMcpToolCallbackProvider1;
    }
    /**
     * 简单对话
     * @param chatRequest
     * @return
     */
    public Flux<ChatResponse> simpleChat(ChatRequest chatRequest){
        ChatModel chatModel = llmService.getChatModel();
        ChatClient chatClient = ChatClient.builder(chatModel).defaultTools(asyncMcpToolCallbackProvider.getToolCallbacks()).build();
        return chatClient.prompt().user(user -> {
            user.text(chatRequest.getMessages()[0].getContent());
        }).stream().chatResponse();
    }
    /**
     * RAG对话
     */
    public Flux<ChatResponse> simpleRagChat(ChatRequest chatRequest){
        ChatModel chatModel = llmService.getChatModel();
        ChatClient chatClient = ChatClient.builder(chatModel).build();
        PromptTemplate template = new PromptTemplate(ragPromptResource);
        String ragTemplate = template.getTemplate();
        SearchRequest searchRequest = SearchRequest.builder()
                .topK(5)
                .query(chatRequest.getMessages()[0].getContent())
                .filterExpression(buildBaseAccessFilter(chatRequest.getDocumentId()))
                .build();
        return chatClient.prompt().user(user -> {
            user.text(chatRequest.getMessages()[0].getContent());
        }).advisors(new SimpleLoggerAdvisor(),
                QuestionAnswerAdvisor.builder(vectorStore)
                        .userTextAdvise(ragTemplate)
                        .searchRequest(searchRequest)
                        .build())
                .stream()
                .chatResponse();
    }

    /**
     * 多模态对话
     * @return
     */
    public Flux<ChatResponse> multimodalChat(ChatRequest chatRequest){
        ChatModel chatModel = llmService.getMultimodalModel();
        ChatClient chatClient = ChatClient.builder(chatModel).build();
        String objectName = chatRequest.getResourceId();
        return chatClient.prompt().user(user -> {
            user.text(chatRequest.getMessages()[0].getContent());
            if(objectName != null && !objectName.isEmpty()){
                Media mediaFromMinio = null;
                try {
                    mediaFromMinio = minioService.getMediaFromMinio(objectName);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                user.media(mediaFromMinio);
            }
        }).stream().chatResponse();
    }

    /**
     *ai建议
     * @return
     */
    public Flux<ChatResponse> suggestion(){
        String promptBase = "作为一个基于SpringAI大模型的大学生智能对话学习助手." +
                "你需要通过用户上传的知识库分析用户的学习情况，通过用户所处年级，年龄，专业情况分析出当前学生的学习情况" +
                " 专业：软件工程 "+
                " 主学课程：";
        List<Document> documents = documentService.getKnowledges();
        String result = documents.stream()
                .map(Document::getFileName)
                .collect(Collectors.joining(","));
        String prompt = promptBase+result+"。不要客观分析，以第一人称回答，不要使用markdown格式回答 不能超过500字。";
        ChatModel chatModel = llmService.getQwenModel();
        ChatClient chatClient = ChatClient.builder(chatModel).build();
        return chatClient.prompt().user(user -> {
            user.text(prompt);
        }).stream().chatResponse();
    }

    // meta ==>
    private String buildBaseAccessFilter(List<String> documentId) {
        String openId = StpUtil.getLoginIdAsString();

        // 如果没有 ID，返回一个 false 的表达式
        if (documentId == null || documentId.isEmpty()) {
            return "document_id in [\"___empty___\"]"; // 不让查询任何知识库
        }
        StringBuilder sb = new StringBuilder();
        sb.append("document_id in [");
        for (int i = 0; i < documentId.size(); i++) {
            if (i != 0) {
                sb.append(",");
            }
            sb.append(documentId.get(i));
        }
        sb.append("]");
        log.info("Vector Search Filter SQL: {}", sb);
        log.info("Vector Search Filter Parameter: {}", documentId);
        return sb.toString();
    }
}
