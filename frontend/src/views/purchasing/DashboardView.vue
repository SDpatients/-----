<script setup lang="ts">
import MetricCard from '@/components/common/MetricCard.vue'
import PageContainer from '@/components/common/PageContainer.vue'

const todos = ['星河电子资质审核待补充资料', 'PO-202605-0002 订单变更待确认', 'ASN-202605-0003 收货差异待处理', 'NCR-202605-0002 需要质量复核']
const risks = ['北辰包装绩效分低于 70，建议冻结新订单', '本周 2 单交付存在延期风险', '5 月对账差异金额 4060 元待确认']
</script>

<template>
  <div class="dashboard">
    <div class="metrics">
      <MetricCard label="合作供应商" value="128" trend="本月新增 6 家" tone="blue" />
      <MetricCard label="待确认订单" value="24" trend="较昨日减少 8 单" tone="green" />
      <MetricCard label="交付预警" value="7" trend="2 单高风险" tone="orange" />
      <MetricCard label="质量异常" value="5" trend="3 单处理中" tone="red" />
    </div>
    <div class="dashboard-grid">
      <PageContainer title="待办中心" subtitle="聚合采购方内部待处理事项">
        <el-timeline>
          <el-timeline-item v-for="item in todos" :key="item" type="primary" timestamp="今天">{{ item }}</el-timeline-item>
        </el-timeline>
      </PageContainer>
      <PageContainer title="风险预警" subtitle="基于虚拟数据展示业务风险">
        <div v-for="item in risks" :key="item" class="risk-item">{{ item }}</div>
      </PageContainer>
    </div>
    <PageContainer title="采购协同趋势" subtitle="订单、发货、检验、对账的阶段性趋势占位">
      <div class="trend-panel">
        <div v-for="height in [58, 72, 46, 88, 64, 92, 76, 104, 86, 110, 98, 120]" :key="height" class="trend-bar" :style="{ height: `${height}px` }" />
      </div>
    </PageContainer>
  </div>
</template>

<style scoped>
.dashboard {
  display: grid;
  gap: 18px;
}

.metrics {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 18px;
}

.dashboard-grid {
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  gap: 18px;
}

.risk-item {
  padding: 14px;
  margin-bottom: 12px;
  color: #92400e;
  background: #fff7ed;
  border: 1px solid #fed7aa;
  border-radius: 12px;
}

.trend-panel {
  display: flex;
  gap: 16px;
  align-items: flex-end;
  height: 180px;
  padding: 20px;
  background: linear-gradient(180deg, #f8fbff, #edf4fb);
  border-radius: 14px;
}

.trend-bar {
  flex: 1;
  background: linear-gradient(180deg, #1f5eff, #0bb783);
  border-radius: 10px 10px 2px 2px;
}
</style>
