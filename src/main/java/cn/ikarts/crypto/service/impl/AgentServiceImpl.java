package cn.ikarts.crypto.service.impl;

import cn.ikarts.crypto.agent.CryptoAgentManager;
import cn.ikarts.crypto.service.AgentService;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.message.TextBlock;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

/**
 * 智能体对话实现
 *
 * @author shenhuan
 * @date 2025-12-30 11:25
 **/
@Service
public class AgentServiceImpl implements AgentService {

    private static final Logger logger = LoggerFactory.getLogger(AgentServiceImpl.class);

    @Resource
    private CryptoAgentManager cryptoAgentManager;

    @Override
    public Flux<String> chat(String message, String sessionId) {

        //响应式发射器
        Sinks.Many<String> sink =
                Sinks.many().unicast().onBackpressureBuffer();

        cryptoAgentManager.run(message, sink, sessionId);

        return sink.asFlux()
                .doOnCancel(
                        () -> logger.info("Client disconnected from stream"))
                .doOnError(e -> logger.error("Error occurred during streaming", e));
    }


}
