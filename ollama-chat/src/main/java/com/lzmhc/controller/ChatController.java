package com.lzmhc.controller;

import com.lzmhc.dto.ChatRequest;
import com.lzmhc.mapper.mysql.entity.Document;
import com.lzmhc.service.AiChatService;
import com.lzmhc.service.DocumentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;


@RestController
@Slf4j
//@CrossOrigin(origins = "https://lzmhc.top:18080")
public class ChatController {
    @Autowired
    private AiChatService aiChatService;
    @Autowired
    private DocumentService documentService;
    /**
     * 对话
     * @param chatRequest
     * @return
     * @throws InterruptedException
     */
    @PostMapping(value = "/chat", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamResp(@RequestBody ChatRequest chatRequest) throws InterruptedException {
        System.out.println(chatRequest);
        if(chatRequest.isFlag()){
            // RAG对话
            Flux<ChatResponse> chatResp = aiChatService.simpleRagChat(chatRequest);
            return chatResp.map(chatObj -> chatObj.getResult().getOutput().getText())
                    .onErrorResume(e -> Flux.just("大模型服务不可用"));
        }else{
            // 通用对话
            if("gpt-4o-mini:latest".equals(chatRequest.getModel())){
                //多模态对话
                Flux<ChatResponse> chatResp = aiChatService.multimodalChat(chatRequest);
                return chatResp.map(chatObj -> chatObj.getResult().getOutput().getText())
                        .onErrorResume(e -> Flux.just("大模型服务不可用"));
            }else{
                // 通用对话
                Flux<ChatResponse> chatResp = aiChatService.simpleChat(chatRequest);
                return chatResp.map(chatObj -> chatObj.getResult().getOutput().getText())
                        .onErrorResume(e -> Flux.just("大模型服务不可用"));
            }
        }
    }
    /**
     * 获取知识库列表
     */
    @GetMapping("/getKnowledgeList")
    public Flux<Document> getKnowledgeList(){
        return documentService.getKnowledgeList();
    }
    /**
     * ai智能建议
     */
    @GetMapping("/suggestion")
    public Flux<String> suggestion(){
        Flux<ChatResponse> chatResp = aiChatService.suggestion();
        return chatResp.map(chatObj -> chatObj.getResult().getOutput().getText())
                .onErrorResume(e -> Flux.just("大模型服务不可用"));
    }
}
