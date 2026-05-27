<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { vmiApi, forecastApi } from '@/api/inventory'
import { supplierApi } from '@/api/supplier'
import PageContainer from '@/components/common/PageContainer.vue'
import StatusTag from '@/components/business/StatusTag.vue'
import SupplierSelector from '@/components/business/SupplierSelector.vue'

// ---- tab 控制 ----
const activeTab = ref('vmi')

watch(activeTab, () => {
  if (activeTab.value === 'vmi') loadVmi()
  else if (activeTab.value === 'forecast') loadForecasts()
})

// 供应商名称缓存
const supplierNames = ref<Record<number, string>>({})
const ensureSupplierNames = async (ids: number[]) => {
  const unseen = ids.filter(id => id && !supplierNames.value[id])
  if (unseen.length === 0) return
  for (const id of unseen) {
    try {
      const s = await supplierApi.detail(id)
      supplierNames.value[id] = s.name
    } catch {
      supplierNames.value[id] = `#${id}`
    }
  }
}
const getSupplierName = (id: number) => supplierNames.value[id] || String(id)

// ==================== VMI库存 ====================
const vmiLoading = ref(false)
const vmiRecords = ref<any[]>([])
const vmiTotal = ref(0)
const vmiQuery = reactive({ pageNum: 1, pageSize: 10, keyword: '' })

const loadVmi = async () => {
  vmiLoading.value = true
  try {
    const params: Record<string, unknown> = { pageNum: vmiQuery.pageNum, pageSize: vmiQuery.pageSize }
    if (vmiQuery.keyword) params.keyword = vmiQuery.keyword
    const res = await vmiApi.page(params)
    vmiRecords.value = res.records
    vmiTotal.value = res.total
    if (res.total === 0) vmiQuery.pageNum = 1
    ensureSupplierNames(res.records.map((r: any) => r.supplierId).filter(Boolean))
  } finally { vmiLoading.value = false }
}

const resetVmiQuery = () => {
  vmiQuery.keyword = ''
  loadVmi()
}

// 同步库存弹窗
const showSyncDialog = ref(false)
const syncForm = reactive({
  supplierId: null as number | null, materialCode: '',
  warehouseId: null as number | null, warehouseName: '',
  onhandQty: 0, availableQty: 0, safetyQty: 0, maxQty: 0,
})

const openSyncDialog = () => {
  syncForm.supplierId = null
  syncForm.materialCode = ''
  syncForm.warehouseId = null
  syncForm.warehouseName = ''
  syncForm.onhandQty = 0
  syncForm.availableQty = 0
  syncForm.safetyQty = 0
  syncForm.maxQty = 0
  showSyncDialog.value = true
}

const submitSync = async () => {
  if (!syncForm.supplierId) { ElMessage.warning('请选择供应商'); return }
  if (!syncForm.materialCode) { ElMessage.warning('请填写物料编码'); return }
  try {
    await vmiApi.sync(syncForm as any)
    ElMessage.success('库存同步成功')
    showSyncDialog.value = false
    loadVmi()
  } catch { /* */ }
}

// ==================== 需求预测 ====================
const forecastLoading = ref(false)
const forecastRecords = ref<any[]>([])
const forecastTotal = ref(0)
const forecastQuery = reactive({ pageNum: 1, pageSize: 10, keyword: '' })

const loadForecasts = async () => {
  forecastLoading.value = true
  try {
    const params: Record<string, unknown> = { pageNum: forecastQuery.pageNum, pageSize: forecastQuery.pageSize }
    if (forecastQuery.keyword) params.keyword = forecastQuery.keyword
    const res = await forecastApi.page(params)
    forecastRecords.value = res.records
    forecastTotal.value = res.total
    if (res.total === 0) forecastQuery.pageNum = 1
    ensureSupplierNames(res.records.map((r: any) => r.supplierId).filter(Boolean))
  } finally { forecastLoading.value = false }
}

const resetForecastQuery = () => {
  forecastQuery.keyword = ''
  loadForecasts()
}

const demandTypeMap: Record<number, string> = { 1: '预测', 2: 'JIT', 3: 'VMI补货' }

// 创建预测弹窗
const showForecastCreate = ref(false)
const forecastCreateForm = reactive({
  supplierId: null as number | null, materialCode: '',
  demandDate: '', demandQty: 0, demandType: 1,
})

