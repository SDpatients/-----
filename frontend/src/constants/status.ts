export type TagType = 'success' | 'warning' | 'danger' | 'info' | 'primary'

export const statusMap: Record<string, { label: string; type: TagType }> = {
  // 供应商准入状态（与后端 SupplierStatusEnum 对齐）
  '0': { label: '待审核', type: 'warning' },
  '1': { label: '初审中', type: 'primary' },
  '2': { label: '复审中', type: 'primary' },
  '3': { label: '终审中', type: 'primary' },
  '4': { label: '审核通过', type: 'success' },
  '5': { label: '审核驳回', type: 'danger' },
  '6': { label: '已禁用', type: 'info' },
  // 旧版兼容别名
  '合作中': { label: '合作中', type: 'success' },
  '暂停合作': { label: '暂停合作', type: 'warning' },
  '黑名单': { label: '黑名单', type: 'danger' },
  // 订单状态
  '订单0': { label: '草稿', type: 'info' },
  '订单1': { label: '待确认', type: 'warning' },
  '订单2': { label: '已确认', type: 'success' },
  '订单7': { label: '已取消', type: 'info' },
  '订单8': { label: '已拒单', type: 'danger' },
  // 送货状态
  '送货0': { label: '待发货', type: 'warning' },
  '送货1': { label: '已发货', type: 'primary' },
  '送货2': { label: '运输中', type: 'primary' },
  '送货3': { label: '已送达', type: 'success' },
  '送货4': { label: '已收货', type: 'success' },
  '送货5': { label: '已拒收', type: 'danger' },
  '送货6': { label: '质检中', type: 'warning' },
  // 质检结果
  '质检0': { label: '待检验', type: 'warning' },
  '质检1': { label: '合格', type: 'success' },
  '质检2': { label: '不合格', type: 'danger' },
  '质检3': { label: '部分合格', type: 'primary' },
  // 对账状态
  '对账0': { label: '待对账', type: 'warning' },
  '对账1': { label: '对账中', type: 'primary' },
  '对账2': { label: '已确认', type: 'success' },
  '对账3': { label: '有异议', type: 'danger' },
  '对账4': { label: '已完成', type: 'success' },
  // 发票（旧版兼容，新版本见下方）
  '付款旧0': { label: '未付款', type: 'warning' },
  '付款旧1': { label: '已付款', type: 'success' },
  // 旧式字符串值（兼容）
  active: { label: '合作中', type: 'success' },
  pending: { label: '待审核', type: 'warning' },
  frozen: { label: '已冻结', type: 'danger' },
  draft: { label: '草稿', type: 'info' },
  confirmed: { label: '已确认', type: 'success' },
  changed: { label: '变更中', type: 'warning' },
  shipped: { label: '已发货', type: 'primary' },
  received: { label: '已收货', type: 'success' },
  exception: { label: '异常', type: 'danger' },
  processing: { label: '处理中', type: 'warning' },
  closed: { label: '已关闭', type: 'info' },
  reconciled: { label: '已对账', type: 'success' },
  invoiced: { label: '已开票', type: 'primary' },
  paid: { label: '已付款', type: 'success' },
  // NCR 状态
  'NCR0': { label: '草稿', type: 'info' },
  'NCR1': { label: '已发布', type: 'warning' },
  'NCR2': { label: '处理中', type: 'primary' },
  'NCR3': { label: '待验证', type: 'warning' },
  'NCR4': { label: '已关闭', type: 'success' },
  'NCR5': { label: '已取消', type: 'info' },
  // 8D 状态
  '8D0': { label: '草稿', type: 'info' },
  '8D1': { label: '已提交', type: 'warning' },
  '8D2': { label: '审核中', type: 'primary' },
  '8D3': { label: '已退回', type: 'danger' },
  '8D4': { label: '已关闭', type: 'success' },
  // 申诉状态
  'AP0': { label: '草稿', type: 'info' },
  'AP1': { label: '已提交', type: 'warning' },
  'AP2': { label: '审核中', type: 'primary' },
  'AP3': { label: '已通过', type: 'success' },
  'AP4': { label: '已驳回', type: 'danger' },
  // RFQ 状态
  'RFQ0': { label: '草稿', type: 'info' },
  'RFQ1': { label: '已发布', type: 'warning' },
  'RFQ2': { label: '报价中', type: 'primary' },
  'RFQ3': { label: '已截止', type: 'info' },
  'RFQ4': { label: '已定价', type: 'success' },
  'RFQ5': { label: '已取消', type: 'info' },
  // 报价状态
  'QT0': { label: '草稿', type: 'info' },
  'QT1': { label: '已提交', type: 'warning' },
  'QT2': { label: '已采纳', type: 'success' },
  'QT3': { label: '未采纳', type: 'danger' },
  'QT4': { label: '已撤回', type: 'info' },
  // 黑名单
  'BLK0': { label: '已解除', type: 'success' },
  'BLK1': { label: '生效中', type: 'danger' },
  // 绩效
  'PERF': { label: '已评估', type: 'success' },
  // 发票
  '发票0': { label: '待开票', type: 'warning' },
  '发票1': { label: '已上传', type: 'primary' },
  '发票2': { label: '已验真', type: 'warning' },
  '发票3': { label: '已认证', type: 'success' },
  '发票4': { label: '已作废', type: 'danger' },
  // 扣款
  '扣款0': { label: '草稿', type: 'info' },
  '扣款1': { label: '已提交', type: 'warning' },
  '扣款2': { label: '已确认', type: 'success' },
  '扣款3': { label: '有异议', type: 'danger' },
  '扣款4': { label: '已入账', type: 'primary' },
  // 付款
  '付款0': { label: '待付款', type: 'warning' },
  '付款1': { label: '部分付款', type: 'primary' },
  '付款2': { label: '已付款', type: 'success' },
  '付款3': { label: '已拒绝', type: 'danger' },
  // VMI库存
  'VMI1': { label: '正常', type: 'success' },
  'VMI2': { label: '预警', type: 'warning' },
  'VMI3': { label: '缺料', type: 'danger' },
  // 需求预测
  '预测0': { label: '草稿', type: 'info' },
  '预测1': { label: '已发布', type: 'warning' },
  '预测2': { label: '已响应', type: 'primary' },
  '预测3': { label: '已关闭', type: 'info' },
  // 同步任务
  '任务0': { label: '待处理', type: 'warning' },
  '任务1': { label: '处理中', type: 'primary' },
  '任务2': { label: '已完成', type: 'success' },
  '任务3': { label: '失败', type: 'danger' },
  '任务4': { label: '已暂停', type: 'info' },
}

export const riskMap: Record<string, { label: string; type: TagType }> = {
  low: { label: '低风险', type: 'success' },
  medium: { label: '中风险', type: 'warning' },
  high: { label: '高风险', type: 'danger' },
  none: { label: '无风险', type: 'info' },
  blacklisted: { label: '已拉黑', type: 'danger' },
}