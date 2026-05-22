# 供应商协同系统 (Supplier Collaboration Platform)

## 项目简介
本项目是一套成熟的供应商协同（SRM）解决方案，旨在打通“采购方”与“供应方”之间的信息壁垒。系统对标“企业版淘宝+钉钉”，既覆盖采购全流程管控，又包含供应商自助协同与即时通讯。核心架构由**5大采购业务模块**、**2大协同门户**及**1套底层支撑体系**构成。

## 技术栈

### 前端技术
- **框架**: Vue 3.4+ (Composition API)
- **UI组件库**: Element Plus 2.4+
- **构建工具**: Vite 5.0+
- **状态管理**: Pinia
- **路由**: Vue Router 4
- **HTTP客户端**: Axios
- **工具库**: VueUse、Day.js、Lodash-es

### 后端技术
- **核心框架**: Spring Boot 3.2+
- **安全框架**: Spring Security + JWT + OAuth2 (支持多租户/多角色)
- **ORM框架**: MyBatis-Plus 3.5+
- **API文档**: Knife4j (Swagger 3)
- **参数校验**: Hibernate Validator

### 数据存储
- **关系型数据库**: MySQL 8.0+ (核心业务数据：订单、库存、财务)
- **缓存**: Redis 7.0+ (会话、字典、热点数据、分布式锁)
- **文档数据库**: MongoDB 7.0+ (操作日志、非结构化数据、8D报告附件)
- **搜索引擎**: Elasticsearch 8.11+ (供应商全文检索、物料模糊查询)

### 中间件
- **消息队列**: RabbitMQ 3.12+ (订单异步下发、状态回传、消息通知解耦)
- **文件存储**: MinIO (资质证书、合同、图纸、发票附件)
- **网关**: Spring Cloud Gateway (API路由与鉴权)

### 运维部署
- **容器化**: Docker + Docker Compose
- **CI/CD**: GitHub Actions / Jenkins
- **监控**: Prometheus + Grafana
- **日志**: ELK Stack

## 项目结构

```
supplier-collaboration/
├── backend/                    # 后端项目
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/supplier/
│   │   │   │       ├── common/          # 公共模块
│   │   │   │       ├── security/        # 安全与权限(IAM)
│   │   │   │       ├── sourcing/        # 战略寻源(RFQ/招投标)
│   │   │   │       ├── order/           # 采购订单协同(PO/PCO)
│   │   │   │       ├── logistics/       # 物流与交付(ASN/JIT)
│   │   │   │       ├── quality/         # 质量协同(NCR/8D)
│   │   │   │       ├── settlement/      # 财务结算(对账/发票)
│   │   │   │       ├── portal/          # 供应商门户
│   │   │   │       └── integration/     # 集成网关(ERP/EDI)
│   │   │   └── resources/
│   └── Dockerfile
│
├── frontend/                   # 前端项目
│   ├── src/
│   │   ├── views/
│   │   │   ├── purchasing/     # 采购方管理后台
│   │   │   │   ├── sourcing/
│   │   │   │   ├── order/
│   │   │   │   ├── quality/
│   │   │   │   └── finance/
│   │   │   ├── supplier/       # 供应商门户
│   │   │   │   ├── dashboard/
│   │   │   │   ├── orders/
│   │   │   │   └── forecast/
│   │   │   └── system/         # 系统管理
│   └── Dockerfile
│
├── database/                   # 数据库脚本
├── docker/                     # Docker配置
└── docs/                       # 项目文档
```

## 核心功能模块

### 一、 采购方核心业务模块（内部管控）
1.  **战略寻源与招投标**
    - 供应商准入与资质审核
    - RFQ询价与自动横向比价
    - 在线招投标与定标管理
2.  **采购订单协同**
    - ERP订单同步与一键下发
    - 订单变更管理 (PCO)
    - 交期反馈与延期风险看板
3.  **物流与交付协同**
    - ASN（提前发货通知）管理
    - 条码标签打印与扫码入库
    - JIT/VMI供应商寄售库存管理
4.  **质量协同**
    - IQC检验标准维护
    - 异常处理 (NCR) 与退货流程
    - 8D报告整改闭环
5.  **财务结算协同**
    - 三单匹配（订单/收货/发票）
    - 电子对账与发票OCR识别
    - 付款计划对接资金系统

### 二、 供应商门户模块（外部协同）
1.  **工作台 (Dashboard)**
    - 待办事项中心（报价/接单/异常）
    - 绩效雷达图（交付率/合格率）
2.  **订单与发货中心**
    - 在线接单/拒单与交期反馈
    - 发货管理与送货单打印
3.  **库存与排产**
    - 滚动需求预测 (Forecast)
    - VMI库存水位告警
4.  **沟通与申诉**
    - 基于单据的即时通讯/留言
    - 质量判定与扣款在线申诉

### 三、 底层支撑体系
1.  **权限与组织架构 (IAM)**
    - RBAC精细化权限控制
    - 多角色切换（一人多岗）
2.  **系统集成网关**
    - ERP双向同步 (SAP/Oracle/金蝶/用友)
    - EDI报文处理 (EDIFACT, ANSI X12)
3.  **消息中心**
    - 多渠道触达（站内信、邮件、短信、企微机器人）

## 快速开始

### 环境要求
- JDK 17+
- Node.js 18+
- MySQL 8.0+
- Redis 7.0+
- MongoDB 7.0+
- RabbitMQ 3.12+
- Elasticsearch 8.11+ (可选)
- Docker & Docker Compose (推荐)

### 本地开发
```bash
# 1. 克隆项目
git clone https://github.com/yourusername/supplier-collaboration
cd supplier-collaboration

# 2. 启动基础服务
docker-compose up -d

# 3. 启动后端
cd backend && mvn spring-boot:run

# 4. 启动前端
cd frontend && npm run dev
```

### 访问地址
- **前端门户**: http://localhost:5173
- **后端API**: http://localhost:8080
- **API文档**: http://localhost:8080/doc.html

### 默认账号
- **采购方管理员**: admin / admin123
- **供应商账号**: supplier001 / supplier123

## 开发规范
- **API规范**: RESTful + 统一返回格式 + `/api/v1/` 版本控制
- **提交规范**: Conventional Commits
- **分支策略**: Git Flow (master/develop/feature/hotfix)

## 许可证
MIT License

## 联系方式
- 项目地址: https://github.com/yourusername/supplier-collaboration
- 示例域名: your.email@example.com
