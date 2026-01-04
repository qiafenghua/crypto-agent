package cn.ikarts.crypto;

import cn.ikarts.crypto.utils.KeyInfoExtractUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author shenhuan
 * @date 2026-01-04 14:35
 **/
public class CoingeckoTest {

    private static final String BASE_URL = "https://api.coingecko.com/api/v3";
    private static final String API_KEY = "CG-fd5P5STaA1X5iE1RsFYKLJnG";
    private static final String API_KEY_HEADER = "x-cg-demo-api-key";
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) {
        CoingeckoTest coingeckoTest = new CoingeckoTest();
        coingeckoTest.search();
    }


    public void search() {
        try {
            String url = BASE_URL + "/search?query=" + java.net.URLEncoder.encode("aster", StandardCharsets.UTF_8);
            //设置超时时间
            client.newBuilder().callTimeout(60, java.util.concurrent.TimeUnit.SECONDS).build();

            Request request = new Request.Builder()
                    .header(API_KEY_HEADER, API_KEY)
                    .url(url)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    System.out.println("搜索失败加密货币失败，失败原因： " + response.message());
                }

                String keyInfo = KeyInfoExtractUtils.extractCoingeckoKeyInfo(mapper.readTree(response.body().string()));

                System.out.println("[CoingeckoTool] 搜索成功，请求结果: " + keyInfo);
            }
        } catch (IOException e) {
            System.out.println("请求CoinGecko API错误: " + e);
        }

    }

}
