package cn.ikarts.crypto.entity;


import lombok.Data;

/**
 * @author shenhuan
 * @date 2025-12-31 10:51
 **/
@Data
public class McpConfig {

    private Long id;

    /**
     * mcp名称
     */
    private String mcpName;

    /**
     * mcp描述
     */
    private String mcpDesc;

    /**
     * mcp类型
     */
    private String mcpType;

    /**
     * mcp地址
     */
    private String mcpUrl;

    /**
     * mcp请求头
     */
    private String header;

    /**
     * mcp超时时间
     */
    private Integer timeout;

    /**
     * mcp启用状态：0-禁用 1-启用
     */
    private String status = "1";

}
