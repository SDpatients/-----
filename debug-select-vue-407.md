# Debug Session: select-vue-407
- **Status**: [FIXED]
- **Issue**: `SupplierDeliveryCenterView.vue:126` 模板引用了未定义的 `formatQuantity`，导致 `<el-table-column>` 渲染失败，Element Plus 上抛 `select.vue:407 Unhandled error during execution of component update`（连锁错误）
- **Root Cause**: `frontend/src/views/supplier/SupplierDeliveryCenterView.vue:126` 调用 `formatQuantity(row.quantity)`，但 `<script setup>` 中无该函数
- **Fix**: 在 `<script setup>` 中新增本地 `formatQuantity` 函数（处理 null/undefined/非数字，使用 `toLocaleString('zh-CN')` 千分位格式化）
- **Component Stack**: `<RouterView> <ElMain> <ElContainer> <ElContainer> <SupplierLayout> <RouterView>`
- **Element Plus Version**: 2.9.3
- **Debug Server**: 未启动（根因由堆栈 + 源码静态分析直接定位，未走运行时取证）
- **Log File**: 无

## Reproduction Steps
1. 登录供应商侧账号
2. 访问 `http://localhost:5173/supplier/deliveries`
3. 后端返回发货列表后，el-table 渲染"发货数量"列触发 `formatQuantity` 调用 → TypeError → Element Plus 渲染函数级联抛错（`select.vue:407` 等多条噪音错误）

## Hypotheses & Verification
| ID | Hypothesis | Likelihood | Effort | Evidence |
|----|------------|------------|--------|----------|
| A | el-select v-model 类型不匹配 / multiple 与非数组混用 | — | — | Rejected（错误路径不含 el-select 渲染） |
| B | el-option :value 类型不匹配 | — | — | Rejected（同上） |
| C | 选项源为 null/undefined | — | — | Rejected（同上） |
| D | 子组件 modelValue 异常 | — | — | Rejected（堆栈直达 `SupplierDeliveryCenterView`） |
| E | **模板引用了未在 setup 中暴露的函数 `formatQuantity`** | **Confirmed** | Low | `SupplierDeliveryCenterView.vue:126` 引用 `formatQuantity`；脚本部分无此函数定义；TypeError 直接指向 `_ctx.formatQuantity is not a function` |

> 注：原 A–D 是基于首条 `select.vue:407` 噪音报错提出的假设；用户提供完整堆栈后被直接证伪/覆盖。

## Log Evidence
未走运行时取证。证据来源：
- 用户提供的浏览器控制台堆栈：`SupplierDeliveryCenterView.vue:126:14` `_ctx.formatQuantity is not a function`
- 源码比对：`grep formatQuantity frontend/src` 仅此一处引用，无定义

## Verification Conclusion
- Pre-fix：`<el-table-column>` renderCell → `formatQuantity` 抛 TypeError → el-table 渲染中断 → Element Plus 上抛 `select.vue:407` 级联错误（select 内部因父组件 update 中断而失败）
- Post-fix：新增 `formatQuantity` 局部函数；TypeScript 类型检查通过（`vue-tsc --noEmit` 对该文件无新报错）；预期表格可正常渲染"发货数量"列

## Cleanup
无需清理 instrumentation。本次未启动 Debug Server，无日志文件需删除。`debug-select-vue-407.md` 留作记录。
