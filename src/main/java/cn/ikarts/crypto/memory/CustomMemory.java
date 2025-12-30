package cn.ikarts.crypto.memory;

import cn.ikarts.crypto.utils.ModelHelper;
import io.agentscope.core.memory.autocontext.AutoContextConfig;
import io.agentscope.core.memory.autocontext.AutoContextMemory;
import io.agentscope.core.message.Msg;

import java.util.List;

/**
 * 智能体上下文记忆
 *
 * @author shenhuan
 * @date 2025-12-30 10:06
 **/
public class CustomMemory {

    private AutoContextMemory memory;

    // TODO 保存到数据库中
    public CustomMemory() {
        AutoContextConfig contextConfig = AutoContextConfig.builder()
                .msgThreshold(50)
                .maxToken(64 * 1024)
                .tokenRatio(0.7)
                .lastKeep(20)
                .largePayloadThreshold(10 * 1024)
                .offloadSinglePreview(300)
                .minConsecutiveToolMessages(4)
                .currentRoundCompressionRatio(0.3)  // 当前轮次压缩到 30%
                .build();

        this.memory = new AutoContextMemory(contextConfig, ModelHelper.getLanguageModel());
    }

    public AutoContextMemory getMemory() {
        return memory;
    }

}
