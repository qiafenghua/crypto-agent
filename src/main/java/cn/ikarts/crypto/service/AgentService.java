package cn.ikarts.crypto.service;

import reactor.core.publisher.Flux;

/**
 * @author shenhuan
 * @date 2025-12-30 11:24
 **/
public interface AgentService {
    Flux<String> chat(String message, String sessionId);
}
