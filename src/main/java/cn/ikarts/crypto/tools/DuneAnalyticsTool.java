package cn.ikarts.crypto.tools;

import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Component
public class DuneAnalyticsTool {
    private static final Logger logger = LoggerFactory.getLogger(DuneAnalyticsTool.class);

    private static final String BASE_URL = "https://api.dune.com/api/v1";
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private final String apiKey;

    public DuneAnalyticsTool() {
        // TODO 密钥系统参数配置或者数据库配置
        this.apiKey = "SIzz7IDn2ajulHqKUHskcL3QKo7KrRgb";
        if (!StringUtils.hasText(apiKey)) {
            throw new IllegalStateException("请设置环境变量 DUNE_API_KEY");
        }
    }

    @Tool(name = "dune_execute_query",
            description = "执行已保存的 Dune 查询，返回最新结果。适用于 TVL、活跃地址、DEX 交易量、NFT 等")
    public String executeQuery(@ToolParam(name = "query_id", description = "Dune 查询 ID，如 123456") int queryId) throws IOException {
        String url = BASE_URL + "/query/" + queryId + "/results?api_key=" + apiKey;
        Request request = new Request.Builder().url(url).build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                logger.error("[DuneAnalyticsTool] Dune Analytics API 请求失败: HTTP " + response.code() + " - " + response.body().string());
                return "暂时无法请求到数据，失败原因：" + response.message();
            }

            logger.info("[DuneAnalyticsTool] 请求Dune Analytics API 成功，响应结果: {}", response.body().string());

            return mapper.readTree(response.body().string()).toPrettyString();
        }
    }
}