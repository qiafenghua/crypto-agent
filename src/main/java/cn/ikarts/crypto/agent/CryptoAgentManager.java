package cn.ikarts.crypto.agent;

import cn.ikarts.crypto.utils.PromptHelper;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.agent.Event;
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

    private final ReActAgent analyzerAgent;

    private final ReActAgent coordinatorAgent;

    private final ReActAgent researcherAgent;

    private final ReActAgent synthesizerAgent;


    public CryptoAgentManager(ReActAgent analyzerAgent, ReActAgent coordinatorAgent, ReActAgent researcherAgent, ReActAgent synthesizerAgent) {
//        this.dataSource = dataSource;
        this.analyzerAgent = analyzerAgent;
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
                .participants(coordinatorAgent, researcherAgent, analyzerAgent, synthesizerAgent)
                .announcement(announcement)
                .enableAutoBroadcast(true)
                .build()) {

            hub.enter().block();

            Msg msg = coordinatorAgent.call(userMsg).block();
            logger.info("【规划智能体】规划结果： {}", msg.getTextContent());
            sink.tryEmitNext(msg.getTextContent());

            String researchText = String.format("用户问题：{} \n 请根据如下任务规划完成研究任务：{}", userMsg, msg.getTextContent());
            Msg researchMsg = Msg.builder().content(TextBlock.builder().text(researchText).build()).build();
            sink.tryEmitNext(researchMsg.getTextContent());

            Msg serachMsg = researcherAgent.call(researchMsg).block();
            logger.info("【研究智能体】研究结果： {}", serachMsg.getTextContent());
            sink.tryEmitNext(serachMsg.getTextContent());


            Msg analyzeMsg = analyzerAgent.call(serachMsg).block();
            logger.info("【分析智能体】分析结果： {}", analyzeMsg.getTextContent());
            sink.tryEmitNext(analyzeMsg.getTextContent());

            Msg synthesizeMsg = synthesizerAgent.call(analyzeMsg).block();
            logger.info("【合成智能体】合成结果： {}", synthesizeMsg.getTextContent());
            sink.tryEmitNext(synthesizeMsg.getTextContent());
        }
    }


    private static void loadSession(SessionManager sessionManager, String sessionId) {
        if (sessionManager.sessionExists()) {
            // Load existing session
            sessionManager.loadIfExists();
        } else {
            logger.info("✓ New session created: {}\n", sessionId);
        }
    }

    public void processStream(Flux<Event> generator, Sinks.Many<String> sink) {
        generator
                .doOnNext(output -> logger.info("output = {}", output))
                .filter(event -> !event.isLast())
                .map(
                        event -> {
                            Msg msg = event.getMessage();
                            return msg.getContent().stream()
                                    .filter(block -> block instanceof TextBlock)
                                    .map(block -> ((TextBlock) block).getText())
                                    .toList();
                        })
                .flatMap(Flux::fromIterable)
                .map(content -> content)
                .doOnNext(sink::tryEmitNext)
                .doOnError(
                        e -> {
                            logger.error(
                                    "Unexpected error in stream processing: {}", e.getMessage(), e);
                            sink.tryEmitNext("System processing error, please try again later.");
                        })
                .doOnComplete(
                        () -> {
                            logger.info("Stream processing completed successfully");
                            sink.tryEmitComplete();
                        })
                .subscribe(
                        // onNext - already handled in doOnNext
                        null,
                        // onError
                        e -> {
                            logger.error("Stream processing failed: {}", e.getMessage(), e);
                            sink.tryEmitError(e);
                        });
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