const openForecastCreate = () => {
  forecastCreateForm.supplierId = null
  forecastCreateForm.materialCode = ''
  forecastCreateForm.demandDate = ''
  forecastCreateForm.demandQty = 0
  forecastCreateForm.demandType = 1
  showForecastCreate.value = true
}

const submitForecastCreate = async () => {
  if (!forecastCreateForm.supplierId) { ElMessage.warning('请选择供应商'); return }
  try {
    await forecastApi.create(forecastCreateForm as any)
    ElMessage.success('需求预测创建成功')
    showForecastCreate.value = false
    loadForecasts()
  } catch { /* */ }
}

const handlePublish = async (row: any) => {
  try {
    await forecastApi.publish(row.id)
    ElMessage.success('已发布')
    loadForecasts()
  } catch { /* */ }
}

const handleRespond = async (row: any) => {
  try {
    await forecastApi.respond(row.id)
    ElMessage.success('已响应')
    loadForecasts()
  } catch { /* */ }
}

const handleClose = async (row: any) => {
  try {
    await forecastApi.close(row.id)
    ElMessage.success('已关闭')
    loadForecasts()
  } catch { /* */ }
}

onMounted(loadVmi)
</script>

<template>
  <PageContainer title="VMI库存与需求预测" subtitle="VMI库存监控与需求预测管理">
    <el-tabs v-model="activeTab" type="border-card">
      <!-- ========== VMI库存 Tab ========== -->
      <el-tab-pane name="vmi">
        <template #label>
          <span class="tab-label">VMI库存</span>
        </template>
        <div v-if="activeTab === 'vmi'">
          <div class="search-panel">
            <el-form inline :model="vmiQuery" @submit.prevent="loadVmi">
              <el-form-item label="关键词">
                <el-input v-model="vmiQuery.keyword" placeholder="物料编码/仓库" clearable @clear="loadVmi" @keyup.enter="loadVmi" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="loadVmi">查询</el-button>
                <el-button @click="resetVmiQuery">重置</el-button>
                <el-button type="success" @click="openSyncDialog">同步库存</el-button>
              </el-form-item>
            </el-form>
          </div>
          <div class="card-table">
            <el-table v-loading="vmiLoading" :data="vmiRecords" border highlight-current-row>
              <el-table-column prop="materialCode" label="物料编码" width="140" />
              <el-table-column prop="warehouseName" label="仓库" width="140" />
              <el-table-column label="供应商" min-width="160">
                <template #default="{ row }">{{ getSupplierName(row.supplierId) }}</template>
              </el-table-column>
              <el-table-column prop="onhandQty" label="在手量" width="100" />
              <el-table-column prop="availableQty" label="可用量" width="100" />
              <el-table-column prop="safetyQty" label="安全库存" width="100" />
              <el-table-column prop="maxQty" label="最大库存" width="100" />
              <el-table-column label="状态" width="100">
                <template #default="{ row }"><StatusTag :value="row.inventoryStatus" prefix="VMI" /></template>
              </el-table-column>
              <el-table-column prop="lastSyncTime" label="最后同步" width="160" />
            </el-table>
          </div>
          <el-pagination
            v-model:current-page="vmiQuery.pageNum" v-model:page-size="vmiQuery.pageSize"
            :total="vmiTotal" layout="total, prev, pager, next, sizes" class="mt-4"
            @current-change="() => { if (!vmiLoading) loadVmi() }" @size-change="() => { if (!vmiLoading) loadVmi() }"
          />
        </div>
      </el-tab-pane>

      <!-- ========== 需求预测 Tab ========== -->
      <el-tab-pane name="forecast">
        <template #label>
          <span class="tab-label">需求预测</span>
        </template>
        <div v-if="activeTab === 'forecast'">
          <div class="search-panel">
            <el-form inline :model="forecastQuery" @submit.prevent="loadForecasts">
              <el-form-item label="关键词">
                <el-input v-model="forecastQuery.keyword" placeholder="需求单号/物料" clearable @clear="loadForecasts" @keyup.enter="loadForecasts" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="loadForecasts">查询</el-button>
                <el-button @click="resetForecastQuery">重置</el-button>
                <el-button type="success" @click="openForecastCreate">创建预测</el-button>
              </el-form-item>
            </el-form>
          </div>
          <div class="card-table">
            <el-table v-loading="forecastLoading" :data="forecastRecords" border highlight-current-row>
              <el-table-column prop="demandNo" label="需求单号" width="160" />
              <el-table-column label="供应商" min-width="160">
                <template #default="{ row }">{{ getSupplierName(row.supplierId) }}</template>
              </el-table-column>
              <el-table-column prop="materialCode" label="物料编码" width="140" />
              <el-table-column prop="demandDate" label="需求日期" width="120" />
              <el-table-column prop="demandQty" label="需求数量" width="100" />
              <el-table-column label="需求类型" width="110">
                <template #default="{ row }">{{ demandTypeMap[row.demandType] || row.demandType }}</template>
              </el-table-column>
              <el-table-column label="状态" width="100">
                <template #default="{ row }"><StatusTag :value="row.demandStatus" prefix="预测" /></template>
              </el-table-column>
              <el-table-column label="操作" width="200" fixed="right">
                <template #default="{ row }">
                  <el-button v-if="row.demandStatus === 0" link type="warning" @click="handlePublish(row)">发布</el-button>
                  <el-button v-if="row.demandStatus === 1" link type="primary" @click="handleRespond(row)">响应</el-button>
                  <el-button v-if="row.demandStatus !== 3" link type="danger" @click="handleClose(row)">关闭</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
          <el-pagination
            v-model:current-page="forecastQuery.pageNum" v-model:page-size="forecastQuery.pageSize"
            :total="forecastTotal" layout="total, prev, pager, next, sizes" class="mt-4"
            @current-change="() => { if (!forecastLoading) loadForecasts() }" @size-change="() => { if (!forecastLoading) loadForecasts() }"
          />
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- VMI库存同步弹窗 -->
    <el-dialog v-model="showSyncDialog" title="同步库存" width="550px" :close-on-click-modal="false">
      <el-form :model="syncForm" label-width="100px">
        <el-form-item label="供应商" required>
          <SupplierSelector v-model="syncForm.supplierId" />
        </el-form-item>
        <el-form-item label="物料编码" required>
          <el-input v-model="syncForm.materialCode" placeholder="请输入物料编码" />
        </el-form-item>
        <el-form-item label="仓库ID">
          <el-input-number v-model="syncForm.warehouseId" :min="1" style="width:100%" />
        </el-form-item>
        <el-form-item label="仓库名称">
          <el-input v-model="syncForm.warehouseName" placeholder="仓库名称" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="在手量">
              <el-input-number v-model="syncForm.onhandQty" :min="0" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="可用量">
              <el-input-number v-model="syncForm.availableQty" :min="0" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="安全库存">
              <el-input-number v-model="syncForm.safetyQty" :min="0" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最大库存">
              <el-input-number v-model="syncForm.maxQty" :min="0" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="showSyncDialog = false">取消</el-button>
        <el-button type="primary" @click="submitSync">确认同步</el-button>
      </template>
    </el-dialog>

    <!-- 创建需求预测弹窗 -->
    <el-dialog v-model="showForecastCreate" title="创建需求预测" width="550px" :close-on-click-modal="false">
      <el-form :model="forecastCreateForm" label-width="100px">
        <el-form-item label="供应商" required>
          <SupplierSelector v-model="forecastCreateForm.supplierId" />
        </el-form-item>
        <el-form-item label="物料编码" required>
          <el-input v-model="forecastCreateForm.materialCode" placeholder="物料编码" />
        </el-form-item>
        <el-form-item label="需求日期">
          <el-date-picker v-model="forecastCreateForm.demandDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="需求数量">
          <el-input-number v-model="forecastCreateForm.demandQty" :min="0" style="width:100%" />
        </el-form-item>
        <el-form-item label="需求类型">
          <el-select v-model="forecastCreateForm.demandType" style="width:100%">
            <el-option label="预测" :value="1" />
            <el-option label="JIT" :value="2" />
            <el-option label="VMI补货" :value="3" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showForecastCreate = false">取消</el-button>
        <el-button type="primary" @click="submitForecastCreate">确认</el-button>
      </template>
    </el-dialog>
  </PageContainer>
</template>

<style scoped>
.tab-label {
  display: flex;
  align-items: center;
  gap: 6px;
}

.search-panel {
  background: #ffffff;
  padding: 16px;
  border-radius: 12px;
  margin-bottom: 16px;
}

.card-table {
  background: #ffffff;
  border-radius: 12px;
  padding: 16px;
}
</style>