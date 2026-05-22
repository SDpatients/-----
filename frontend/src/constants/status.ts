export const statusMap: Record<string, { label: string; type: 'success' | 'warning' | 'danger' | 'info' | 'primary' }> = {
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
}

export const riskMap: Record<string, { label: string; type: 'success' | 'warning' | 'danger' | 'info' }> = {
  low: { label: '低风险', type: 'success' },
  medium: { label: '中风险', type: 'warning' },
  high: { label: '高风险', type: 'danger' },
  none: { label: '无风险', type: 'info' },
}
