<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { logisticsApi } from '@/api/logistics'
import { toAsn } from '@/api/adapters'
import PageContainer from '@/components/common/PageContainer.vue'
import StatusTag from '@/components/business/StatusTag.vue'
import type { AsnNotice } from '@/types/business'

const router = useRouter()
const barcodeInput = ref('')
const inputRef = ref<HTMLInputElement>()
const loading = ref(false)
const scannedAsn = ref<AsnNotice | null>(null)

// 收货确认表单
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
    const result = await logisticsApi.scanReceive(barcode)
    scannedAsn.value = toAsn(result)
    receiveForm.receivedQty = scannedAsn.value.quantity || 0
    receiveForm.location = scannedAsn.value.warehouse || ''
    ElMessage.success(`已识别：${scannedAsn.value.asnNo}`)
  } catch {
    scannedAsn.value = null
    ElMessage.error('未找到匹配的送货单，请检查条码')
  } finally {
    loading.value = false
  }
}

async function confirmReceive() {
  if (!scannedAsn.value) return
  if (receiveForm.receivedQty <= 0) {
    ElMessage.warning('请输入实收数量')
    return
  }
  loading.value = true
  try {
    const data: any = {
      receivedQty: receiveForm.receivedQty,
      location: receiveForm.location,
      remark: receiveForm.remark,
    }
    // 如果有差异，追加差异信息
    const asnQty = scannedAsn.value.quantity || 0
    if (receiveForm.receivedQty !== asnQty) {
      data.discrepancy = {
        asnId: scannedAsn.value.id,
        asnNo: scannedAsn.value.asnNo,
        receivedQty: receiveForm.receivedQty,
        discrepantQty: asnQty - receiveForm.receivedQty,
        discrepancyReason: receiveForm.discrepancyReason || '数量差异',
        discrepancyType: receiveForm.discrepancyType || (receiveForm.receivedQty < asnQty ? '少收' : '多收'),
        handler: '',
        remark: receiveForm.remark,
      }
    }
    await logisticsApi.confirmScanReceive(scannedAsn.value.id, data)
    ElMessage.success('收货确认成功')
    resetForm()
  } finally {
    loading.value = false
  }
}

function resetForm() {
  scannedAsn.value = null
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

const asnQty = computed(() => scannedAsn.value?.quantity || 0)
const hasDiscrepancy = computed(() => scannedAsn.value && receiveForm.receivedQty !== asnQty.value)
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
      <div v-if="scannedAsn" v-loading="loading" class="scan-result">
        <div class="result-header">
          <div class="result-title">送货单信息</div>
          <StatusTag :value="scannedAsn.status" />
        </div>

        <div class="result-grid">
          <div class="result-item">
            <span class="result-label">ASN号</span>
            <span class="result-value">{{ scannedAsn.asnNo }}</span>
          </div>
          <div class="result-item">
            <span class="result-label">订单号</span>
            <span class="result-value">{{ scannedAsn.orderNo }}</span>
          </div>
          <div class="result-item">
            <span class="result-label">供应商</span>
            <span class="result-value">{{ scannedAsn.supplierName }}</span>
          </div>
          <div class="result-item">
            <span class="result-label">应发数量</span>
            <span class="result-value">{{ scannedAsn.quantity }}</span>
          </div>
          <div class="result-item">
            <span class="result-label">仓库</span>
            <span class="result-value">{{ scannedAsn.warehouse }}</span>
          </div>
          <div class="result-item">
            <span class="result-label">预计到货</span>
            <span class="result-value">{{ scannedAsn.eta }}</span>
          </div>
        </div>

        <el-divider />

        <div class="receive-form">
          <div class="result-title">收货确认</div>
          <el-form :model="receiveForm" label-width="100px" style="max-width: 560px">
            <el-form-item label="实收数量" required>
              <el-input-number v-model="receiveForm.receivedQty" :min="0" style="width: 200px" />
              <span v-if="hasDiscrepancy" class="diff-warning">
                {{ receiveForm.receivedQty < asnQty ? '少收' : '多收' }}
                {{ Math.abs(receiveForm.receivedQty - asnQty) }}
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
                <el-input-number v-model="receiveForm.discrepantQty" :min="0" :value="Math.abs(receiveForm.receivedQty - asnQty)" disabled style="width: 200px" />
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

      <el-empty v-if="!scannedAsn && !loading" description="扫描或输入条码查询待收货的送货单" :image-size="120" />
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