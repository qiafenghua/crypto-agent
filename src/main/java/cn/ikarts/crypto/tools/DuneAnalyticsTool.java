//package cn.ikarts.crypto.tools;
//
//import java.io.IOException;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Component;
//import org.springframework.util.StringUtils;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.node.ArrayNode;
//import com.fasterxml.jackson.databind.node.ObjectNode;
//
//import io.agentscope.core.tool.Tool;
//import io.agentscope.core.tool.ToolParam;
//import okhttp3.OkHttpClient;
//import okhttp3.Request;
//import okhttp3.Response;
//
//@Component
//public class DuneAnalyticsTool {
//    private static final Logger logger = LoggerFactory.getLogger(DuneAnalyticsTool.class);
//
//    private static final String BASE_URL = "https://api.dune.com/api/v1";
//    private final OkHttpClient client = new OkHttpClient();
//    private final ObjectMapper mapper = new ObjectMapper();
//    private final String apiKey;
//
//    public DuneAnalyticsTool() {
//        // TODO 密钥系统参数配置或者数据库配置
//        this.apiKey = "SIzz7IDn2ajulHqKUHskcL3QKo7KrRgb";
//        if (!StringUtils.hasText(apiKey)) {
//            throw new IllegalStateException("请设置环境变量 DUNE_API_KEY");
//        }
//    }
//
//    @Tool(name = "dune_execute_query",
//            description = "执行已保存的 Dune 查询，返回关键结果数据。适用于 TVL、活跃地址、DEX 交易量、NFT 等链上数据分析")
//    public String executeQuery(@ToolParam(name = "query_id", description = "Dune 查询 ID，如 123456") int queryId) throws IOException {
//        String url = BASE_URL + "/query/" + queryId + "/results?api_key=" + apiKey;
//        Request request = new Request.Builder().url(url).build();
//
//        try (Response response = client.newCall(request).execute()) {
//            if (!response.isSuccessful()) {
//                logger.error("[DuneAnalyticsTool] Dune Analytics API 请求失败，状态码: {}, 错误: {}", response.code(), response.message());
//                return "暂时无法请求到数据，失败原因：" + response.message();
//            }
//
//            var responseBody = response.body();
//            if (responseBody == null) {
//                logger.error("[DuneAnalyticsTool] 响应体为空");
//                return "暂时无法请求到数据，响应体为空";
//            }
//
//            String bodyString = responseBody.string();
//            logger.info("[DuneAnalyticsTool] 请求Dune Analytics API 成功，查询ID: {}", queryId);
//
////            JsonNode fullData = mapper.readTree(bodyString);
//            return bodyString;
//        }
//    }
//
//
//}