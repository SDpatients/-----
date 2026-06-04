<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { logisticsApi } from '@/api/logistics'
import { mockApi } from '@/api/mockApi'
import { toAsn } from '@/api/adapters'
import { toId } from '@/utils/id'
import PageContainer from '@/components/common/PageContainer.vue'
import StatusTag from '@/components/business/StatusTag.vue'
import type { AsnNotice } from '@/types/business'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const asn = ref<AsnNotice | null>(null)
const lines = ref<any[]>([])

const id = computed(() => route.params.id as string)

const loadData = async () => {
  loading.value = true
  try {
    // 加载发货单基本信息
    const data = await logisticsApi.deliveryDetail(id.value)
    asn.value = toAsn(data)

    // 加载发货明细
    try {
      const details = await mockApi.getDeliveryDetails(toId(id.value))
      lines.value = details
    } catch {
      lines.value = []
    }
  } catch {
    ElMessage.error('获取发货单详情失败')
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<template>
  <PageContainer title="发货单详情" subtitle="查看发货单基本信息、发货明细">
    <template #actions>
      <el-button @click="router.push('/supplier/deliveries')">返回列表</el-button>
      <el-button type="primary" @click="loadData">刷新</el-button>
    </template>

    <div v-loading="loading">
      <el-empty v-if="!asn" description="未找到数据记录" />

      <template v-if="asn">
        <!-- 基本信息 -->
        <div class="section-title">基本信息</div>
        <div class="detail-grid">
          <div class="detail-item">
            <div class="detail-label">ASN号</div>
            <div class="detail-value">{{ asn.asnNo || '-' }}</div>
          </div>
          <div class="detail-item">
            <div class="detail-label">关联订单</div>
            <div class="detail-value">{{ asn.orderNo || '-' }}</div>
          </div>
          <div class="detail-item">
            <div class="detail-label">发货数量</div>
            <div class="detail-value">{{ asn.quantity || '-' }}</div>
          </div>
          <div class="detail-item">
            <div class="detail-label">收货仓库</div>
            <div class="detail-value">{{ asn.warehouse || '-' }}</div>
          </div>
          <div class="detail-item">
            <div class="detail-label">发货日期</div>
            <div class="detail-value">{{ asn.shipDate || '-' }}</div>
          </div>
          <div class="detail-item">
            <div class="detail-label">预计到货</div>
            <div class="detail-value">{{ asn.eta || '-' }}</div>
          </div>
          <div class="detail-item">
            <div class="detail-label">状态</div>
            <div class="detail-value"><StatusTag :value="asn.status" /></div>
          </div>
          <div v-if="asn.supplierName" class="detail-item">
            <div class="detail-label">供应商</div>
            <div class="detail-value">{{ asn.supplierName }}</div>
          </div>
        </div>

        <!-- 发货明细 -->
        <div class="section-title">发货明细</div>
        <el-table :data="lines" border>
          <el-table-column prop="materialCode" label="物料编码" width="140" />
          <el-table-column prop="materialName" label="物料名称" min-width="140" />
          <el-table-column prop="materialSpec" label="规格型号" width="150" />
          <el-table-column prop="unit" label="单位" width="80" />
          <el-table-column prop="planQty" label="计划数量" width="110">
            <template #default="{ row }">{{ row.planQty?.toFixed(2) || '-' }}</template>
          </el-table-column>
          <el-table-column prop="actualQty" label="本次发货" width="110">
            <template #default="{ row }">{{ row.actualQty?.toFixed(2) || '-' }}</template>
          </el-table-column>
          <el-table-column prop="batchNo" label="批次号" width="160" />
          <el-table-column prop="boxCount" label="箱数" width="90" />
          <el-table-column prop="caseNo" label="箱号" width="120" />
          <el-table-column prop="barcode" label="条码" width="140" />
          <el-table-column prop="remark" label="备注" min-width="120" />
        </el-table>
      </template>
    </div>
  </PageContainer>
</template>

<style scoped>
.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin: 16px 0 12px;
  padding-left: 8px;
  border-left: 3px solid #409eff;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
  margin-bottom: 16px;
}

.detail-item {
  padding: 10px 12px;
  background: #fafbfc;
  border: 1px solid #edf0f4;
  border-radius: 8px;
}

.detail-label {
  font-size: 11px;
  color: #8a98aa;
  margin-bottom: 4px;
}

.detail-value {
  font-size: 14px;
  font-weight: 600;
  color: #2c3e50;
}
</style>
