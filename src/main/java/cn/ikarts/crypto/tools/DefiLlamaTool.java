//package cn.ikarts.crypto.tools;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Component;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.node.ObjectNode;
//
//import io.agentscope.core.tool.Tool;
//import io.agentscope.core.tool.ToolParam;
//import okhttp3.OkHttpClient;
//import okhttp3.Request;
//import okhttp3.Response;
//
//@Component
//public class DefiLlamaTool {
//    private static final Logger logger = LoggerFactory.getLogger(DefiLlamaTool.class);
//    private static final String BASE_URL = "https://api.llama.fi";
//    private final OkHttpClient client = new OkHttpClient();
//    private final ObjectMapper mapper = new ObjectMapper();
//
//    @Tool(name = "defillama_get_protocol_tvl",
//            description = "获取指定 DeFi 协议的关键 TVL 数据，包括当前 TVL、24小时变化、协议类型等核心指标")
//    public String getProtocolTvl(@ToolParam(name = "protocol_slug", description = "协议 slug，如 uniswap, aave, lido") String slug) throws IOException {
//        String url = BASE_URL + "/protocol/" + slug;
//        Request request = new Request.Builder().url(url).build();
//
//        try (Response response = client.newCall(request).execute()) {
//            if (!response.isSuccessful()) {
//                logger.error("查询TVL失败，状态码: {}, 错误: {}", response.code(), response.message());
//                return "暂时无法获取TVL数据，失败原因：" + response.message();
//            }
//
//            var responseBody = response.body();
//            if (responseBody == null) {
//                logger.error("响应体为空");
//                return "暂时无法获取TVL数据，响应体为空";
//            }
//
//            String bodyString = responseBody.string();
//            logger.info("[DefiLlamaTool] 请求Defillama API成功，协议: {}", slug);
//
//            JsonNode fullData = mapper.readTree(bodyString);
//
//            // 只提取关键信息，减少上下文长度
//            ObjectNode summary = mapper.createObjectNode();
//            summary.put("name", fullData.path("name").asText());
//            summary.put("symbol", fullData.path("symbol").asText());
//            summary.put("category", fullData.path("category").asText());
//            summary.put("chains", fullData.path("chains").toString());
//
//            // 当前TVL
//            summary.put("tvl", fullData.path("tvl").asDouble());
//            summary.put("change_1h", fullData.path("change_1h").asDouble());
//            summary.put("change_1d", fullData.path("change_1d").asDouble());
//            summary.put("change_7d", fullData.path("change_7d").asDouble());
//
//            // 只保留最近7天的历史数据，而不是全部
//            if (fullData.has("tvl")) {
//                JsonNode tvlArray = fullData.get("tvl");
//                if (tvlArray.isArray() && tvlArray.size() > 0) {
//                    List<JsonNode> recentTvl = new ArrayList<>();
//                    int startIndex = Math.max(0, tvlArray.size() - 7);
//                    for (int i = startIndex; i < tvlArray.size(); i++) {
//                        recentTvl.add(tvlArray.get(i));
//                    }
//                    summary.set("recent_7d_tvl", mapper.valueToTree(recentTvl));
//                }
//            }
//
//            // 其他有用信息
//            if (fullData.has("url")) {
//                summary.put("url", fullData.path("url").asText());
//            }
//            if (fullData.has("description")) {
//                summary.put("description", fullData.path("description").asText());
//            }
//
//            return summary.toPrettyString();
//        }
//    }
//}