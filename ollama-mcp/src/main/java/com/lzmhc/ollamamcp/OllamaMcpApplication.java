package com.lzmhc.ollamamcp;

import com.lzmhc.ollamamcp.mcpService.OpenMeteoService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
public class OllamaMcpApplication {
    @Bean
    public ToolCallbackProvider weatherTools(OpenMeteoService openMeteoService) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(openMeteoService)
                .build();
    }
    public static void main(String[] args) {
        SpringApplication.run(OllamaMcpApplication.class, args);
    }

}
