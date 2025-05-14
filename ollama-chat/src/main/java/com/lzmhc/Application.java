package com.lzmhc;


import com.lzmhc.mcpserver.KnowledgeService;
import com.lzmhc.mcpserver.OpenMeteoService;
import io.modelcontextprotocol.client.McpAsyncClient;
import org.springframework.ai.mcp.AsyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
public class Application {
//    @Bean
//    public AsyncMcpToolCallbackProvider asyncMcpToolCallbackProvider(
//            List<McpAsyncClient> mcpAsyncClients) {
//        return new AsyncMcpToolCallbackProvider(mcpAsyncClients);
//    }
    @Bean
    public ToolCallbackProvider weatherTools(OpenMeteoService openMeteoService, KnowledgeService knowledgeService) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(openMeteoService)
                .toolObjects(knowledgeService)
                .build();
    }
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
