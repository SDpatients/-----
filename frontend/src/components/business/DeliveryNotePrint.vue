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

const linesForPrint = computed(() =>
  props.lines?.length ? props.lines : [{ lineNo: 1, materialCode: '-', materialName: '-', unit: '-', orderQty: '-', shipQty: props.asn.quantity, batchNo: '-', caseNo: '-', barcode: '-', orderLineNo: 0, shippedQty: 0, qtyPerCase: 0, remark: '' }]
)

function generatePrintHtml() {
  const isDelivery = printType.value === 'delivery'
  const lines = linesForPrint.value

  let contentHtml = ''

  if (isDelivery) {
    contentHtml = `
      <div class="delivery-note">
        <div class="dn-header">
          <div class="dn-title">送 货 单</div>
          <div class="dn-no">单号：${props.asn.asnNo || '-'}</div>
        </div>
        <div class="dn-info-grid">
          <div class="dn-info-item"><span class="dn-label">关联订单</span><span class="dn-value">${props.asn.orderNo || '-'}</span></div>
          <div class="dn-info-item"><span class="dn-label">供应商</span><span class="dn-value">${props.asn.supplierName || '-'}</span></div>
          <div class="dn-info-item"><span class="dn-label">发货日期</span><span class="dn-value">${props.asn.shipDate || today.value}</span></div>
          <div class="dn-info-item"><span class="dn-label">预计到货</span><span class="dn-value">${props.asn.eta || '-'}</span></div>
          <div class="dn-info-item"><span class="dn-label">收货仓库</span><span class="dn-value">${props.asn.warehouse || '-'}</span></div>
          <div class="dn-info-item"><span class="dn-label">打印日期</span><span class="dn-value">${today.value}</span></div>
        </div>
        <table class="dn-table">
          <thead>
            <tr><th>行号</th><th>物料编码</th><th>物料名称/规格</th><th>单位</th><th>订单数量</th><th>本次发货</th><th>批次号</th><th>箱号</th><th>条码</th></tr>
          </thead>
          <tbody>
            ${lines.map(line => `
              <tr>
                <td>${line.lineNo}</td>
                <td>${line.materialCode || '-'}</td>
                <td>${line.materialName || '-'}</td>
                <td>${line.unit || '-'}</td>
                <td class="num">${line.orderQty || '-'}</td>
                <td class="num">${line.shipQty || '-'}</td>
                <td>${line.batchNo || '-'}</td>
                <td>${line.caseNo || '-'}</td>
                <td class="barcode-cell">
                  ${line.barcode && line.barcode !== '-' ? `<svg class="barcode-svg" data-barcode="${line.barcode}"></svg>` : '<span>-</span>'}
                </td>
              </tr>
            `).join('')}
          </tbody>
        </table>
        <div class="dn-footer">
          <div class="dn-footer-item"><span>发货数量合计：<strong>${totalQty.value}</strong></span></div>
          <div class="dn-footer-item"><span>箱数合计：<strong>${totalCases.value}</strong></span></div>
        </div>
        <div class="dn-signatures">
          <div class="dn-sign-item"><span>发货人：</span><span class="sign-line"></span></div>
          <div class="dn-sign-item"><span>司机：</span><span class="sign-line"></span></div>
          <div class="dn-sign-item"><span>收货人：</span><span class="sign-line"></span></div>
        </div>
      </div>
    `
  } else {
    contentHtml = `
      <div class="label-sheet">
        <div class="label-grid">
          ${lines.map(line => `
            <div class="label-card">
              <div class="label-barcode">
                <svg class="label-barcode-svg" data-barcode="${line.barcode || '-'}"></svg>
                <span class="label-barcode-text">${line.barcode || '-'}</span>
              </div>
              <div class="label-info">
                <div class="label-material">${line.materialName || '-'}</div>
                <div class="label-detail"><span>批次：${line.batchNo || '-'}</span><span>箱号：${line.caseNo || '-'}</span></div>
                <div class="label-detail"><span>数量：${line.shipQty || 0}</span><span>ASN：${props.asn.asnNo}</span></div>
              </div>
            </div>
          `).join('')}
        </div>
      </div>
    `
  }

  return `
    <!DOCTYPE html>
    <html>
    <head>
      <meta charset="utf-8">
      <title>打印 - ${props.asn.asnNo}<\/title>
      <script src="https://cdn.jsdelivr.net/npm/jsbarcode@3.11.6/dist/JsBarcode.all.min.js"><\/script>
      <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif; font-size: 13px; color: #2c3e50; padding: 20px; }
        .delivery-note { padding: 16px 24px; }
        .dn-header { text-align: center; margin-bottom: 16px; }
        .dn-title { font-size: 22px; font-weight: 700; letter-spacing: 8px; margin-bottom: 4px; }
        .dn-no { font-size: 13px; color: #606266; }
        .dn-info-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px; margin-bottom: 14px; padding: 12px; background: #f8fbff; border: 1px solid #e4ebf3; border-radius: 8px; }
        .dn-info-item { display: flex; gap: 6px; }
        .dn-label { color: #8a98aa; white-space: nowrap; }
        .dn-value { font-weight: 600; }
        .dn-table { width: 100%; border-collapse: collapse; margin-bottom: 12px; }
        .dn-table th, .dn-table td { border: 1px solid #dcdfe6; padding: 6px 8px; text-align: center; font-size: 12px; }
        .dn-table th { background: #f5f7fa; font-weight: 600; color: #303133; }
        .dn-table td.num { text-align: right; }
        .barcode-cell { padding: 4px; }
        .barcode-svg { max-width: 100%; height: 28px; }
        .dn-footer { display: flex; justify-content: flex-end; gap: 24px; margin-bottom: 16px; }
        .dn-footer-item { font-size: 14px; }
        .dn-signatures { display: flex; justify-content: space-around; margin-top: 24px; padding-top: 16px; border-top: 1px dashed #dcdfe6; }
        .dn-sign-item { display: flex; align-items: center; gap: 8px; }
        .sign-line { display: inline-block; width: 100px; border-bottom: 1px solid #303133; }
        .label-sheet { padding: 12px; }
        .label-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 10px; }
        .label-card { border: 1px solid #dcdfe6; border-radius: 8px; padding: 10px; display: flex; gap: 10px; align-items: center; page-break-inside: avoid; }
        .label-barcode { display: flex; flex-direction: column; align-items: center; min-width: 120px; }
        .label-barcode-svg { width: 120px; height: 40px; }
        .label-barcode-text { font-family: monospace; font-size: 9px; color: #333; margin-top: 2px; }
        .label-info { flex: 1; }
        .label-material { font-weight: 600; font-size: 13px; margin-bottom: 4px; }
        .label-detail { display: flex; gap: 12px; font-size: 11px; color: #606266; }
        @page { size: A4; margin: 12mm; }
      <\/style>
    <\/head>
    <body>${contentHtml}
      <script>
        console.log('[Print] page loaded');
        document.addEventListener('DOMContentLoaded', function() {
          console.log('[Print] DOMContentLoaded handler running');
          console.log('[Print] JsBarcode:', typeof window.JsBarcode);

          var barcodeFn = window.JsBarcode;
          if (!barcodeFn) {
            console.error('[Print] JsBarcode NOT loaded!');
          } else {
            var deliveryBarcodes = document.querySelectorAll('.barcode-svg');
            console.log('[Print] Delivery barcodes:', deliveryBarcodes.length);
            deliveryBarcodes.forEach(function(svg, idx) {
              var code = svg.getAttribute('data-barcode');
              console.log('[Print] Barcode[' + idx + ']:', code);
              if (code && code !== '-') {
                try {
                  barcodeFn(svg, code, { format: "CODE128", width: 1.2, height: 25, displayValue: false, margin: 0 });
                  console.log('[Print] OK[' + idx + ']');
                } catch (e) {
                  console.error('[Print] Error[' + idx + ']:', e);
                }
              }
            });

            var labelBarcodes = document.querySelectorAll('.label-barcode-svg');
            console.log('[Print] Label barcodes:', labelBarcodes.length);
            labelBarcodes.forEach(function(svg, idx) {
              var code = svg.getAttribute('data-barcode');
              console.log('[Print] Label Barcode[' + idx + ']:', code);
              if (code && code !== '-') {
                try {
                  barcodeFn(svg, code, { format: "CODE128", width: 1.5, height: 35, displayValue: false, margin: 0 });
                  console.log('[Print] Label OK[' + idx + ']');
                } catch (e) {
                  console.error('[Print] Label Error[' + idx + ']:', e);
                }
              }
            });
          }

          setTimeout(function() {
            console.log('[Print] Calling print()');
            window.print();
          }, 500);
        });
      <\/script>
    <\/body>
    <\/html>
  `
}

