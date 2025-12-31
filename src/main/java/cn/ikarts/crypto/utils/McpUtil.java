package cn.ikarts.crypto.utils;

import cn.hutool.core.util.URLUtil;
import cn.ikarts.crypto.entity.McpConfig;
import io.micrometer.common.util.StringUtils;
import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.internal.StringUtil;
import reactor.core.publisher.Mono;

import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;


/**
 * @author shenhuan
 * @date 2025-12-31 11:31
 **/
@Slf4j
@Deprecated
public class McpUtil {

    /**
     * 创建Mcp客户端
     *
     * @param mcpConfigs mcp配置列表
     * @return mcp客户端列表
     */
    public static List<McpAsyncClient> mcpAsyncClient(List<McpConfig> mcpConfigs) {
        List<McpAsyncClient> clients = new ArrayList<>();

        for (McpConfig mcpConfig : mcpConfigs) {
            clients.add(mcpAsyncClient(mcpConfig));
        }
        return clients;
    }

    /**
     * 创建Mcp客户端
     *
     * @param mcpConfig
     * @return
     */
    public static McpAsyncClient mcpAsyncClient(McpConfig mcpConfig) {
        String url = mcpConfig.getMcpUrl();
        String header = mcpConfig.getHeader();
        Integer timeout = mcpConfig.getTimeout();

        List<String> headers = new ArrayList<>();
        headers.add("Content-Type");
        headers.add("application/json");
        if (StringUtils.isNotBlank(header)) {
            headers.addAll(parseHeader(header));
        }

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .headers(headers.toArray(new String[0]));

        String protocol = URLUtil.url(url).getProtocol();
        String host = URLUtil.url(url).getHost();
        String endPoint = URLUtil.url(url).getPath();
        int port = URLUtil.url(url).getPort();

        // 如果没有端口则忽略
        String baseUrl = port == -1 ? String.format("%s://%s", protocol, host) : String.format("%s://%s:%s", protocol, host, port);

        HttpClientSseClientTransport clientTransport = HttpClientSseClientTransport
                .builder(baseUrl)
                .sseEndpoint(endPoint)
                .requestBuilder(builder)
                .build();

        McpSchema.Implementation clientInfo = new McpSchema.Implementation("", "1.0");
        McpAsyncClient client = null;
        try {
            client = McpClient.async(clientTransport)
                    .clientInfo(clientInfo)
                    .capabilities(McpSchema.ClientCapabilities.builder()
                            .roots(true)
                            .sampling()
                            .build())
                    .loggingConsumer(logging -> Mono.fromRunnable(() -> {
                        log.info("Receive MCP Message: " + logging.data());
                    }))
                    .sampling(request -> {
                        log.info("MCP Sampling: " + request);
                        return Mono.empty();
                    })
                    .requestTimeout(Duration.ofSeconds(timeout))
                    .build();

            client.setLoggingLevel(McpSchema.LoggingLevel.INFO);

            client.initialize().block();
        } catch (Exception e) {
            if (e instanceof TimeoutException) {
                log.error("Mcp连接超时:{}", e.getMessage());
            } else {
                log.error("创建Mcp客户端错误:{}", e.getMessage());
            }
        }

        return client;
    }

    /**
     * 解析Mcp请求头
     *
     * @param header 请求头
     * @return 请求头列表
     */
    private static List<String> parseHeader(String header) {
        List<String> headers = new ArrayList<>();
        if (StringUtil.isBlank(header)) {
            return headers;
        }

        String[] lines = header.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }

            int index = line.indexOf('=');
            if (index > 0) {
                String key = line.substring(0, index).trim();
                String value = line.substring(index + 1).trim();
                headers.add(key);
                headers.add(value);
            }
        }

        return headers;
    }
}
