toolName: view_folder
            
status: success
          
d:\.我的项目\供应商协同\backend
          

        
            
toolName: search_by_regex
            
status: success
          
query: sys_audit_log
          

        
            
toolName: search_by_regex
            
status: success
          
query: AuditLog
          

        
            
toolName: view_files
            
status: success
          
            
filePath: d:\.我的项目\供应商协同\backend\src\main\java\com\supplier\system\entity\SysAuditLog.java
          

        
            
toolName: view_files
            
status: success
          
            
filePath: d:\.我的项目\供应商协同\backend\sql\V1.6__create_write_off_table.sql
          

        
            
toolName: search_by_regex
            
status: success
          
query: trigger|TRIGGER
          

        
            
toolName: view_folder
            
status: success
          
d:\.我的项目\供应商协同\backend\sql
          

        
            
toolName: view_files
            
status: success
          
            
filePath: d:\.我的项目\供应商协同\backend\src\main\java\com\supplier\common\aspect\AuditLogAspect.java
          

        
            
toolName: search_by_regex
            
status: success
          
query: sys_audit_log
          

        
            
toolName: search_by_regex
            
status: success
          
query: trigger|trigger_
          

        
            
toolName: search_by_regex
            
status: success
          
query: sys_audit_log
          

        
            
toolName: view_files
            
status: success
          
            
filePath: d:\.我的项目\供应商协同\backend\src\test\resources\sql\test-schema.sql
          

        
            
toolName: view_files
            
status: success
          
            
filePath: d:\.我的项目\供应商协同\backend\sql\数据库对接修复汇总_第五部分_系统消息门户集成公共模块.md
          

        
            
toolName: AskUserQuestion
            
status: success
          
          
让我先全面检查项目里现有的实现，再判断触发器该如何补齐。
        
            
toolName: view_files
            
status: success
          
            
filePath: d:\.我的项目\供应商协同\backend\src\main\java\com\supplier\system\service\impl\SysAuditLogServiceImpl.java
          

        
            
toolName: view_files
            
status: success
          
            
filePath: d:\.我的项目\供应商协同\backend\src\main\java\com\supplier\system\controller\SysAuditLogController.java
          

        
            
toolName: view_files
            
status: success
          
            
filePath: d:\.我的项目\供应商协同\backend\src\main\java\com\supplier\system\mapper\SysAuditLogMapper.java
          

        
            
toolName: view_files
            
status: success
          
            
filePath: d:\.我的项目\供应商协同\backend\src\main\java\com\supplier\system\query\SysAuditLogQuery.java
          

        
            
toolName: view_files
            
status: success
          
            
filePath: d:\.我的项目\供应商协同\backend\src\main\java\com\supplier\common\annotation\AuditLog.java
          

        
            
toolName: view_files
            
status: success
          
            
filePath: d:\.我的项目\供应商协同\backend\src\main\java\com\supplier\common\entity\BizStatusTrack.java
          

        
            
toolName: view_files
            
status: success
          
            
filePath: d:\.我的项目\供应商协同\backend\src\main\java\com\supplier\portal\entity\PortalTodo.java
          

        
            
toolName: view_files
            
status: success
          
            
filePath: d:\.我的项目\供应商协同\backend\src\main\java\com\supplier\common\service\impl\BizStatusTrackServiceImpl.java
          

        
            
toolName: view_files
            
status: success
          
            
filePath: d:\.我的项目\供应商协同\backend\src\main\java\com\supplier\common\service\BizStatusTrackService.java
          

        
            
toolName: view_files
            
status: success
          
            
filePath: d:\.我的项目\供应商协同\backend\src\main\java\com\supplier\common\entity\BaseEntity.java
          

        
            
toolName: search_by_regex
            
status: success
          
query: sys_audit_log
          

        
            
toolName: view_files
            
status: success
          
            
filePath: d:\.我的项目\供应商协同\backend\src\main\java\com\supplier\common\event\DomainEventPublisher.java
          

        
            
toolName: view_files
            
status: success
          
            
filePath: d:\.我的项目\供应商协同\backend\src\main\java\com\supplier\common\event\DomainEvent.java
          

        
            
toolName: view_files
            
status: success
          
            
filePath: d:\.我的项目\供应商协同\backend\sql\V1.4__create_missing_tables.sql
          

        
            
toolName: search_by_regex
            
status: success
          
query: sys_audit_log
          

        
            
toolName: search_by_regex
            
status: success
          
query: sys_audit_log
          

        
            
toolName: view_files
            
status: success
          
            
filePath: d:\.我的项目\供应商协同\backend\src\main\java\com\supplier\portal\task\RiskWarningSchedulerService.java
          

        
            
toolName: view_files
            
status: success
          
            
filePath: d:\.我的项目\供应商协同\backend\src\main\java\com\supplier\common\config\RabbitMQConfig.java
          

        
            
toolName: view_files
            
status: success
          
            
filePath: d:\.我的项目\供应商协同\backend\src\main\java\com\supplier\common\event\RabbitDomainEventPublisher.java
          

        
            
toolName: search_by_regex
            
status: success
          
query: writeTrack|bizStatusTrackService|BizStatusTrackService
          
经过排查，结论如下：

## 一、sys_audit_log 现状

