package cn.ikarts.crypto.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.IOException;

public class RootDataTool {
    private static final Logger logger = LoggerFactory.getLogger(RootDataTool.class);
    private static final String BASE_URL = "https://api.rootdata.com/open";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private final String apiKey;

    public RootDataTool() {
        this.apiKey = "gBP2Z8AeNRdFa8N1u8vJ0tXsSGsAMnzp";
        if (!StringUtils.hasText(apiKey)) {
            throw new IllegalStateException("请设置环境变量 ROOTDATA_API_KEY 以使用 RootData 工具（从 https://www.rootdata.com/Api 申请）");
        }
    }

    /**
     * 搜索加密货币/Web3 项目、VC 或人物
     */
    @Tool(name = "rootdata_search_projects",
            description = """
                    使用关键词搜索 RootData 中的加密货币/Web3 项目、投资机构或人物。
                    返回项目列表，包括 project_id、名称、简介、logo 和 RootData 链接。
                    必须先使用此工具查找项目 ID，然后再调用 get_project_detail 获取详细信息。
                    """)
    public String searchProjects(
            @ToolParam(name = "query", description = "搜索关键词，如项目名称 'Bitcoin'、符号 'BTC'、代币名或公司名") String query,
            @ToolParam(name = "precise_x_search", description = "是否启用精确 X (Twitter) 句柄搜索", required = false) Boolean preciseXSearch
    ) throws IOException {
        String jsonBody = "{\"query\": \"" + query + "\"";
        if (preciseXSearch != null && preciseXSearch) {
            jsonBody += ", \"precise_x_search\": true";
        }
        jsonBody += "}";

        Request request = new Request.Builder()
                .url(BASE_URL + "/ser_inv")
                .post(RequestBody.create(jsonBody, JSON))
                .header("apikey", apiKey)
                .header("language", "en")
                .header("Content-Type", "application/json")
                .build();

        return executeRequest(request);
    }

    /**
     * 获取指定项目的详细信息（RootData 最强大工具）
     */
    @Tool(name = "rootdata_get_project_detail",
            description = """
                    根据 project_id 或 contract_address 获取加密货币/Web3 项目的完整信息。
                    包括：项目简介、一句话概述、融资总额、市场数据（市值、价格、FDV）、团队、投资者、标签、社交链接、合约地址、支持交易所、生态等。
                    这是 RootData 最核心的数据接口，强烈推荐在确认 project_id 后使用。
                    """)
    public String getProjectDetail(
            @ToolParam(name = "project_id", description = "项目 ID（从 search_projects 获取），与 contract_address 二选一", required = false) Integer projectId,
            @ToolParam(name = "contract_address", description = "智能合约地址（支持直接查询），与 project_id 二选一", required = false) String contractAddress,
            @ToolParam(name = "include_team", description = "是否包含团队成员信息", required = false) Boolean includeTeam,
            @ToolParam(name = "include_investors", description = "是否包含投资者信息", required = false) Boolean includeInvestors
    ) throws IOException {
        if ((projectId == null && contractAddress == null) || (projectId != null && contractAddress != null)) {
            return "错误：必须且仅提供 project_id 或 contract_address 中的一个";
        }

        String jsonBody = projectId != null
                ? "{\"project_id\": " + projectId
                : "{\"contract_address\": \"" + contractAddress + "\"";

        if (includeTeam != null && includeTeam) jsonBody += ", \"include_team\": true";
        if (includeInvestors != null && includeInvestors) jsonBody += ", \"include_investors\": true";
        jsonBody += "}";

        Request request = new Request.Builder()
                .url(BASE_URL + "/get_item")
                .post(RequestBody.create(jsonBody, JSON))
                .header("apikey", apiKey)
                .header("language", "en")
                .header("Content-Type", "application/json")
                .build();

        return executeRequest(request);
    }

    private String executeRequest(Request request) throws IOException {
        logger.info("请求RootData API，URL地址:{}", request);
        try (Response response = client.newCall(request).execute()) {
            String body = response.body().string();
            if (!response.isSuccessful()) {
                logger.error("[RootDataTool] RootData API 请求失败,失败原因：{}", response.message());
                return "RootData API 请求失败,失败原因：" + response.message();
            }
            logger.info("[RootDataTool] 请求RootData API成功，响应结果: {}", body);
            // 美化 JSON 输出
            Object json = mapper.readValue(body, Object.class);
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
        }
    }
}