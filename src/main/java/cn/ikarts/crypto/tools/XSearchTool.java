package cn.ikarts.crypto.tools;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * X（Twitter）搜索工具
 *
 * @author shenhuan
 * @date 2025-12-30 11:27
 **/
@Component
public class XSearchTool {
    private static final Logger logger = LoggerFactory.getLogger(XSearchTool.class);

    private static final String BASE_URL = "https://api.desearch.ai/twitter";
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private final String apiKey;

    public XSearchTool() {
        this.apiKey = "dt_$tg2KjPiSXzU0obmfyoP2JZmNnfoMK5mQjpqQQ01rfGM";
        if (!StringUtils.hasText(apiKey)) {
            throw new IllegalStateException("请设置环境变量 DESEARCH_API_KEY 以使用 DeSearch 搜索工具");
        }
    }

    /**
     * DeSearch 高级推文搜索工具（支持关键词、语义混合查询）
     */
    @Tool(name = "desearch_keyword_search",
            description = """
                    在 X (Twitter) 上进行高级搜索，返回高质量推文的核心信息（作者、内容、互动数据）。
                    支持自然语言查询、关键词、运算符（如 OR, "exact phrase", from:user, since:2025-01-01 等）。
                    适用于采集社区情绪、项目官方公告、KOL 观点、热点事件讨论。
                    """)
    public String searchTweets(
            @ToolParam(name = "query", description = "搜索查询，支持自然语言或高级运算符。例如：\n" +
                    "- \"Solana meme coins\"\n" +
                    "- \"$SOL OR solana from:verified min_faves:50 since:2025-12-01\"\n" +
                    "- \"What are the risks of investing in $PEPE?\"") String query,

            @ToolParam(name = "count", description = "返回推文数量，范围 1-50，推荐 10-20", required = false) Integer count,

            @ToolParam(name = "sort", description = "排序方式：Top（热门）或 Latest（最新），默认 Top", required = false) String sort,

            @ToolParam(name = "lang", description = "语言过滤，如 en（英文）、zh（中文），默认 en", required = false) String lang
    ) throws IOException {

        // 参数默认值
        int finalCount = (count == null || count < 1 || count > 50) ? 20 : count;
        String finalSort = (sort == null || !sort.equals("Latest")) ? "Top" : "Latest";
        String finalLang = (lang == null) ? "en" : lang;

        // 编码查询
        String encodedQuery = java.net.URLEncoder.encode(query, "UTF-8");

        // 构建 URL
        String url = BASE_URL +
                "?query=" + encodedQuery +
                "&count=" + finalCount +
                "&sort=" + finalSort +
                "&lang=" + finalLang;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .header("accept", "application/json")
                .header("content-type", "application/json")
                .header("Authorization", apiKey)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                logger.error("DeSearch API 请求失败，状态码: {}, 错误: {}", response.code(), response.message());
                return "查询X推文失败，错误原因：" + response.message();
            }

            var responseBody = response.body();
            if (responseBody == null) {
                logger.error("响应体为空");
                return "查询X推文失败，响应体为空";
            }

            String bodyString = responseBody.string();
            logger.info("DeSearch API 请求成功，查询: {}, 返回推文数: {}", query, finalCount);

            JsonNode fullData = mapper.readTree(bodyString);

            // 提取关键信息，减少上下文长度
            return cn.ikarts.crypto.utils.KeyInfoExtractUtils.extractXKeyInfo(fullData);
        }
    }


}