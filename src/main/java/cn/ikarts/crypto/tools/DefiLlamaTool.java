package cn.ikarts.crypto.tools;

import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Component
public class DefiLlamaTool {
    private static final Logger logger = LoggerFactory.getLogger(DefiLlamaTool.class);
    private static final String BASE_URL = "https://api.llama.fi";
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    @Tool(name = "defillama_get_protocol_tvl",
            description = "获取指定 DeFi 协议的当前和历史 TVL 数据")
    public String getProtocolTvl(@ToolParam(name = "protocol_slug", description = "协议 slug，如 uniswap, aave, lido") String slug) throws IOException {
        String url = BASE_URL + "/protocol/" + slug;
        Request request = new Request.Builder().url(url).build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                logger.error("查询TVL失败，错误原因：" + response);
                return "暂时无法获取TVL数据，失败原因：" + response.message();
            }
            logger.info("[DefiLlamaTool] 请求Defillama API成功，请求结果：{}", response.body().string());
            return mapper.readTree(response.body().string()).toPrettyString();
        }
    }

    @Tool(name = "defillama_get_chain_tvl",
            description = "获取所有链的 TVL 排名和数据")
    public String getChainTvl() throws IOException {
        String url = BASE_URL + "/chains";
        Request request = new Request.Builder().url(url).build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                logger.error("查询TVL失败，错误原因：" + response);
                return "暂时无法获取TVL数据，失败原因：" + response.message();
            }
            logger.info("[DefiLlamaTool] 请求Defillama API成功，请求结果: {}", response.body().string());
            return mapper.readTree(response.body().string()).toPrettyString();
        }
    }
}