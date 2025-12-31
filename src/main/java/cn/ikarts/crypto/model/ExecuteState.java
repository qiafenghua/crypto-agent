package cn.ikarts.crypto.model;

import lombok.Data;

/**
 * 智能体执行阶段，用于前端通知
 *
 * @author shenhuan
 * @date 2025-12-31 13:47
 **/
@Data
public class ExecuteState {

    /**
     * 状态
     */
    private String state;

    /**
     * 智能体名称
     */
    private String agentName;

    /**
     * 状态信息
     */
    private String message;


    private String step;

}
