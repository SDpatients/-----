<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import type { AsnNotice, DeliveryLineItem } from '@/types/business'

const props = defineProps<{
  asn: AsnNotice
  lines?: DeliveryLineItem[]
  visible: boolean
}>()

const emit = defineEmits<{
  'update:visible': [value: boolean]
}>()

const printType = ref<'delivery' | 'label'>('delivery')

const today = computed(() => dayjs().format('YYYY-MM-DD'))
const totalQty = computed(() => props.lines?.reduce((s, l) => s + l.shipQty, 0) || props.asn.quantity)
const totalCases = computed(() => props.lines?.filter(l => l.caseNo).length || 0)

function handlePrint() {
  window.print()
}

function handlePdfPreview() {
  ElMessage.info('正在生成PDF预览...')
  // 使用浏览器打印为PDF功能
  window.print()
}

function handlePrintLabels() {
  printType.value = 'label'
  setTimeout(() => window.print(), 100)
}

function close() {
  emit('update:visible', false)
}
</script>

<template>
  <el-dialog
    :model-value="visible"
    title="打印送货单 / 标签"
    width="820px"
    :close-on-click-modal="false"
    @update:model-value="emit('update:visible', $event)"
  >
    <div class="print-toolbar no-print">
      <el-radio-group v-model="printType" size="small">
        <el-radio-button value="delivery">送货单</el-radio-button>
        <el-radio-button value="label">条码标签</el-radio-button>
      </el-radio-group>
      <div class="toolbar-actions">
        <el-button type="primary" size="small" @click="handlePrint">打印</el-button>
        <el-button size="small" @click="handlePdfPreview">PDF 预览/下载</el-button>
      </div>
    </div>

    <!-- 送货单模板 -->
    <div v-if="printType === 'delivery'" class="print-content delivery-note" id="delivery-note-print">
      <div class="dn-header">
        <div class="dn-title">送 货 单</div>
        <div class="dn-no">单号：{{ asn.asnNo || '-' }}</div>
      </div>

      <div class="dn-info-grid">
        <div class="dn-info-item">
          <span class="dn-label">关联订单</span>
          <span class="dn-value">{{ asn.orderNo || '-' }}</span>
        </div>
        <div class="dn-info-item">
          <span class="dn-label">供应商</span>
          <span class="dn-value">{{ asn.supplierName || '-' }}</span>
        </div>
        <div class="dn-info-item">
          <span class="dn-label">发货日期</span>
          <span class="dn-value">{{ asn.shipDate || today }}</span>
        </div>
        <div class="dn-info-item">
          <span class="dn-label">预计到货</span>
          <span class="dn-value">{{ asn.eta || '-' }}</span>
        </div>
        <div class="dn-info-item">
          <span class="dn-label">收货仓库</span>
          <span class="dn-value">{{ asn.warehouse || '-' }}</span>
        </div>
        <div class="dn-info-item">
          <span class="dn-label">打印日期</span>
          <span class="dn-value">{{ today }}</span>
        </div>
      </div>

      <table class="dn-table">
        <thead>
          <tr>
            <th>行号</th>
            <th>物料编码</th>
            <th>物料名称/规格</th>
            <th>单位</th>
            <th>订单数量</th>
            <th>本次发货</th>
            <th>批次号</th>
            <th>箱号</th>
            <th>条码</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="line in (lines?.length ? lines : [{ lineNo: 1, materialCode: '-', materialName: '-', unit: '-', orderQty: '-', shipQty: asn.quantity, batchNo: '-', caseNo: '-', barcode: '-', orderLineNo: 0, shippedQty: 0, qtyPerCase: 0, remark: '' }])" :key="line.lineNo">
            <td>{{ line.lineNo }}</td>
            <td>{{ line.materialCode || '-' }}</td>
            <td>{{ line.materialName || '-' }}</td>
            <td>{{ line.unit || '-' }}</td>
            <td class="num">{{ line.orderQty || '-' }}</td>
            <td class="num">{{ line.shipQty || '-' }}</td>
            <td>{{ line.batchNo || '-' }}</td>
            <td>{{ line.caseNo || '-' }}</td>
            <td class="barcode-td">
              <span class="barcode-mini">{{ line.barcode || '-' }}</span>
            </td>
          </tr>
        </tbody>
      </table>

      <div class="dn-footer">
        <div class="dn-footer-item">
          <span>发货数量合计：<strong>{{ totalQty }}</strong></span>
        </div>
        <div class="dn-footer-item">
          <span>箱数合计：<strong>{{ totalCases }}</strong></span>
        </div>
      </div>

      <div class="dn-signatures">
        <div class="dn-sign-item">
          <span>发货人：</span>
          <span class="sign-line"></span>
        </div>
        <div class="dn-sign-item">
          <span>司机：</span>
          <span class="sign-line"></span>
        </div>
        <div class="dn-sign-item">
          <span>收货人：</span>
          <span class="sign-line"></span>
        </div>
      </div>
    </div>

    <!-- 条码标签模板 -->
    <div v-if="printType === 'label'" class="print-content label-sheet" id="label-sheet-print">
      <div class="label-page-title">条码标签打印 - {{ asn.asnNo }}</div>
      <div class="label-grid">
        <div
          v-for="line in (lines?.length ? lines : [])"
          :key="'label-' + line.lineNo"
          class="label-card"
        >
          <div class="label-barcode">
            <svg class="label-barcode-svg" :data-barcode="line.barcode || '-'" />
            <span class="label-barcode-text">{{ line.barcode || '-' }}</span>
          </div>
          <div class="label-info">
            <div class="label-material">{{ line.materialName || '-' }}</div>
            <div class="label-detail">
              <span>批次：{{ line.batchNo || '-' }}</span>
              <span>箱号：{{ line.caseNo || '-' }}</span>
            </div>
            <div class="label-detail">
              <span>数量：{{ line.shipQty || 0 }}</span>
              <span>ASN：{{ asn.asnNo }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <el-button @click="close">关闭</el-button>
      <el-button type="primary" @click="handlePrint">打印</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.print-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
}
.toolbar-actions {
  display: flex;
  gap: 8px;
}

