# crypto-agent

基于 AgentScope 与 Spring Boot 构建的加密货币研究 Agent 服务。项目通过多智能体协作方式，对用户输入的币种或 Web3 项目进行资料收集、信息整合与分析输出，并通过 SSE 流式返回结果。

## 项目概述

当前项目围绕 3 个核心 Agent 组织执行流程：

- `CoordinatorAgent`：负责拆解用户问题、规划研究任务。
- `ResearcherAgent`：负责调用链上/行情/社媒/网页工具收集事实信息。
- `SynthesizerAgent`：负责汇总研究结果并输出最终 Markdown 报告。

整体调用入口为 Spring Boot HTTP 接口，底层通过 AgentScope `ReActAgent`、`MsgHub`、`Toolkit` 和 MCP Client 组织执行链路。

## 主要能力

- 支持对加密货币或 Web3 项目发起研究型问答
- 支持 SSE 流式输出 Agent 执行过程和最终结果
- 支持 CoinGecko、RootData、X(Twitter) 检索等外部数据源
- 支持通过 Tavily MCP 进行网页搜索
- 支持接入 AgentScope Studio 观察 Agent 运行过程
- 支持通过图表 MCP 服务生成图表相关能力

## 技术栈

- Java 17
- Spring Boot 4.0.1
- AgentScope 1.0.4
- Reactor
- OkHttp
- Jackson
- Hutool
- Maven

## 执行流程

1. 客户端调用 `/agent/chat` 并传入 `message`、`sessionId`
2. `AgentController` 将请求转发给 `AgentService`
3. `CryptoAgentManager` 创建用户消息与系统公告消息
4. `CoordinatorAgent` 先生成任务规划
5. `ResearcherAgent` 基于规划调用工具收集数据
6. `SynthesizerAgent` 汇总内容并生成最终报告
7. 服务端通过 SSE 持续向前端推送中间过程和最终结果

## 内置工具与外部依赖

项目当前已接入以下工具或服务：

- `CoingeckoTool`
  - 搜索币种
  - 获取币种详情
  - 获取历史价格曲线
- `RootDataTool`
  - 搜索项目
  - 获取项目详情、团队、投资方等信息
- `XSearchTool`
  - 检索 X/Twitter 内容及互动数据
- `tavily-mcp`
  - 通过 `npx -y tavily-mcp` 以 stdio 方式启动
- `mcp-server-chart`
  - 通过远程 SSE 接口提供图表能力
- AgentScope Studio
  - 默认连接 `http://localhost:3000`

## 项目结构

```text
src/main/java/cn/ikarts/crypto
├─ agent           Agent 编排与执行入口
├─ config          Agent、Studio 与 Spring 配置
├─ constants       Prompt 常量
├─ controller      HTTP 接口
├─ hook            Agent 监控 Hook
├─ service         业务服务层
├─ tools           外部数据工具封装
├─ utils           模型、Prompt、消息处理工具
└─ model/entity    模型与配置对象
```

关键文件：

- `src/main/java/cn/ikarts/crypto/controller/AgentController.java`
- `src/main/java/cn/ikarts/crypto/agent/CryptoAgentManager.java`
- `src/main/java/cn/ikarts/crypto/config/AgentConfig.java`
- `src/main/java/cn/ikarts/crypto/utils/ModelHelper.java`
- `src/main/resources/application.yml`

## 运行前准备

### 1. 基础环境

- 安装 JDK 17
- 安装 Maven
- 安装 Node.js，并确保本机可执行 `npx`
- 本机或可访问环境中启动 AgentScope Studio，默认地址为 `http://localhost:3000`

### 2. 外部服务与 API Key

当前代码中部分密钥直接写在源码里，运行前至少需要确认这些配置可用：

- DashScope 模型 Key：`src/main/java/cn/ikarts/crypto/utils/ModelHelper.java`
- Tavily Key：`src/main/java/cn/ikarts/crypto/config/AgentConfig.java`
- CoinGecko Key：`src/main/java/cn/ikarts/crypto/tools/CoingeckoTool.java`
- DeSearch Key：`src/main/java/cn/ikarts/crypto/tools/XSearchTool.java`
- RootData Key：`src/main/java/cn/ikarts/crypto/tools/RootDataTool.java`

建议改造成环境变量或配置文件注入，避免直接提交到仓库。

### 3. 应用配置

默认配置见 `src/main/resources/application.yml`：

```yaml
spring:
  application:
    name: crypto-agent
server:
  port: 9090
```

## 启动方式

### 方式一：IDE 直接启动

启动主类：

```java
cn.ikarts.crypto.CryptoAgentApplication
```

### 方式二：Maven 启动

```bash
mvn spring-boot:run
```

## 接口说明

### 1. 创建会话

```http
GET /agent/createSession
```

返回值为雪花算法生成的会话 ID。

示例：

```bash
curl "http://localhost:9090/agent/createSession"
```

### 2. 发起对话

```http
GET /agent/chat?message=xxx&sessionId=xxx
Accept: text/event-stream
```

参数说明：

- `message`：用户提问内容
- `sessionId`：会话 ID

示例：

```bash
curl -N "http://localhost:9090/agent/chat?message=请分析Solana近期表现&sessionId=123456789"
```

返回形式为 `text/event-stream`，服务会逐步推送：

- 任务规划
- 数据研究结果
- 最终综合报告

## 测试与示例

测试目录下包含一些独立示例：

- `StudioExample.java`：AgentScope Studio 集成示例
- `McpTest.java`：图表 MCP 服务连接示例
- `CoingeckoTest.java`：CoinGecko 调用示例
- `TokenHolderConcentration.java`：代币持仓集中度相关测试代码

## 当前实现特点

- 结果输出以 Markdown 文本为主，适合前端直接渲染
- `AgentServiceImpl` 使用 Reactor `Sinks.Many` 实现流式响应
- `MonitoringHook` 会输出 Agent 调用与工具执行过程，方便排查问题
- Prompt 已按协调、研究、综合三个角色拆分

## 已知注意事项

### 1. Studio 依赖较强

`AgentConfig` 在 `@PostConstruct` 中会初始化 AgentScope Studio，默认连接 `http://localhost:3000`。如果该服务不可用，应用启动可能受影响。

### 2. 会话持久化暂未真正启用

虽然接口要求传入 `sessionId`，也提供了 `/agent/createSession`，但 `CryptoAgentManager` 中与 `SessionManager` 相关的持久化逻辑目前仍处于注释状态，因此当前更接近“请求级会话标识”，不是完整的持久上下文记忆。

### 3. API Key 仍为源码硬编码

这会带来安全和环境迁移问题，建议尽快改为配置中心、环境变量或 `application.yml` 注入。

### 4. Maven 构建环境需要再校验

本地尝试执行：

```bash
mvn -q -DskipTests compile
```

未完成验证，当前报错为 Maven 无法解析父 POM，且项目 `pom.xml` 同时声明了：

- `java.version=17`
- `maven-compiler-plugin source/target=8`

建议统一 Java 编译版本后再进行完整构建验证。

## 后续建议

- 将所有密钥迁移为环境变量配置
- 补全会话持久化能力
- 增加统一异常处理与接口返回规范
- 将 Agent 输出结构化，便于前端拆分展示
- 增加启动脚本和 `.env.example`
- 为 README 增加前端联调示例或页面截图

## License

当前仓库未声明 License，如需开源发布，请补充许可证文件。
