<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { logisticsApi } from '@/api/logistics'
import { toAsn, toReceiptRecord } from '@/api/adapters'
import PageContainer from '@/components/common/PageContainer.vue'
import StatusTag from '@/components/business/StatusTag.vue'
import type { ReceiptRecord } from '@/types/business'

const router = useRouter()
const barcodeInput = ref('')
const inputRef = ref<HTMLInputElement>()
const loading = ref(false)
const scannedRecord = ref<ReceiptRecord | null>(null)

const receiveForm = reactive({
  receivedQty: 0,
  location: '',
  remark: '',
  discrepancyType: '',
  discrepantQty: 0,
  discrepancyReason: '',
})

function onScan() {
  const barcode = barcodeInput.value.trim()
  if (!barcode) {
    ElMessage.warning('请扫描或输入条码')
    return
  }
  lookupByBarcode(barcode)
}

async function lookupByBarcode(barcode: string) {
  loading.value = true
  try {
    const result = await logisticsApi.receiptScan(barcode)
    scannedRecord.value = toReceiptRecord(result)
    receiveForm.receivedQty = scannedRecord.value.receiptQty || scannedRecord.value.planQty || 0
    receiveForm.location = scannedRecord.value.location || ''
    ElMessage.success(`已识别：物料 ${scannedRecord.value.materialName}`)
  } catch {
    scannedRecord.value = null
    ElMessage.error('未找到匹配的收货记录，请检查条码')
  } finally {
    loading.value = false
  }
}

async function confirmReceive() {
  if (!scannedRecord.value) return
  if (receiveForm.receivedQty <= 0) {
    ElMessage.warning('请输入实收数量')
    return
  }
  loading.value = true
  try {
    const planQty = scannedRecord.value.planQty || 0
    const confirmData: any = {
      receiptQty: receiveForm.receivedQty,
      location: receiveForm.location,
      remark: receiveForm.remark,
    }
    if (receiveForm.receivedQty !== planQty) {
      confirmData.rejectQty = planQty - receiveForm.receivedQty
      if (confirmData.rejectQty < 0) confirmData.rejectQty = 0
    }
    await logisticsApi.receiptConfirm(scannedRecord.value.id, confirmData)
    if (receiveForm.receivedQty !== planQty) {
      try {
        await logisticsApi.receiptAdjust(scannedRecord.value.id, {
          diffQty: Math.abs(planQty - receiveForm.receivedQty),
          diffReason: receiveForm.discrepancyReason || '数量差异',
          handleMethod: receiveForm.discrepancyType === '破损' ? 2 : 1,
          remark: receiveForm.remark,
        })
      } catch { /* 差异调整失败不影响主流程 */ }
    }
    ElMessage.success('收货确认成功')
    resetForm()
  } finally {
    loading.value = false
  }
}

function resetForm() {
  scannedRecord.value = null
  barcodeInput.value = ''
  receiveForm.receivedQty = 0
  receiveForm.location = ''
  receiveForm.remark = ''
  receiveForm.discrepancyType = ''
  receiveForm.discrepantQty = 0
  receiveForm.discrepancyReason = ''
  inputRef.value?.focus()
}

// 键盘事件：扫码枪通常以回车结束
function onKeydown(e: KeyboardEvent) {
  if (e.key === 'Enter') {
    e.preventDefault()
    onScan()
  }
}

onMounted(() => {
  inputRef.value?.focus()
})

const planQty = computed(() => scannedRecord.value?.planQty || 0)
const hasDiscrepancy = computed(() => scannedRecord.value && receiveForm.receivedQty !== planQty.value)
</script>

