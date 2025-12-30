package cn.ikarts.crypto.utils;

import cn.ikarts.crypto.constants.PromptConstant;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;

import java.util.Map;

/**
 * 提示词工具类，系统提示词都由此提供
 *
 * @author shenhuan
 * @date 2025-12-30 15:02
 **/
public class PromptHelper {


    /**
     * 任务协调规划系统提示
     */
    public static String buildCoordinatorSystemPrompt() {
        return PromptConstant.COORDINATOR_SYSTEM_PROMPT;
    }


    /**
     * 自主数据收集系统提示
     */
    public static String buildResearcherSystemPrompt() {
        return PromptConstant.RESEARCHER_SYSTEM_PROMPT;
    }

    /**
     * 智能分析系统提示
     */
    public static String buildAnalyzerSystemPrompt() {
        return PromptConstant.ANALYZER_SYSTEM_PROMPT;
    }

    /**
     * 智能合成系统提示
     */
    public static String buildSynthesizerSystemPrompt() {
        return PromptConstant.SYNTHESIZER_SYSTEM_PROMPT;
    }

    /**
     * 智能体协作公告
     */
    public static String buildAnnouncement(String query) {
        return new SystemPromptTemplate(PromptConstant.ANNOUNCEMENT_PROMPT)
                .render(Map.of("query", query));
    }

}
