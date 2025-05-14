package com.lzmhc.ollamamcpclient.controller;

import com.lzmhc.ollamamcpclient.dto.ChatRequest;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.mcp.AsyncMcpToolCallbackProvider;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class WeatherController {
//    @Autowired
//    private ChatClient chatClient;
    private final ChatClient chatClient;
    public WeatherController(ChatClient.Builder chatClientBuilder, AsyncMcpToolCallbackProvider tools) {
        this.chatClient = chatClientBuilder
                .defaultTools(tools.getToolCallbacks())
                .defaultAdvisors(new PromptChatMemoryAdvisor(new InMemoryChatMemory()))
                .build();
    }
    @PostMapping("/mcp/chat")
    public Flux<String> chat(@RequestBody ChatRequest chatRequest) {
        return chatClient.prompt().user(user -> {
            user.text(chatRequest.getMessages()[0].getContent());
        }).stream().chatResponse().map(chatObj -> chatObj.getResult().getOutput().getText())
                .onErrorResume(e -> Flux.just("大模型服务不可用"));
    }
}
