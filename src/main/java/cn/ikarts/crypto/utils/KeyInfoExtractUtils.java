package cn.ikarts.crypto.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * 关键信息提取工具
 *
 * @author shenhuan
 * @date 2025-12-31 15:40
 **/
public class KeyInfoExtractUtils {

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * 提取X搜索工具中的关键字段信息
     *
     * @param data x数据（数组格式）
     * @return 精简后的JSON字符串
     */
    public static String extractXKeyInfo(JsonNode data) {
        if (data == null || !data.isArray()) {
            return "[]";
        }

        ArrayNode result = mapper.createArrayNode();

        for (JsonNode tweet : data) {
            ObjectNode simplified = mapper.createObjectNode();

            // 用户信息
            JsonNode user = tweet.get("user");
            if (user != null) {
                ObjectNode userInfo = mapper.createObjectNode();
                userInfo.put("username", getTextValue(user, "username"));
                userInfo.put("name", getTextValue(user, "name"));
                userInfo.put("followers", getIntValue(user, "followers_count"));
                userInfo.put("verified", getBoolValue(user, "is_blue_verified"));
                simplified.set("user", userInfo);
            }

            // 推文内容
            simplified.put("text", getTextValue(tweet, "text"));
            simplified.put("created_at", getTextValue(tweet, "created_at"));
            simplified.put("url", getTextValue(tweet, "url"));
            simplified.put("lang", getTextValue(tweet, "lang"));

            // 互动数据
            ObjectNode engagement = mapper.createObjectNode();
            engagement.put("views", getIntValue(tweet, "view_count"));
            engagement.put("likes", getIntValue(tweet, "like_count"));
            engagement.put("retweets", getIntValue(tweet, "retweet_count"));
            engagement.put("replies", getIntValue(tweet, "reply_count"));
            engagement.put("quotes", getIntValue(tweet, "quote_count"));
            simplified.set("engagement", engagement);

            // 媒体（仅保留URL）
            JsonNode media = tweet.get("media");
            if (media != null && media.isArray() && media.size() > 0) {
                ArrayNode mediaUrls = mapper.createArrayNode();
                for (JsonNode m : media) {
                    String mediaUrl = getTextValue(m, "media_url");
                    if (mediaUrl != null && !mediaUrl.isEmpty()) {
                        mediaUrls.add(mediaUrl);
                    }
                }
                if (mediaUrls.size() > 0) {
                    simplified.set("media_urls", mediaUrls);
                }
            }

            result.add(simplified);
        }

        return result.toString();
    }

    /**
     * 提取CoinGecko搜索工具中的关键字段信息，过滤掉图片信息
     *
     * @param data CoinGecko搜索结果数据（包含coins、exchanges、categories、nfts、icos等）
     * @return 精简后的JSON字符串
     */
    public static String extractCoingeckoKeyInfo(JsonNode data) {
        if (data == null || !data.isObject()) {
            return "{}";
        }

        ObjectNode result = mapper.createObjectNode();

        // 提取coins数组（过滤thumb和large）
        JsonNode coins = data.get("coins");
        if (coins != null && coins.isArray()) {
            ArrayNode coinsArray = mapper.createArrayNode();
            for (JsonNode coin : coins) {
                ObjectNode simplified = mapper.createObjectNode();
                simplified.put("id", getTextValue(coin, "id"));
                simplified.put("name", getTextValue(coin, "name"));
                simplified.put("api_symbol", getTextValue(coin, "api_symbol"));
                simplified.put("symbol", getTextValue(coin, "symbol"));
                simplified.put("market_cap_rank", getIntValue(coin, "market_cap_rank"));
                coinsArray.add(simplified);
            }
            result.set("coins", coinsArray);
        } else {
            result.set("coins", mapper.createArrayNode());
        }

        // 提取exchanges数组（过滤thumb和large）
        JsonNode exchanges = data.get("exchanges");
        if (exchanges != null && exchanges.isArray()) {
            ArrayNode exchangesArray = mapper.createArrayNode();
            for (JsonNode exchange : exchanges) {
                ObjectNode simplified = mapper.createObjectNode();
                simplified.put("id", getTextValue(exchange, "id"));
                simplified.put("name", getTextValue(exchange, "name"));
                simplified.put("market_type", getTextValue(exchange, "market_type"));
                exchangesArray.add(simplified);
            }
            result.set("exchanges", exchangesArray);
        } else {
            result.set("exchanges", mapper.createArrayNode());
        }

        // 提取categories数组（无图片字段）
        JsonNode categories = data.get("categories");
        if (categories != null && categories.isArray()) {
            ArrayNode categoriesArray = mapper.createArrayNode();
            for (JsonNode category : categories) {
                ObjectNode simplified = mapper.createObjectNode();
                simplified.put("id", getTextValue(category, "id"));
                simplified.put("name", getTextValue(category, "name"));
                categoriesArray.add(simplified);
            }
            result.set("categories", categoriesArray);
        } else {
            result.set("categories", mapper.createArrayNode());
        }

        // 提取nfts数组（过滤thumb）
        JsonNode nfts = data.get("nfts");
        if (nfts != null && nfts.isArray()) {
            ArrayNode nftsArray = mapper.createArrayNode();
            for (JsonNode nft : nfts) {
                ObjectNode simplified = mapper.createObjectNode();
                simplified.put("id", getTextValue(nft, "id"));
                simplified.put("name", getTextValue(nft, "name"));
                simplified.put("symbol", getTextValue(nft, "symbol"));
                nftsArray.add(simplified);
            }
            result.set("nfts", nftsArray);
        } else {
            result.set("nfts", mapper.createArrayNode());
        }

        // 保留icos数组（通常为空）
        JsonNode icos = data.get("icos");
        if (icos != null && icos.isArray()) {
            result.set("icos", icos);
        } else {
            result.set("icos", mapper.createArrayNode());
        }

        return result.toString();
    }

    private static String getTextValue(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value != null && !value.isNull() ? value.asText() : "";
    }

    private static int getIntValue(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value != null && !value.isNull() ? value.asInt(0) : 0;
    }

    private static boolean getBoolValue(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value != null && !value.isNull() && value.asBoolean(false);
    }
}
