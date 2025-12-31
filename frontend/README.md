# Crypto Agent Frontend

基于 Vue3 的智能体对话可视化界面。

## 功能特性

- 全屏对话界面，发送消息后自动收窄
- 右侧实时显示智能体执行状态
- 支持 Markdown 渲染
- SSE 流式响应

## 运行

```bash
cd frontend
npm install
npm run dev
```

前端运行在 http://localhost:3000，会自动代理 `/api` 请求到后端 8080 端口。

## 构建

```bash
npm run build
```

构建产物在 `dist` 目录。
