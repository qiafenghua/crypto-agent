//package cn.ikarts.crypto.agent;
//
//import cn.ikarts.crypto.memory.CustomMemory;
//import cn.ikarts.crypto.utils.ModelHelper;
//import io.agentscope.core.ReActAgent;
//import io.agentscope.core.memory.InMemoryMemory;
//import io.agentscope.core.memory.autocontext.AutoContextHook;
//import io.agentscope.core.message.Msg;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Component;
//import reactor.core.publisher.Mono;
//
///**
// * 整合所有信息，生成全面参考报告，确保逻辑连贯和来源引用。
// *
// * @author shenhuan
// * @date 2025-12-30 09:52
// **/
//public class SynthesizerAgent {
//    private static final Logger logger = LoggerFactory.getLogger(SynthesizerAgent.class);
//
//    private final ReActAgent agent;
//
//    private final CustomMemory memory;
//
//    public SynthesizerAgent() {
//        memory = new CustomMemory();
//
//        agent = ReActAgent.builder()
//                .name("SynthesizerAgent")
//                .sysPrompt("你是一个整合所有信息，生成全面参考报告，确保逻辑连贯和来源引用的代理")
//                .model(ModelHelper.getLanguageModel())
//                .hook(new AutoContextHook())
//                .memory(new InMemoryMemory())
//                .enablePlan()
//                .build();
//    }
//
//    public Mono<Msg> synthesize(Msg task) {
//        logger.info("【SynthesizerAgent】：收到用户请求信息：{}，开始整合信息生成报告！", task);
//        return agent.call(task);
//    }
//
//}
