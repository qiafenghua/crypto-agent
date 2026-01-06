package cn.ikarts.crypto;

import io.agentscope.core.tool.mcp.McpClientBuilder;
import io.agentscope.core.tool.mcp.McpClientWrapper;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * @author shenhuan
 * @date 2026-01-06 13:43
 **/
public class McpTest {

    public static void main(String[] args) {
        McpClientWrapper sseClient = McpClientBuilder.create("mcp-server-chart")
                .sseTransport("https://mcp.api-inference.modelscope.net/6adbd84c10b141/sse")
                .timeout(Duration.ofSeconds(60))
                .buildAsync()
                .block();

        Mono<Void> mono = sseClient.initialize();
        mono.block();
    }
}
