package cn.ikarts.crypto.utils;

import io.agentscope.core.model.DashScopeChatModel;

/**
 * 模型提供工具类
 *
 * @author shenhuan
 * @date 2025-12-30 09:47
 **/
public class ModelHelper {


    private static final String DASHSCOPE_API_KEY = "sk-7c16b6de48a4444a85acf6e15c418618";

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