function printContent() {
  const html = generatePrintHtml()
  const printWindow = window.open('', '_blank')
  if (!printWindow) {
    ElMessage.error('请允许弹出窗口以使用打印功能')
    return
  }
  printWindow.document.write(html)
  printWindow.document.close()
}

function handlePrint() {
  printContent()
}

function handlePdfPreview() {
  ElMessage.info('请在打印对话框中选择"另存为PDF"')
  printContent()
}

function handlePrintLabels() {
  printType.value = 'label'
  setTimeout(() => printContent(), 100)
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

    <!-- 预览区域 -->
    <div class="print-preview">
      <div v-if="printType === 'delivery'" class="delivery-note">
        <div class="dn-header">
          <div class="dn-title">送 货 单</div>
          <div class="dn-no">单号：{{ asn.asnNo || '-' }}</div>
        </div>
        <div class="dn-info-grid">
          <div class="dn-info-item"><span class="dn-label">关联订单</span><span class="dn-value">{{ asn.orderNo || '-' }}</span></div>
          <div class="dn-info-item"><span class="dn-label">供应商</span><span class="dn-value">{{ asn.supplierName || '-' }}</span></div>
          <div class="dn-info-item"><span class="dn-label">发货日期</span><span class="dn-value">{{ asn.shipDate || today }}</span></div>
          <div class="dn-info-item"><span class="dn-label">预计到货</span><span class="dn-value">{{ asn.eta || '-' }}</span></div>
          <div class="dn-info-item"><span class="dn-label">收货仓库</span><span class="dn-value">{{ asn.warehouse || '-' }}</span></div>
          <div class="dn-info-item"><span class="dn-label">打印日期</span><span class="dn-value">{{ today }}</span></div>
        </div>
        <table class="dn-table">
          <thead>
            <tr><th>行号</th><th>物料编码</th><th>物料名称/规格</th><th>单位</th><th>订单数量</th><th>本次发货</th><th>批次号</th><th>箱号</th><th>条码</th></tr>
          </thead>
          <tbody>
            <tr v-for="line in linesForPrint" :key="line.lineNo">
              <td>{{ line.lineNo }}</td>
              <td>{{ line.materialCode || '-' }}</td>
              <td>{{ line.materialName || '-' }}</td>
              <td>{{ line.unit || '-' }}</td>
              <td class="num">{{ line.orderQty || '-' }}</td>
              <td class="num">{{ line.shipQty || '-' }}</td>
              <td>{{ line.batchNo || '-' }}</td>
              <td>{{ line.caseNo || '-' }}</td>
              <td class="barcode-td"><span class="barcode-mini">{{ line.barcode || '-' }}</span></td>
            </tr>
          </tbody>
        </table>
        <div class="dn-footer">
          <div class="dn-footer-item"><span>发货数量合计：<strong>{{ totalQty }}</strong></span></div>
          <div class="dn-footer-item"><span>箱数合计：<strong>{{ totalCases }}</strong></span></div>
        </div>
        <div class="dn-signatures">
          <div class="dn-sign-item"><span>发货人：</span><span class="sign-line"></span></div>
          <div class="dn-sign-item"><span>司机：</span><span class="sign-line"></span></div>
          <div class="dn-sign-item"><span>收货人：</span><span class="sign-line"></span></div>
        </div>
      </div>
      <div v-else class="label-sheet">
        <div class="label-grid">
          <div v-for="line in lines" :key="'label-' + line.lineNo" class="label-card">
            <div class="label-barcode">
              <svg class="label-barcode-svg" :data-barcode="line.barcode || '-'" />
              <span class="label-barcode-text">{{ line.barcode || '-' }}</span>
            </div>
            <div class="label-info">
              <div class="label-material">{{ line.materialName || '-' }}</div>
              <div class="label-detail"><span>批次：{{ line.batchNo || '-' }}</span><span>箱号：{{ line.caseNo || '-' }}</span></div>
              <div class="label-detail"><span>数量：{{ line.shipQty || 0 }}</span><span>ASN：{{ asn.asnNo }}</span></div>
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

.print-preview {
  max-height: 600px;
  overflow-y: auto;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 8px;
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

</style>