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
