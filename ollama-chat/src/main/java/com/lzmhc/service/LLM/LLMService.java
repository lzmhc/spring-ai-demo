package com.lzmhc.service.LLM;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;

public interface LLMService {
    /**
     * 获取对话模型
     * @return
     */
    ChatModel getChatModel();
    /**
     * 获取RAG对话模型
     */
    EmbeddingModel getEmbeddingModel();
    /**
     * 获取多模态模型
     */
    ChatModel getMultimodalModel();
    /**
     * 超长上下文对话模型
     */
    ChatModel getLongContextChatModel();
    /**
     * 获取qwen大模型
     */
    ChatModel getQwenModel();
}
