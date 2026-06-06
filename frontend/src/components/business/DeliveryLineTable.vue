<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import type { DeliveryLineItem } from '@/types/business'

export interface OrderLineOption {
  lineNo: number
  materialCode: string
  materialName: string
  spec: string
  unit: string
  orderQty: number
  shippedQty: number
  deliveryDate: string
  orderDetailId?: number | string
}

const props = withDefaults(defineProps<{
  modelValue: DeliveryLineItem[]
  orderLines?: OrderLineOption[]
  readonly?: boolean
}>(), {
  orderLines: () => [],
  readonly: false,
})

const emit = defineEmits<{
  'update:modelValue': [value: DeliveryLineItem[]]
}>()

const lines = ref<DeliveryLineItem[]>(
  props.modelValue.length > 0
    ? props.modelValue.map(l => ({ ...l }))
    : [createEmptyLine(1)],
)

watch(() => props.modelValue, (val) => {
  if (val.length > 0) lines.value = val.map(l => ({ ...l }))
}, { deep: true })

watch(lines, (val) => {
  const newVal = val.map(l => ({ ...l }))
  if (JSON.stringify(newVal) !== JSON.stringify(props.modelValue)) {
    emit('update:modelValue', newVal)
  }
}, { deep: true })

function createEmptyLine(lineNo: number): DeliveryLineItem {
  return {
    lineNo,
    materialCode: '',
    materialName: '',
    orderLineNo: 0,
    orderDetailId: null,
    unit: '',
    orderQty: 0,
    shippedQty: 0,
    shipQty: 0,
    batchNo: '',
    caseNo: '',
    qtyPerCase: 0,
    barcode: '',
    remark: '',
  }
}

function addLine() {
  const maxLine = lines.value.reduce((max, l) => Math.max(max, l.lineNo), 0)
  lines.value.push(createEmptyLine(maxLine + 1))
}

function removeLine(index: number) {
  if (lines.value.length <= 1) return
  lines.value.splice(index, 1)
}

