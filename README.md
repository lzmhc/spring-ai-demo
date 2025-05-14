## Ollama-Chat Server

## 基于大语言模型的在线教育自动化辅导系统

#### 介绍

基于SpringAi的Ollama-Chat后端项目，使用Ollama部署模型

此项目是我的毕设题目《基于大模型的在线教育学习平台辅导系统》

此仓库只是目前的半成品，能力有限，开发难度较大，仅供参考学习

如有问题可联系：

QQ：2910463910

微信：mhc2910463910

邮箱：lzmhc.top@foxmail.com

#### 部署运行
```
# JDK17 //springai最低版本17
# spring 3.x.x //最低版本
# ollama //需要本地运行ollama
```
#### 项目功能
[y] AiChat聊天对话
[y] 知识库文本向量化 
[y] RAG知识库问答
[y] 多模态对话
[y] MCP

#### 用户认证
- SaToken框架

> 如果不需要登陆认真，移除satoken相关的依赖及config类

#### 文件存储
- S3 Minio存储

  > 需要自行部署minio服务器，推荐使用docker部署，然后在yml中配置

#### ollama api
- /v1/models 获取所有模型列表

#### 前端项目


#### 跨域解决
> config/SaTokenConfigure.java
```
   SaHolder.getResponse()
       // ---------- 设置跨域响应头 ----------
       // 允许指定域访问跨域资源
       .setHeader("Access-Control-Allow-Origin", "*")
       // 允许所有请求方式
       .setHeader("Access-Control-Allow-Methods", "*")
       // 允许的header参数
       .setHeader("Access-Control-Allow-Headers", "*")
       // 有效时间
       .setHeader("Access-Control-Max-Age", "3600")
       ;
       // 如果是预检请求，则立即返回到前端
       SaRouter.match(SaHttpMethod.OPTIONS)
       .free(r -> System.out.println("--------OPTIONS预检请求，不做处理"))
       .back();
```
#### api登陆验证
> config/SaTokenConfigure.java
```
 return new SaReactorFilter()
   // 指定 [拦截路由]
   .addInclude("/**")    /* 拦截所有path */
   // 指定 [放行路由]
   .addExclude("/static/**", "/oauth2/qq/login", "/connection")
   // 指定[认证函数]: 每次请求执行
   .setAuth(obj -> {
        System.out.println("---------- sa全局认证");
   //     SaRouter.match("/**", () -> StpUtil.checkLogin());
   });
```
#### 数据库 Mysql + PgSql
1. dump-Qingjin-xxxxxxx.sql mysql表结构，部分表从SmartAdmin导入
2. pgsql.sql vector_store表存储向量化文本

#### MCP服务扩展
- com.lzmhc.mcpserver 存放mcpService
#### MCP启用
- com.lzmhc.service.AiChatService
```
# 添加.defaultTools(asyncMcpToolCallbackProvider.getToolCallbacks()) 
# 示例 
ChatClient.builder(chatModel).defaultTools(asyncMcpToolCallbackProvider.getToolCallbacks()).build();
```

#### 致谢

[Spring AI+Ollama+pgvector实现本地RAG](https://github.com/jianyuan1991/ragdemo)

[Sa-Token](https://github.com/dromara/Sa-Token)

[know-hub-ai](https://github.com/NingNing0111/know-hub-ai)

[ollama](https://github.com/ollama/ollama)

[dynamic-datasource](https://github.com/baomidou/dynamic-datasource)

[ragdemo](https://github.com/jianyuan1991/ragdemo)