<template>
  <PageContainer title="仓库扫码收货" subtitle="扫描条码或输入ASN号快速收货确认">
    <template #actions>
      <el-button @click="router.push('/purchasing/asn')">返回列表</el-button>
    </template>

    <div class="scan-area">
      <div class="scan-input-area">
        <el-input
          ref="inputRef"
          v-model="barcodeInput"
          placeholder="扫描或输入条码 / ASN号"
          size="large"
          clearable
          @keydown="onKeydown"
        >
          <template #prefix>
            <el-icon><svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 7V5a2 2 0 0 1 2-2h2"/><path d="M17 3h2a2 2 0 0 1 2 2v2"/><path d="M21 17v2a2 2 0 0 1-2 2h-2"/><path d="M7 21H5a2 2 0 0 1-2-2v-2"/><line x1="7" y1="12" x2="17" y2="12"/></svg></el-icon>
          </template>
          <template #append>
            <el-button type="primary" :loading="loading" @click="onScan">查询</el-button>
          </template>
        </el-input>
        <div class="scan-hint">支持扫码枪、手动输入条码或 ASN 号，按回车查询</div>
      </div>

      <el-divider />

      <!-- 扫码结果 -->
      <div v-if="scannedRecord" v-loading="loading" class="scan-result">
        <div class="result-header">
          <div class="result-title">收货记录信息</div>
          <StatusTag :value="scannedRecord.receiptStatus" />
        </div>

        <div class="result-grid">
          <div class="result-item">
            <span class="result-label">物料编码</span>
            <span class="result-value">{{ scannedRecord.materialCode }}</span>
          </div>
          <div class="result-item">
            <span class="result-label">物料名称</span>
            <span class="result-value">{{ scannedRecord.materialName }}</span>
          </div>
          <div class="result-item">
            <span class="result-label">计划数量</span>
            <span class="result-value">{{ scannedRecord.planQty }}</span>
          </div>
          <div class="result-item">
            <span class="result-label">已收数量</span>
            <span class="result-value">{{ scannedRecord.receiptQty }}</span>
          </div>
          <div class="result-item">
            <span class="result-label">仓库</span>
            <span class="result-value">{{ scannedRecord.warehouseName || '-' }}</span>
          </div>
          <div class="result-item">
            <span class="result-label">库位</span>
            <span class="result-value">{{ scannedRecord.location || '-' }}</span>
          </div>
        </div>

        <el-divider />

        <div class="receive-form">
          <div class="result-title">收货确认</div>
          <el-form :model="receiveForm" label-width="100px" style="max-width: 560px">
            <el-form-item label="实收数量" required>
              <el-input-number v-model="receiveForm.receivedQty" :min="0" style="width: 200px" />
              <span v-if="hasDiscrepancy" class="diff-warning">
                {{ receiveForm.receivedQty < planQty ? '少收' : '多收' }}
                {{ Math.abs(receiveForm.receivedQty - planQty) }}
              </span>
            </el-form-item>
            <el-form-item label="收货库位">
              <el-input v-model="receiveForm.location" placeholder="如 A-01-03" />
            </el-form-item>

            <!-- 差异记录 -->
            <template v-if="hasDiscrepancy">
              <el-divider content-position="left">收货差异记录</el-divider>
              <el-form-item label="差异类型">
                <el-select v-model="receiveForm.discrepancyType" style="width: 200px">
                  <el-option label="少收" value="少收" />
                  <el-option label="多收" value="多收" />
                  <el-option label="破损" value="破损" />
                  <el-option label="错发" value="错发" />
                  <el-option label="其他" value="其他" />
                </el-select>
              </el-form-item>
              <el-form-item label="差异数量">
                <el-input-number v-model="receiveForm.discrepantQty" :min="0" :value="Math.abs(receiveForm.receivedQty - planQty)" disabled style="width: 200px" />
              </el-form-item>
              <el-form-item label="差异原因">
                <el-input
                  v-model="receiveForm.discrepancyReason"
                  type="textarea"
                  :rows="2"
                  placeholder="请描述差异原因"
                />
              </el-form-item>
            </template>

            <el-form-item label="备注">
              <el-input v-model="receiveForm.remark" placeholder="收货备注" />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" size="large" :loading="loading" @click="confirmReceive">
                确认收货
              </el-button>
              <el-button size="large" @click="resetForm">清空重扫</el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>

      <el-empty v-if="!scannedRecord && !loading" description="扫描或输入条码查询待收货记录" :image-size="120" />
    </div>
  </PageContainer>
</template>

<style scoped>
.scan-area {
  max-width: 760px;
}
.scan-input-area {
  margin-bottom: 8px;
}
.scan-hint {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
  text-align: center;
}
.scan-result {
  background: #f8fbff;
  border: 1px solid #e4ebf3;
  border-radius: 12px;
  padding: 20px;
}
.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.result-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}
.result-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}
.result-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 8px 10px;
  background: white;
  border-radius: 6px;
}
.result-label {
  font-size: 11px;
  color: #8a98aa;
}
.result-value {
  font-size: 14px;
  font-weight: 600;
  color: #2c3e50;
}
.receive-form {
  margin-top: 4px;
}
.diff-warning {
  margin-left: 8px;
  color: #e6a23c;
  font-size: 13px;
  font-weight: 600;
}
</style>