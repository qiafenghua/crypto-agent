package cn.ikarts.crypto.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import cn.ikarts.crypto.agent.CryptoAgentManager;
import cn.ikarts.crypto.service.AgentService;
import jakarta.annotation.Resource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

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

        // 异步执行智能体处理，避免阻塞
        Mono.fromRunnable(() -> cryptoAgentManager.run(message, sink, sessionId))
            .subscribeOn(Schedulers.boundedElastic())
            .doOnError(error -> {
                logger.error("智能体处理出错: {}", error.getMessage(), error);
                sink.tryEmitError(error);
            })
            .subscribe();

        return sink.asFlux()
                .doOnCancel(() -> {
                    logger.info("Client disconnected from stream for session: {}", sessionId);
                    sink.tryEmitComplete();
                })
                .doOnError(e -> logger.error("Error occurred during streaming for session {}: {}", sessionId, e.getMessage(), e))
                .onErrorResume(e -> {
                    // 发送错误信息给客户端而不是直接终止流
                    return Flux.just("❌ 处理过程中出现错误，请稍后重试");
                });
    }


}
