package cn.ikarts.crypto.utils;

import io.agentscope.core.model.DashScopeChatModel;

/**
 * 模型提供工具类
 *
 * @author shenhuan
 * @date 2025-12-30 09:47
 **/
public class ModelHelper {


    private static final String DASHSCOPE_API_KEY = "sk-6e3be3651c8845aa92130a76f86c2fe8";

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
