# Debug Session: backend-startup

Status: [OPEN]

## Goal

启动供应商协同系统后端项目，收集启动失败证据，并基于运行时日志修复启动过程中的报错。

## Hypotheses

1. 默认 JDK 版本不是 Java 17，导致 Spring Boot 3 编译或启动失败。
2. dev 配置依赖 MySQL、Redis、MongoDB、RabbitMQ 等中间件，未启动或连接参数不匹配导致启动失败。
3. 新增实体字段与数据库表字段或 MyBatis-Plus 映射不一致，导致启动或运行时报错。
4. Spring Bean 注入存在循环依赖、缺失 Bean 或扫描路径问题，导致 ApplicationContext 启动失败。
5. 配置项、YAML 结构或安全配置存在冲突，导致配置绑定或 SecurityFilterChain 初始化失败。

## Evidence Log

- 首次启动失败时，Tomcat 报错 `Port 8080 was already in use`。
- 通过 `Get-NetTCPConnection -LocalPort 8080` 确认占用进程为 `PID 50532`。
- 终止占用进程后重新启动，Spring Boot 成功启动并监听 `http://localhost:8080/api`。

## Fix Log

- 停止了占用 8080 端口的旧进程。
- 重新启动后端，未修改业务代码。

## Verification

- 启动日志显示 `Tomcat started on port 8080 (http) with context path '/api'`。
- 启动日志显示 `Started SupplierCollaborationApplication in 3.816 seconds`。
