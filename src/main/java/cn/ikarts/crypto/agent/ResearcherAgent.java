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
//import reactor.core.publisher.Mono;
//
//
///**
// * 自主收集加密货币和项目方信息，包括实时数据、新闻和社区反馈。
// *
// * @author shenhuan
// * @date 2025-12-30 09:51
// **/
//public class ResearcherAgent {
//    private static final Logger logger = LoggerFactory.getLogger(ResearcherAgent.class);
//
//    private final ReActAgent agent;
//
//    private final CustomMemory memory;
//
////    private final
//
//    public ResearcherAgent() {
//
//        //提供搜索研究工具
//        Toolkit toolkit = new Toolkit();
////        toolkit.registerTool(new CoingeckoTool());
//
//        //上下文管理
//        memory = new CustomMemory();
//
//        this.agent = ReActAgent.builder()
//                .name("ResearcherAgent")
//                .sysPrompt("你是一个自主收集加密货币和项目方信息，包括实时数据、新闻和社区反馈的代理")
//                .model(ModelHelper.getLanguageModel())
//                .memory(new InMemoryMemory())
//                .toolkit(toolkit)
//                .enablePlan()
//                .hook(new AutoContextHook())
//                .build();
//    }
//
//    /**
//     * 搜索
//     *
//     * @param task 任务
//     * @return 结果
//     */
//    public Mono<Msg> search(Msg task) {
//        logger.info("【ResearcherAgent】：收到用户请求:{},开始搜索相关信息！", task);
//        return agent.call(task);
//    }
//}
