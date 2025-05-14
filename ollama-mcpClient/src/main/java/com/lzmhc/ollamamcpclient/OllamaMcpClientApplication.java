package com.lzmhc.ollamamcpclient;


import io.modelcontextprotocol.client.McpAsyncClient;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.mcp.AsyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.List;

@SpringBootApplication(exclude = {
        org.springframework.ai.autoconfigure.mcp.client.SseHttpClientTransportAutoConfiguration.class
})
public class OllamaMcpClientApplication {
    @Primary
    @Bean
    public AsyncMcpToolCallbackProvider asyncMcpToolCallbackProvider(
            List<McpAsyncClient> mcpAsyncClients) {
        return new AsyncMcpToolCallbackProvider(mcpAsyncClients);
    }
    @Bean
    public CommandLineRunner predefinedQuestions(ChatClient.Builder chatClientBuilder,
                                                 AsyncMcpToolCallbackProvider tools,
                                                 ConfigurableApplicationContext context) {
        return args -> {
            // 构建ChatClient并注入MCP工具
            var chatClient = chatClientBuilder
                    .defaultTools(tools.getToolCallbacks())
                    .build();

            // 使用ChatClient与LLM交互
            String userInput = "兰州的天气如何？";
            System.out.println("\n>>> QUESTION: " + userInput);
            System.out.println("\n>>> ASSISTANT: " + chatClient.prompt(userInput).call().content());
            context.close();
        };
    }

    public static void main(String[] args) {
        SpringApplication.run(OllamaMcpClientApplication.class, args);
    }

}
