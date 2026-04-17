package cn.ikarts.crypto.agent;

import cn.ikarts.crypto.utils.PromptHelper;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.pipeline.MsgHub;
import io.agentscope.core.session.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

/**
 * 智能体控制器，分析流程均通过该类控制
 *
 * @author shenhuan
 * @date 2025-12-30 16:08
 **/
@Component
public class CryptoAgentManager {

    private static final Logger logger = LoggerFactory.getLogger(CryptoAgentManager.class);

//    private final DataSource dataSource;

//    private final ReActAgent analyzerAgent;

    private final ReActAgent coordinatorAgent;

    private final ReActAgent researcherAgent;

    private final ReActAgent synthesizerAgent;


    public CryptoAgentManager(ReActAgent coordinatorAgent, ReActAgent researcherAgent, ReActAgent synthesizerAgent) {
//        this.dataSource = dataSource;
//        this.analyzerAgent = analyzerAgent;
        this.coordinatorAgent = coordinatorAgent;
        this.researcherAgent = researcherAgent;
        this.synthesizerAgent = synthesizerAgent;
    }


    /**
     * 智能体开始分析用户问题
     *
     * @param query 用户问题
     * @return
     */
    public void run(String query, Sinks.Many<String> sink, String sessionId) {
        // 用户消息
        Msg userMsg =
                Msg.builder()
                        .role(MsgRole.USER)
                        .content(TextBlock.builder().text(query).build())
                        .build();

        // 创建公告消息
        Msg announcement = Msg.builder()
                .name("system")
                .role(MsgRole.SYSTEM)
                .content(TextBlock.builder()
                        .text(PromptHelper.buildAnnouncement(query))
                        .build())
                .build();

        // 智能体通信通道
        try (MsgHub hub = MsgHub.builder()
                .participants(coordinatorAgent, researcherAgent, synthesizerAgent)
                .announcement(announcement)
                .enableAutoBroadcast(true)
                .build()) {

            hub.enter().block();

            // 异步处理智能体调用链，实现流式输出
            processAgentChain(userMsg, sink)
                    .doOnError(error -> {
                        logger.error("智能体处理链出错: {}", error.getMessage(), error);
                        sink.tryEmitError(error);
                    })
                    .doOnComplete(() -> {
                        logger.info("智能体处理链完成");
                        sink.tryEmitComplete();
                    })
                    .subscribe();
        }
    }

    /**
     * 异步处理智能体调用链
     */
    private Flux<Void> processAgentChain(Msg userMsg, Sinks.Many<String> sink) {
        return Flux.defer(() -> {
            // 发送开始信号
            sink.tryEmitNext("🤖 开始分析您的问题...\n");

            return coordinatorAgent.call(userMsg)
                    .doOnNext(msg -> {
                        logger.info("【规划智能体】规划结果： {}", msg.getTextContent());
                        sink.tryEmitNext("📋 **任务规划完成**\n" + msg.getTextContent() + "\n\n");
                    })
                    .flatMap(coordinatorMsg -> {
                        String researchText = String.format("用户问题：%s \n 请根据如下任务规划完成研究任务：%s",
                                userMsg.getTextContent(), coordinatorMsg.getTextContent());
                        Msg researchMsg = Msg.builder()
                                .content(TextBlock.builder().text(researchText).build())
                                .build();

                        sink.tryEmitNext("🔍 开始数据研究...\n");
                        return researcherAgent.call(researchMsg);
                    })
                    .doOnNext(researchMsg -> {
                        logger.info("【研究智能体】研究结果： {}", researchMsg.getTextContent());
                        sink.tryEmitNext(" **数据研究完成**\n" + researchMsg.getTextContent() + "\n\n");
                    })
//                    .flatMap(researchMsg -> {
//                        sink.tryEmitNext("🧠 开始深度分析...\n");
//                        return analyzerAgent.call(researchMsg);
//                    })
//                    .doOnNext(analyzeMsg -> {
//                        logger.info("【分析智能体】分析结果： {}", analyzeMsg.getTextContent());
//                        sink.tryEmitNext("📈 **深度分析完成**\n" + analyzeMsg.getTextContent() + "\n\n");
//                    })
                    .flatMap(analyzeMsg -> {
                        sink.tryEmitNext(" 开始生成最终报告...\n");
                        return synthesizerAgent.call(analyzeMsg);
                    })
                    .doOnNext(synthesizeMsg -> {
                        logger.info("【合成智能体】合成结果： {}", synthesizeMsg.getTextContent());
                        sink.tryEmitNext(" **最终报告**\n" + synthesizeMsg.getTextContent() + "\n");
                    })
                    .then();
        });
    }


    private static void loadSession(SessionManager sessionManager, String sessionId) {
        if (sessionManager.sessionExists()) {
            // Load existing session
            sessionManager.loadIfExists();
        } else {
            logger.info("✓ New session created: {}\n", sessionId);
        }
    }

    public void createSessionManager() {
        // 上下文管理
//        AutoContextConfig autoContextConfig = AutoContextConfig.builder().tokenRatio(0.4).lastKeep(10).build();
//        AutoContextMemory memory = new AutoContextMemory(autoContextConfig, ModelHelper.getLanguageModel());
//
//        SessionManager sessionManager = SessionManager.forSessionId(sessionId)
//                .withSession(new MysqlSession(dataSource, "crypto_agent", sessionId, true))
//                .addComponent(memory);
//
//        loadSession(sessionManager, sessionId);

        //编排智能体执行顺序
//        SequentialPipeline pipeline = SequentialPipeline.builder()
//                .addAgent(coordinatorAgent)
//                .addAgent(researcherAgent)
//                .addAgent(analyzerAgent)
//                .addAgent(synthesizerAgent)
//                .build();
    }


}
