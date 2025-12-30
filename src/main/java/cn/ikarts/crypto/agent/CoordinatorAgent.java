//package cn.ikarts.crypto.agent;
//
//import cn.ikarts.crypto.hook.MonitoringHook;
//import cn.ikarts.crypto.utils.ModelHelper;
//import io.agentscope.core.ReActAgent;
//import io.agentscope.core.agent.AgentBase;
//import io.agentscope.core.agent.Event;
//import io.agentscope.core.memory.InMemoryMemory;
//import io.agentscope.core.memory.autocontext.AutoContextConfig;
//import io.agentscope.core.memory.autocontext.AutoContextMemory;
//import io.agentscope.core.message.Msg;
//import io.agentscope.core.pipeline.Pipelines;
//import io.agentscope.core.pipeline.SequentialPipeline;
//import io.agentscope.core.session.SessionManager;
//import io.agentscope.core.session.mysql.MysqlSession;
//import io.agentscope.core.tool.Toolkit;
//import jakarta.annotation.Resource;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Component;
//import reactor.core.publisher.Flux;
//
//import javax.sql.DataSource;
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * 规划器智能体,接收用户查询，分解任务，协调其他代理，监控进度和异常。
// *
// * @author shenhuan
// * @date 2025-12-30 09:37
// **/
//public class CoordinatorAgent {
//    private static final Logger logger = LoggerFactory.getLogger(CoordinatorAgent.class);
//
//    @Resource
//    private DataSource dataSource;
//
//    private final ReActAgent coordinatorAgent;
//    private final AnalyzerAgent analyzerAgent;
//    private final SynthesizerAgent synthesizerAgent;
//    private final ResearcherAgent researcherAgent;
//
//    // 规划器工具
//    Toolkit toolkit = new Toolkit();
//
//    public CoordinatorAgent() {
//
//        // TODO 添加规划期需要的工具
//
//        this.coordinatorAgent = ReActAgent.builder()
//                .name("CoordinatorAgent")
//                .sysPrompt("你是一个协调代理，分解任务并协调其他代理")
//                .model(ModelHelper.getLanguageModel())
//                .hook(new MonitoringHook())
//                .memory(new InMemoryMemory())
//                .toolkit(new Toolkit())
//                .build();
//
//        this.analyzerAgent = new AnalyzerAgent();
//        this.synthesizerAgent = new SynthesizerAgent();
//        this.researcherAgent = new ResearcherAgent();
//    }
//
//    /**
//     * 智能体开始分析用户问题
//     *
//     * @param userMsg
//     * @return
//     */
//    public Flux<Event> run(Msg userMsg, String sessionId) {
//
//        // 上下文管理
//        AutoContextConfig autoContextConfig = AutoContextConfig.builder().tokenRatio(0.4).lastKeep(10).build();
//        AutoContextMemory memory = new AutoContextMemory(autoContextConfig, ModelHelper.getLanguageModel());
//
//        SessionManager sessionManager = SessionManager.forSessionId(sessionId)
//                .withSession(new MysqlSession(dataSource, "crypto_agent", sessionId, true))
//                .addComponent(memory)
//                .addComponent(toolkit);
//
//        loadSession(sessionManager, sessionId);
//        List<AgentBase> agents = new ArrayList<>();
////        agents.addAll(coordinatorAgent, researcherAgent, analyzerAgent, synthesizerAgent)
////        SequentialPipeline
//
//        return coordinatorAgent.stream(userMsg)
//                .doFinally(signalType -> {
//                    logger.info("[CoordinatorAgent] 规划器执行完毕，结束信号{}，保存session：{}", signalType, sessionId);
//                    sessionManager.saveSession();
//                });
//    }
//
//    private static void loadSession(SessionManager sessionManager, String sessionId) {
//        if (sessionManager.sessionExists()) {
//            // Load existing session
//            sessionManager.loadIfExists();
//        } else {
//            logger.info("✓ New session created: {}\n", sessionId);
//        }
//    }
//
////    private  SequentialPipeline buildPipeline() {
////        return SequentialPipeline.builder()
////                .addAgent(coordinatorAgent)
////                .addAgent(researcherAgent)
////                .addAgent(analyzerAgent)
////                .addAgent(synthesizerAgent)
////                .build();
////    }
//}