- **Java 实体**：[SysAuditLog.java](file:///d:/.我的项目/供应商协同/backend/src/main/java/com/supplier/system/entity/SysAuditLog.java) 字段齐整（`businessId`、`businessNo` 已存在）
- **Mapper / Service / Controller**：[SysAuditLogMapper.java](file:///d:/.我的项目/供应商协同/backend/src/main/java/com/supplier/system/mapper/SysAuditLogMapper.java)、[SysAuditLogServiceImpl.java](file:///d:/.我的项目/供应商协同/backend/src/main/java/com/supplier/system/service/impl/SysAuditLogServiceImpl.java)、[SysAuditLogController.java](file:///d:/.我的项目/供应商协同/backend/src/main/java/com/supplier/system/controller/SysAuditLogController.java) 仅提供**分页查询 + 详情**
- **建表脚本**：在 `V1.1~V1.6` 迁移脚本里**找不到** `sys_audit_log` 的 CREATE TABLE，只在 `test-schema.sql` 和 [数据库对接修复汇总_第五部分](file:///d:/.我的项目/供应商协同/backend/sql/数据库对接修复汇总_第五部分_系统消息门户集成公共模块.md) 中提及（"完全匹配，无问题"）

## 二、审计日志的写入路径

1. **注解声明**：[AuditLog.java](file:///d:/.我的项目/供应商协同/backend/src/main/java/com/supplier/common/annotation/AuditLog.java)（module / businessType / action / businessIdExpr / businessNoExpr / beforeStatusExpr / afterStatusExpr）
2. **AOP 织入**：[AuditLogAspect.java](file:///d:/.我的项目/供应商协同/backend/src/main/java/com/supplier/common/aspect/AuditLogAspect.java)
   - `@AfterReturning` 写成功日志（`resultStatus=1`）
   - `@AfterThrowing` 写异常日志（`resultStatus=0`）
   - 通过 SpEL 解析 `businessIdExpr` / `businessNoExpr` → 写入 `sys_audit_log`

**但写完就结束了**——`sys_audit_log` 没有任何后续消费：

| 应有联动 | 当前实现 |
|---------|---------|
| 异常 (`resultStatus=0`) 告警 | ❌ 无 |
| 失败/失败工单 → 通知 | ❌ 无 |
| `business_id` + `business_no` 反查业务单 → 自动联动 portal_todo / biz_status_track | ❌ 无 |
| MySQL 触发器 | ❌ 全工程无任何 `TRIGGER` |
| 定时扫描异常审计 | ❌ 无 |
| 通过 MQ 事件广播 | ❌ 无（DomainEventPublisher 已有，但 audit log 路径未接入） |

## 三、和"实时监控"相关的现有设施

- [RiskWarningSchedulerService.java](file:///d:/.我的项目/供应商协同/backend/src/main/java/com/supplier/portal/task/RiskWarningSchedulerService.java) — 定时扫描订单/送货逾期，但**不读 sys_audit_log**
- [BizStatusTrackServiceImpl.java](file:///d:/.我的项目/供应商协同/backend/src/main/java/com/supplier/common/service/impl/BizStatusTrackServiceImpl.java) — 业务状态轨迹（已实现，但各业务是手动调用 `writeTrack`，并非由 sys_audit_log 触发）
- [RabbitMQConfig.java](file:///d:/.我的项目/供应商协同/backend/src/main/java/com/supplier/common/config/RabbitMQConfig.java) — 已有 `supplier.notice.exchange` 等，但 audit log 路径未消费任何队列

## 四、结论

"实时监控触发器没有设计好"这个判断是**正确的**——`sys_audit_log` 实质上是一个**写完即弃的日志表**，`business_id` / `business_no` 这两个字段存了但没有任何下游消费。系统目前完全依赖 AOP 写入，没有 DB 触发器、没有事件发布、没有告警通道。

## 五、需要你确认的修复方向

请选择要补哪一层（可多选，但同一处最好只选一种主链路）：

| # | 方案 | 落地点 |
|---|------|--------|
| A | **AOP 层增加"异常即告警"** | 扩展 `AuditLogAspect`：`resultStatus=0` 时发布 `audit.failed` 事件 → 通知中心 |
| B | **AOP 层增加"审计即发 MQ"** | 扩展 `AuditLogAspect`：每次成功也发 `audit.recorded` 事件，下游 portal_todo / 大数据等订阅 |
| C | **BizStatusTrack 自动联动** | `AuditLogAspect` 写入时，若 `beforeStatus`/`afterStatus` 都不为空，自动调 `bizStatusTrackService.writeTrack(...)` |
| D | **DB 触发器（MySQL TRIGGER）** | 新建 `V1.7__create_audit_log_trigger.sql`，在 `sys_audit_log` 上挂 `AFTER INSERT` 触发器，写入监控表或发 MQ 客户端调用 |
| E | **定时扫描异常审计** | 新建 `AuditLogMonitorScheduler`，每分钟扫 `result_status=0 AND error_message LIKE '%关键模块%'` |
| F | **建表脚本补齐** | 新建 `V1.7__create_audit_log_table.sql` 把 `sys_audit_log` 的正式 DDL 补上（含 `business_id` 索引） |

请告诉我优先做哪几项（推荐 **A + F**，最少改动拿到"实时告警"价值），我会直接落地代码。