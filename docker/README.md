# Docker 部署指南

## 目录结构

```
docker/
├── docker-compose.yml          # 开发环境配置
├── docker-compose.prod.yml     # 生产环境配置
├── .env                        # 环境变量配置
├── nginx/
│   └── nginx.conf              # Nginx 主配置文件
├── mysql/
│   └── conf.d/
│       └── my.cnf              # MySQL 配置文件
├── redis/
│   └── redis.conf              # Redis 配置文件
├── mongodb/
│   └── mongod.conf             # MongoDB 配置文件
├── elasticsearch/
│   └── elasticsearch.yml       # Elasticsearch 配置文件
├── rabbitmq/
│   └── enabled_plugins         # RabbitMQ 插件配置
└── minio/
    └── config.json             # MinIO 配置文件
```

## 快速开始

### 开发环境

```bash
# 进入 docker 目录
cd docker

# 启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f

# 停止所有服务
docker-compose down

# 停止并删除数据卷
docker-compose down -v
```

### 生产环境

```bash
# 启动所有服务
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d

# 查看服务状态
docker-compose -f docker-compose.yml -f docker-compose.prod.yml ps

# 停止所有服务
docker-compose -f docker-compose.yml -f docker-compose.prod.yml down
```

## 服务说明

| 服务 | 端口 | 说明 |
|------|------|------|
| MySQL | 3306 | 关系型数据库 |
| Redis | 6379 | 缓存和消息队列 |
| MongoDB | 27017 | 文档数据库 |
| RabbitMQ | 5672/15672 | 消息队列 (管理界面端口15672) |
| Elasticsearch | 9200 | 搜索引擎 |
| MinIO | 9000/9001 | 对象存储 (控制台端口9001) |
| Nginx | 80/443 | 反向代理 (仅生产环境) |

## 默认账号密码

| 服务 | 用户名 | 密码 |
|------|--------|------|
| MySQL | root | root123456 |
| Redis | - | redis123456 |
| MongoDB | admin | mongo123456 |
| RabbitMQ | guest | guest |
| MinIO | minioadmin | minioadmin123 |

## 健康检查

所有服务都配置了健康检查,可以通过以下命令查看:

```bash
docker-compose ps
```

## 数据持久化

所有服务的数据都存储在 Docker 数据卷中:

```bash
# 查看数据卷
docker volume ls | grep supplier

# 查看数据卷详情
docker volume inspect supplier-mysql-data
```

## 注意事项

1. 生产环境请修改 `.env` 文件中的默认密码
2. 生产环境建议启用 HTTPS,需要配置 SSL 证书
3. 根据实际服务器配置调整内存和 CPU 限制
4. 定期备份数据卷数据