/* 送货单样式 */
.delivery-note {
  padding: 16px 24px;
  font-size: 13px;
  color: #2c3e50;
}
.dn-header {
  text-align: center;
  margin-bottom: 16px;
}
.dn-title {
  font-size: 22px;
  font-weight: 700;
  letter-spacing: 8px;
  margin-bottom: 4px;
}
.dn-no {
  font-size: 13px;
  color: #606266;
}
.dn-info-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
  margin-bottom: 14px;
  padding: 12px;
  background: #f8fbff;
  border: 1px solid #e4ebf3;
  border-radius: 8px;
}
.dn-info-item {
  display: flex;
  gap: 6px;
}
.dn-label {
  color: #8a98aa;
  white-space: nowrap;
}
.dn-value {
  font-weight: 600;
}
.dn-table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 12px;
}
.dn-table th, .dn-table td {
  border: 1px solid #dcdfe6;
  padding: 6px 8px;
  text-align: center;
  font-size: 12px;
}
.dn-table th {
  background: #f5f7fa;
  font-weight: 600;
  color: #303133;
}
.dn-table td.num {
  text-align: right;
}
.barcode-td {
  font-family: 'Courier New', monospace;
  font-size: 10px;
}
.barcode-mini {
  display: inline-block;
  background: #f0f0f0;
  padding: 2px 6px;
  border-radius: 3px;
}
.dn-footer {
  display: flex;
  justify-content: flex-end;
  gap: 24px;
  margin-bottom: 16px;
}
.dn-footer-item {
  font-size: 14px;
}
.dn-signatures {
  display: flex;
  justify-content: space-around;
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px dashed #dcdfe6;
}
.dn-sign-item {
  display: flex;
  align-items: center;
  gap: 8px;
}
.sign-line {
  display: inline-block;
  width: 100px;
  border-bottom: 1px solid #303133;
}

/* 条码标签样式 */
.label-sheet {
  padding: 12px;
}
.label-page-title {
  font-size: 16px;
  font-weight: 700;
  text-align: center;
  margin-bottom: 12px;
}
.label-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}
.label-card {
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  padding: 10px;
  display: flex;
  gap: 10px;
  align-items: center;
  page-break-inside: avoid;
}
.label-barcode {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 120px;
}
.label-barcode-svg {
  width: 120px;
  height: 40px;
  background: linear-gradient(90deg, #000 1px, transparent 1px, transparent 3px, #000 3px, #000 4px, transparent 4px, transparent 6px, #000 6px, #000 7px, transparent 7px) repeat-x;
  border: 1px solid #333;
}
.label-barcode-text {
  font-family: monospace;
  font-size: 9px;
  color: #333;
  margin-top: 2px;
}
.label-info {
  flex: 1;
}
.label-material {
  font-weight: 600;
  font-size: 13px;
  margin-bottom: 4px;
}
.label-detail {
  display: flex;
  gap: 12px;
  font-size: 11px;
  color: #606266;
}

/* 打印样式 */
@media print {
  .no-print, ::v-deep(.el-dialog__header), ::v-deep(.el-dialog__footer), ::v-deep(.el-overlay) {
    display: none !important;
  }
  ::v-deep(.el-dialog) {
    position: static !important;
    width: 100% !important;
    max-width: 100% !important;
    margin: 0 !important;
    padding: 0 !important;
    box-shadow: none !important;
    border: none !important;
  }
  ::v-deep(.el-dialog__body) {
    padding: 0 !important;
  }
  .print-content {
    width: 100%;
  }
  body {
    background: white !important;
  }
  @page {
    size: A4;
    margin: 12mm;
  }
}
</style>