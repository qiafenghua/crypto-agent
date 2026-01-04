package cn.ikarts.crypto.tools;

import cn.ikarts.crypto.utils.KeyInfoExtractUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * CoinGecko API 工具
 */
@Component
public class CoingeckoTool {
    private static final Logger logger = LoggerFactory.getLogger(CoingeckoTool.class);
    private static final String BASE_URL = "https://api.coingecko.com/api/v3";
    private static final String API_KEY = "CG-fd5P5STaA1X5iE1RsFYKLJnG";
    private static final String API_KEY_HEADER = "x-cg-demo-api-key";
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    @Tool(name = "coingecko_get_coin_data",
            description = "获取指定加密货币的详细信息，包括价格、市值、24h涨幅、历史数据链接等")
    public String getCoinData(@ToolParam(name = "coin_id", description = "CoinGecko coin ID，如 bitcoin, ethereum, solana") String coinId) throws IOException {
        String url = BASE_URL + "/coins/" + coinId + "?localization=false&tickers=false&market_data=true&community_data=true&developer_data=true&sparkline=false";
        Request request = new Request.Builder()
                .header(API_KEY_HEADER, API_KEY)
                .url(url)
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                logger.error("[CoingeckoTool] 请求CoinGecko API错误: {}", response);
                return "暂时无法获取代币详细信息，请求CoinGecko API错误: " + response;
            }
            String result = response.body().string();
            logger.info("[CoingeckoTool] 获取代币详细信息成功，请求结果: {}", result);
            return mapper.readTree(result).toPrettyString();
        }
    }

    @Tool(name = "coingecko_search_coins",
            description = "模糊搜索加密货币，返回最匹配的项目列表及其正确 coin_id。必须在调用其他 CoinGecko 工具前使用此工具验证项目是否存在。")
    public String searchCoins(
            @ToolParam(name = "query", description = "项目名称、符号或关键词，如 'bitcoin', 'BTC', 'Solana'") String query
    ) throws IOException {
        String url = BASE_URL + "/search?query=" + java.net.URLEncoder.encode(query, StandardCharsets.UTF_8);
        Request request = new Request.Builder()
                .header(API_KEY_HEADER, API_KEY)
                .url(url)
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                logger.error("[CoingeckoTool] 搜索失败: {}", response.message());
                return "搜索失败加密货币失败，失败原因： " + response.message();
            }
            String result = response.body().string();
            logger.info("[CoingeckoTool] 搜索成功，请求结果: {}", result);
            return KeyInfoExtractUtils.extractCoingeckoKeyInfo(mapper.readTree(result));
        }
    }

    @Tool(name = "coingecko_get_market_chart",
            description = "获取指定加密货币的历史价格数据（天级）")
    public String getMarketChart(@ToolParam(name = "coin_id", description = "CoinGecko coin ID，如 bitcoin, ethereum, solana") String coinId,
                                 @ToolParam(name = "days", description = "天数：1, 7, 30, 90, 365") String days) throws IOException {
        String url = BASE_URL + "/coins/" + coinId + "/market_chart?vs_currency=usd&days=" + days;
        Request request = new Request.Builder()
                .header(API_KEY_HEADER, API_KEY)
                .url(url)
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                logger.error("[CoingeckoTool] 获取历史价格数据错误: {}", response);
                return "获取历史价格数据错误: " + response;
            }
            String result = response.body().string();
            logger.info("[CoingeckoTool] 请求CoinGecko API成功，请求结果: {}", result);
            return mapper.readTree(result).toPrettyString();
        }
    }
}