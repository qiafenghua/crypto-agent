//package cn.ikarts.crypto.agent;
//
//import cn.ikarts.crypto.memory.CustomMemory;
//import cn.ikarts.crypto.utils.ModelHelper;
//import io.agentscope.core.ReActAgent;
//import io.agentscope.core.memory.InMemoryMemory;
//import io.agentscope.core.memory.autocontext.AutoContextHook;
//import io.agentscope.core.message.Msg;
//import io.agentscope.core.tool.Toolkit;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Component;
//import reactor.core.publisher.Mono;
//
///**
// * 分析收集数据，进行趋势预测、风险评估和项目评估。
// *
// * @author shenhuan
// * @date 2025-12-30 09:52
// **/
//public class AnalyzerAgent {
//    private static final Logger logger = LoggerFactory.getLogger(AnalyzerAgent.class);
//    private final ReActAgent agent;
//    private final CustomMemory memory;
//
//    public AnalyzerAgent() {
//        // 添加分析工具
//        Toolkit toolkit = new Toolkit();
//        //
//
//        //上下文信息
//        memory = new CustomMemory();
//
//        agent = ReActAgent.builder()
//                .name("AnalyzerAgent")
//                .sysPrompt("你是一个分析收集数据，进行趋势预测、风险评估和项目评估的代理")
//                .model(ModelHelper.getLanguageModel())
//                .hook(new AutoContextHook())
//                .memory(new InMemoryMemory())
//                .toolkit(toolkit)
//                .enablePlan()
//                .build();
//    }
//
//    public Mono<Msg> analyze(Msg task) {
//        logger.info("【AnalyzerAgent】：收到用户请求:{}，开始分析用户需求！", task);
//        return agent.call(task);
//    }
//}
