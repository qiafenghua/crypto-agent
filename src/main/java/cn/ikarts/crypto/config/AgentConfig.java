package cn.ikarts.crypto.config;

import cn.ikarts.crypto.hook.MonitoringHook;
import cn.ikarts.crypto.tools.*;
import cn.ikarts.crypto.utils.ModelHelper;
import cn.ikarts.crypto.utils.PromptHelper;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.memory.InMemoryMemory;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.core.tool.mcp.McpClientBuilder;
import io.agentscope.core.tool.mcp.McpClientWrapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * 智能体定义及配置
 *
 * @author shenhuan
 * @date 2025-12-30 15:53
 **/
@Configuration
public class AgentConfig {

    /**
     * 分析收集数据，进行趋势预测、风险评估和项目评估。
     */
    @Bean("analyzerAgent")
    public ReActAgent analyzerAgent() {
        // 添加分析工具
        Toolkit toolkit = new Toolkit();

        return ReActAgent.builder()
                .name("AnalyzerAgent")
                .sysPrompt(PromptHelper.buildAnalyzerSystemPrompt())
                .model(ModelHelper.getLanguageModel())
                .memory(new InMemoryMemory())
                .toolkit(toolkit)
                .build();
    }

    /**
     * 规划器智能体,接收用户查询，分解任务，协调其他代理，监控进度和异常。
     */
    @Bean("coordinatorAgent")
    public ReActAgent coordinatorAgent() {

        Toolkit toolkit = new Toolkit();

        return ReActAgent.builder()
                .name("CoordinatorAgent")
                .sysPrompt(PromptHelper.buildCoordinatorSystemPrompt())
                .model(ModelHelper.getLanguageModel())
                .hook(new MonitoringHook())
                .memory(new InMemoryMemory())
                .toolkit(toolkit)
                .build();
    }

    /**
     * 自主收集加密货币和项目方信息，包括实时数据、新闻和社区反馈。
     */
    @Bean("researcherAgent")
    public ReActAgent researcherAgent() {

        //coingecko mcp工具
        McpClientWrapper sseClient = McpClientBuilder.create("remote-mcp")
                .sseTransport("https://mcp.api.coingecko.com/sse")
                .timeout(Duration.ofSeconds(60))
                .buildAsync()
                .block();

        Toolkit toolkit = new Toolkit();
        toolkit.registerTool(new XSearchTool());
//        toolkit.registerTool(new CoingeckoTool());
//        toolkit.registerTool(new DuneAnalyticsTool());
//        toolkit.registerTool(new DefiLlamaTool());
        toolkit.registerTool(new RootDataTool());
        toolkit.registerMcpClient(sseClient);

        return ReActAgent.builder()
                .name("ResearcherAgent")
                .sysPrompt(PromptHelper.buildResearcherSystemPrompt())
                .model(ModelHelper.getLanguageModel())
                .memory(new InMemoryMemory())
                .toolkit(toolkit)
                .build();
    }

    /**
     * 整合所有信息，生成全面参考报告，确保逻辑连贯和来源引用。
     */
    @Bean("synthesizerAgent")
    public ReActAgent synthesizerAgent() {

        //图表生成工具
        McpClientWrapper sseClient = McpClientBuilder.create("remote-mcp")
                .sseTransport("https://mcp.api-inference.modelscope.net/6adbd84c10b141/sse")
                .timeout(Duration.ofSeconds(60))
                .buildAsync()
                .block();

        Toolkit toolkit = new Toolkit();
        toolkit.registerMcpClient(sseClient);

        return ReActAgent.builder()
                .name("SynthesizerAgent")
                .sysPrompt(PromptHelper.buildSynthesizerSystemPrompt())
                .model(ModelHelper.getLanguageModel())
                .memory(new InMemoryMemory())
                .toolkit(toolkit)
                .build();
    }

}