// 选择订单行后自动回填
function onOrderLineSelect(index: number, lineNo: number) {
  const selected = props.orderLines.find(o => o.lineNo === lineNo)
  if (!selected) return
  const line = lines.value[index]
  line.orderLineNo = selected.lineNo
  line.orderDetailId = selected.orderDetailId
  line.materialCode = selected.materialCode
  line.materialName = `${selected.materialName} ${selected.spec}`.trim()
  line.unit = selected.unit
  line.orderQty = selected.orderQty
  line.shippedQty = selected.shippedQty
  // 自动生成批次号
  if (!line.batchNo) {
    const now = new Date()
    const date = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}${String(now.getDate()).padStart(2, '0')}`
    line.batchNo = `B${date}-${String(index + 1).padStart(2, '0')}`
  }
  // 自动生成条码
  if (!line.barcode) {
    const orderPart = selected.materialCode.replace(/[^A-Z0-9]/g, '').slice(0, 6)
    const dateCode = new Date().toISOString().slice(2, 10).replace(/-/g, '')
    const seq = String(index + 1).padStart(3, '0')
    line.barcode = `${orderPart}${dateCode}${seq}`
  }
}

// 获取订单行的可发数量
function getAvailableQty(line: DeliveryLineItem): number {
  const orderLine = props.orderLines.find(o => o.lineNo === line.orderLineNo)
  if (!orderLine) return 0
  return orderLine.orderQty - orderLine.shippedQty
}

const shipTotal = computed(() => lines.value.reduce((sum, l) => sum + l.shipQty, 0))
const caseTotal = computed(() => lines.value.reduce((sum, l) => sum + (l.caseNo ? 1 : 0), 0))

function generateBarcode(index: number) {
  const line = lines.value[index]
  if (!line) return
  const orderPart = (line.materialCode || 'ASN').replace(/[^A-Z0-9]/g, '').slice(0, 6)
  const dateCode = new Date().toISOString().slice(2, 10).replace(/-/g, '')
  const seq = String(index + 1).padStart(3, '0')
  const newBarcode = `${orderPart}${dateCode}${seq}`
  // 使用新对象引用触发响应式更新
  lines.value[index] = { ...line, barcode: newBarcode }
}
</script>

<template>
  <div class="delivery-line-table">
    <div class="table-header">
      <span class="header-title">送货明细行</span>
      <span class="header-summary">
        共 {{ lines.length }} 行，发货合计 <strong>{{ shipTotal }}</strong>，箱数 <strong>{{ caseTotal }}</strong>
      </span>
    </div>

    <el-table :data="lines" border size="small" style="width: 100%">
      <el-table-column label="行号" width="60" fixed="left">
        <template #default="{ row }">{{ row.lineNo }}</template>
      </el-table-column>

      <el-table-column label="物料编码" width="130">
        <template #default="{ row }">
          <el-input
            v-if="!readonly"
            v-model="row.materialCode"
            size="small"
            placeholder="物料编码"
          />
          <span v-else>{{ row.materialCode || '-' }}</span>
        </template>
      </el-table-column>

      <el-table-column label="物料名称/规格" min-width="150">
        <template #default="{ row }">
          <el-input
            v-if="!readonly"
            v-model="row.materialName"
            size="small"
            placeholder="物料名称"
          />
          <span v-else>{{ row.materialName || '-' }}</span>
        </template>
      </el-table-column>

      <el-table-column label="单位" width="70">
        <template #default="{ row }">{{ row.unit || '-' }}</template>
      </el-table-column>

      <el-table-column label="订单数量" width="90">
        <template #default="{ row }">{{ row.orderQty || '-' }}</template>
      </el-table-column>

      <el-table-column label="已发数量" width="90">
        <template #default="{ row }">{{ row.shippedQty || 0 }}</template>
      </el-table-column>

      <el-table-column label="本次发货" width="100">
        <template #default="{ row }">
          <el-input-number
            v-if="!readonly"
            v-model="row.shipQty"
            :min="1"
            :max="row.orderQty - row.shippedQty || undefined"
            size="small"
            style="width: 100%"
          />
          <span v-else>{{ row.shipQty || '-' }}</span>
        </template>
      </el-table-column>

      <el-table-column label="批次号" width="160">
        <template #default="{ row }">
          <el-input
            v-if="!readonly"
            v-model="row.batchNo"
            size="small"
            placeholder="如 B20260520-01"
          />
          <el-tag v-else size="small" type="info">{{ row.batchNo || '-' }}</el-tag>
        </template>
      </el-table-column>

      <el-table-column label="箱号" width="110">
        <template #default="{ row }">
          <el-input
            v-if="!readonly"
            v-model="row.caseNo"
            size="small"
            placeholder="如 C01"
          />
          <span v-else>{{ row.caseNo || '-' }}</span>
        </template>
      </el-table-column>

      <el-table-column label="每箱数量" width="100">
        <template #default="{ row }">
          <el-input-number
            v-if="!readonly"
            v-model="row.qtyPerCase"
            :min="0"
            size="small"
            style="width: 100%"
          />
          <span v-else>{{ row.qtyPerCase || '-' }}</span>
        </template>
      </el-table-column>

      <el-table-column label="备注" min-width="100">
        <template #default="{ row }">
          <el-input
            v-if="!readonly"
            v-model="row.remark"
            size="small"
            placeholder="备注"
          />
          <span v-else>{{ row.remark || '-' }}</span>
        </template>
      </el-table-column>

      <el-table-column v-if="!readonly" label="操作" width="70" fixed="right">
        <template #default="{ $index }">
          <el-button
            link
            type="danger"
            size="small"
            :disabled="lines.length <= 1"
            @click="removeLine($index)"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div v-if="!readonly" class="table-footer">
      <el-button type="primary" link size="small" @click="addLine">
        + 添加明细行
      </el-button>
    </div>
  </div>
</template>

<style scoped>
.delivery-line-table {
  margin-top: 4px;
}
.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.header-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}
.header-summary {
  font-size: 12px;
  color: #909399;
}
.table-footer {
  margin-top: 8px;
  text-align: left;
}
.barcode-cell {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}
.barcode-svg {
  display: flex;
  flex-direction: column;
  align-items: center;
}
.barcode-img {
  width: 120px;
  height: 28px;
}
.barcode-text {
  font-size: 10px;
  font-family: monospace;
  color: #606266;
}
</style>