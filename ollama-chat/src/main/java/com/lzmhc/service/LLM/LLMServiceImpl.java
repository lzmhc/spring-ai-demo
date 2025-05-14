package com.lzmhc.service.LLM;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.ollama.management.ModelManagementOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LLMServiceImpl implements LLMService{
    @Value("${spring.ai.ollama.base-url}")
    private String baseUrl;
    @Value("${chat.simple.model}")
    private String simpleModel;
    @Value("${chat.embedding.model}")
    private String embbedModel;
    @Value("${chat.multimodal.model}")
    private String multimodalModel;
    @Value("${chat.longContext.model}")
    private String longContextModel;
    @Value("${chat.qwen.model}")
    private String qwenModel;
    @Override
    public ChatModel getChatModel() {
        return OllamaChatModel.builder()
                .ollamaApi(new OllamaApi(baseUrl))
                .defaultOptions(OllamaOptions.builder().model(simpleModel).build())
                .build();
    }

    @Override
    public EmbeddingModel getEmbeddingModel() {
        return OllamaEmbeddingModel.builder()
                .ollamaApi(new OllamaApi(baseUrl))
                .defaultOptions(OllamaOptions.builder().model(embbedModel).build())
                .build();
    }

    @Override
    public ChatModel getMultimodalModel() {
        return OllamaChatModel.builder()
                .ollamaApi(new OllamaApi(baseUrl))
                .defaultOptions(OllamaOptions.builder().model(multimodalModel).build())
                .build();
    }

    @Override
    public ChatModel getLongContextChatModel() {
        return OllamaChatModel.builder()
                .ollamaApi(new OllamaApi(baseUrl))
                .defaultOptions(OllamaOptions.builder().model(longContextModel).build())
                .build();
    }

    @Override
    public ChatModel getQwenModel() {
        return OllamaChatModel.builder()
                .ollamaApi(new OllamaApi(baseUrl))
                .defaultOptions(OllamaOptions.builder().model(qwenModel).build())
                .build();
    }
}
