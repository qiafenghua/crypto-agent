package cn.ikarts.crypto.controller;

import cn.hutool.core.util.IdUtil;
import cn.ikarts.crypto.service.AgentService;
import jakarta.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * AI智能体对话入口
 *
 * @author shenhuan
 * @date 2025-12-30 11:18
 **/
@RestController
@RequestMapping("/agent")
public class AgentController {

    @Resource
    private AgentService agentService;

    @GetMapping(path = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(@RequestParam String message,
                             @RequestParam String sessionId) {
        return agentService.chat(message, sessionId);
    }

    @GetMapping("createSession")
    public String createSession() {
        return IdUtil.getSnowflake().nextIdStr();
    }


}
