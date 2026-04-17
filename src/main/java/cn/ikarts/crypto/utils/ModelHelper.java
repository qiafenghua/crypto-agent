package cn.ikarts.crypto.utils;

import io.agentscope.core.model.DashScopeChatModel;

/**
 * 模型提供工具类
 *
 * @author shenhuan
 * @date 2025-12-30 09:47
 **/
public class ModelHelper {


    private static final String DASHSCOPE_API_KEY = "your-dashscope-api-key";

    public static DashScopeChatModel getLanguageModel(String modelName) {
        return DashScopeChatModel.builder()
                .apiKey(DASHSCOPE_API_KEY)
                .modelName(modelName)
                .build();
    }

    public static DashScopeChatModel getLanguageModel() {
        return getLanguageModel("qwen-plus");
    }

}
