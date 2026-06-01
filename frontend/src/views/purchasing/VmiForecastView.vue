<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { vmiApi, forecastApi, type VmiInventoryQuery, type VmiInventorySyncDTO } from '@/api/inventory'
import { supplierApi } from '@/api/supplier'
import { materialApi } from '@/api/material'
import { toSupplier } from '@/api/adapters'
import PageContainer from '@/components/common/PageContainer.vue'
import StatusTag from '@/components/business/StatusTag.vue'
import type { Supplier } from '@/types/business'

const activeTab = ref('vmi')

watch(activeTab, () => {
  if (activeTab.value === 'vmi') loadVmi()
  else if (activeTab.value === 'forecast') loadForecasts()
})

/* ==================== 供应商名称缓存 ==================== */
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

/* ==================== VMI库存 ==================== */
const vmiLoading = ref(false)
const vmiRecords = ref<any[]>([])
const vmiTotal = ref(0)
const vmiQuery = reactive<VmiInventoryQuery>({ pageNum: 1, pageSize: 10, keyword: '' })

const loadVmi = async () => {
  vmiLoading.value = true
  try {
    const res = await vmiApi.page(vmiQuery)
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

/* ==================== 供应商选择弹窗（单选用） ==================== */
const supplierDialogVisible = ref(false)
const supplierLoading = ref(false)
const supplierList = ref<Supplier[]>([])
const supplierTotal = ref(0)
const supplierQuery = reactive({ pageNum: 1, pageSize: 10, keyword: '' })
const tempSelectedSupplier = ref<Supplier | null>(null)
type SupplierDialogTarget = 'sync' | 'forecast'
let supplierTarget: SupplierDialogTarget = 'sync'

const openSupplierDialog = (target: SupplierDialogTarget) => {
  supplierTarget = target
  tempSelectedSupplier.value = null
  supplierQuery.pageNum = 1
  supplierQuery.keyword = ''
  loadSupplierList()
  supplierDialogVisible.value = true
}

const loadSupplierList = async () => {
  supplierLoading.value = true
  try {
    const result = await supplierApi.page({
      pageNum: supplierQuery.pageNum,
      pageSize: supplierQuery.pageSize,
      keyword: supplierQuery.keyword || undefined,
    } as any)
    supplierList.value = result.records.map(toSupplier)
    supplierTotal.value = result.total
  } finally { supplierLoading.value = false }
}

const searchSupplier = () => { supplierQuery.pageNum = 1; loadSupplierList() }
const resetSupplierQuery = () => { supplierQuery.keyword = ''; supplierQuery.pageNum = 1; loadSupplierList() }
const onSupplierPageChange = () => { loadSupplierList() }
const onSupplierPageSizeChange = () => { supplierQuery.pageNum = 1; loadSupplierList() }

const isSupplierSelected = (row: Supplier) => tempSelectedSupplier.value?.id === row.id
const toggleSupplierSelection = (row: Supplier) => {
  tempSelectedSupplier.value = isSupplierSelected(row) ? null : row
}

const confirmSupplierSelection = () => {
  if (!tempSelectedSupplier.value) { ElMessage.warning('请选择一个供应商'); return }
  if (supplierTarget === 'sync') {
    syncForm.supplierId = Number(tempSelectedSupplier.value.id)
  } else {
    forecastCreateForm.supplierId = Number(tempSelectedSupplier.value.id)
  }
  supplierDialogVisible.value = false
}

/* ==================== 物料选择弹窗（单选用） ==================== */
const materialDialogVisible = ref(false)
const materialLoading = ref(false)
const materialList = ref<any[]>([])
const materialTotal = ref(0)
const materialQuery = reactive({ pageNum: 1, pageSize: 10, keyword: '' })
const tempSelectedMaterial = ref<any>(null)
type MaterialDialogTarget = 'sync' | 'forecast'
let materialTarget: MaterialDialogTarget = 'sync'

const openMaterialDialog = (target: MaterialDialogTarget) => {
  materialTarget = target
  tempSelectedMaterial.value = null
  materialQuery.pageNum = 1
  materialQuery.keyword = ''
  loadMaterialList()
  materialDialogVisible.value = true
}

const loadMaterialList = async () => {
  materialLoading.value = true
  try {
    const result = await materialApi.page({
      pageNum: materialQuery.pageNum,
      pageSize: materialQuery.pageSize,
      keyword: materialQuery.keyword || undefined,
    })
    materialList.value = result.records
    materialTotal.value = result.total
  } finally { materialLoading.value = false }
}

const searchMaterial = () => { materialQuery.pageNum = 1; loadMaterialList() }
const resetMaterialQuery = () => { materialQuery.keyword = ''; materialQuery.pageNum = 1; loadMaterialList() }
const onMaterialPageChange = () => { loadMaterialList() }
const onMaterialPageSizeChange = () => { materialQuery.pageNum = 1; loadMaterialList() }

const isMaterialSelected = (row: any) => tempSelectedMaterial.value?.id === row.id
const toggleMaterialSelection = (row: any) => {
  tempSelectedMaterial.value = isMaterialSelected(row) ? null : row
}

const confirmMaterialSelection = () => {
  if (!tempSelectedMaterial.value) { ElMessage.warning('请选择一个物料'); return }
  if (materialTarget === 'sync') {
    syncForm.materialCode = tempSelectedMaterial.value.code || ''
  } else {
    forecastCreateForm.materialCode = tempSelectedMaterial.value.code || ''
  }
  materialDialogVisible.value = false
}

/* ==================== 同步库存弹窗 ==================== */
const syncFormRef = ref<FormInstance>()
const syncFormRules: FormRules = {
  supplierId: [{ required: true, message: '请选择供应商', trigger: 'change' }],
  materialCode: [{ required: true, message: '物料编码不能为空', trigger: 'blur' }],
}
const showSyncDialog = ref(false)
const syncForm = reactive<VmiInventorySyncDTO>({
  supplierId: 0, materialCode: '',
  warehouseId: undefined, warehouseName: undefined,
  onhandQty: 0, availableQty: 0, safetyQty: undefined, maxQty: undefined,
})

const openSyncDialog = () => {
  syncForm.supplierId = 0
  syncForm.materialCode = ''
  syncForm.warehouseId = undefined
  syncForm.warehouseName = undefined
  syncForm.onhandQty = 0
  syncForm.availableQty = 0
  syncForm.safetyQty = undefined
  syncForm.maxQty = undefined
  showSyncDialog.value = true
}

const submitSync = async () => {
  if (!await syncFormRef.value?.validate()) return
  try {
    await vmiApi.sync(syncForm)
    ElMessage.success('库存同步成功')
    showSyncDialog.value = false
    loadVmi()
  } catch { /* */ }
}

/* ==================== 需求预测 ==================== */
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

const forecastFormRef = ref<FormInstance>()
const forecastFormRules: FormRules = {
  supplierId: [{ required: true, message: '请选择供应商', trigger: 'change' }],
  materialCode: [{ required: true, message: '物料编码不能为空', trigger: 'blur' }],
  demandDate: [{ required: true, message: '需求日期不能为空', trigger: 'change' }],
  demandQty: [{ required: true, message: '需求数量不能为空', trigger: 'blur' }],
}
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
  if (!await forecastFormRef.value?.validate()) return
  try {
    await forecastApi.create(forecastCreateForm as any)
    ElMessage.success('需求预测创建成功')
    showForecastCreate.value = false
    loadForecasts()
  } catch { /* */ }
}

const handlePublish = async (row: any) => {
  try { await forecastApi.publish(row.id); ElMessage.success('已发布'); loadForecasts() } catch { /* */ }
}
const handleRespond = async (row: any) => {
  try { await forecastApi.respond(row.id); ElMessage.success('已响应'); loadForecasts() } catch { /* */ }
}
const handleClose = async (row: any) => {
  try { await forecastApi.close(row.id); ElMessage.success('已关闭'); loadForecasts() } catch { /* */ }
}

onMounted(loadVmi)
</script>

<template>
  <PageContainer title="VMI库存与需求预测" subtitle="VMI库存监控与需求预测管理">
    <el-tabs v-model="activeTab" type="border-card">
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
      <el-form ref="syncFormRef" :model="syncForm" :rules="syncFormRules" label-width="100px">
        <el-form-item label="供应商" prop="supplierId">
          <div class="select-area">
            <el-button type="primary" plain @click="openSupplierDialog('sync')">
              <el-icon style="margin-right: 4px"><svg viewBox="0 0 1024 1024" width="1em" height="1em"><path d="M512 64a448 448 0 110 896 448 448 0 010-896z m0 64a384 384 0 100 768 384 384 0 000-768z m-42.667 213.333h85.334v170.667h170.666v85.333h-170.666v170.667h-85.334V597.333H298.667V512h170.666V341.333z" fill="currentColor"/></svg></el-icon>
              选择供应商
            </el-button>
            <span v-if="!syncForm.supplierId" class="select-hint">点击从供应商库选择</span>
            <el-tag v-else type="success" closable @close="syncForm.supplierId = null">已选</el-tag>
          </div>
        </el-form-item>
        <el-form-item label="物料" prop="materialCode">
          <div class="select-area">
            <el-button type="success" plain @click="openMaterialDialog('sync')">
              <el-icon style="margin-right: 4px"><svg viewBox="0 0 1024 1024" width="1em" height="1em"><path d="M512 64a448 448 0 110 896 448 448 0 010-896z m0 64a384 384 0 100 768 384 384 0 000-768z m-42.667 213.333h85.334v170.667h170.666v85.333h-170.666v170.667h-85.334V597.333H298.667V512h170.666V341.333z" fill="currentColor"/></svg></el-icon>
              选择物料
            </el-button>
            <span v-if="!syncForm.materialCode" class="select-hint">点击从物料库选择</span>
            <el-tag v-else type="success" closable @close="syncForm.materialCode = ''">{{ syncForm.materialCode }}</el-tag>
          </div>
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
      <el-form ref="forecastFormRef" :model="forecastCreateForm" :rules="forecastFormRules" label-width="100px">
        <el-form-item label="供应商" prop="supplierId">
          <div class="select-area">
            <el-button type="primary" plain @click="openSupplierDialog('forecast')">
              <el-icon style="margin-right: 4px"><svg viewBox="0 0 1024 1024" width="1em" height="1em"><path d="M512 64a448 448 0 110 896 448 448 0 010-896z m0 64a384 384 0 100 768 384 384 0 000-768z m-42.667 213.333h85.334v170.667h170.666v85.333h-170.666v170.667h-85.334V597.333H298.667V512h170.666V341.333z" fill="currentColor"/></svg></el-icon>
              选择供应商
            </el-button>
            <span v-if="!forecastCreateForm.supplierId" class="select-hint">点击从供应商库选择</span>
            <el-tag v-else type="success" closable @close="forecastCreateForm.supplierId = null">已选</el-tag>
          </div>
        </el-form-item>
        <el-form-item label="物料" prop="materialCode">
          <div class="select-area">
            <el-button type="success" plain @click="openMaterialDialog('forecast')">
              <el-icon style="margin-right: 4px"><svg viewBox="0 0 1024 1024" width="1em" height="1em"><path d="M512 64a448 448 0 110 896 448 448 0 010-896z m0 64a384 384 0 100 768 384 384 0 000-768z m-42.667 213.333h85.334v170.667h170.666v85.333h-170.666v170.667h-85.334V597.333H298.667V512h170.666V341.333z" fill="currentColor"/></svg></el-icon>
              选择物料
            </el-button>
            <span v-if="!forecastCreateForm.materialCode" class="select-hint">点击从物料库选择</span>
            <el-tag v-else type="success" closable @close="forecastCreateForm.materialCode = ''">{{ forecastCreateForm.materialCode }}</el-tag>
          </div>
        </el-form-item>
        <el-form-item label="需求日期" prop="demandDate">
          <el-date-picker v-model="forecastCreateForm.demandDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="需求数量" prop="demandQty">
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

    <!-- 供应商选择弹窗 -->
    <el-dialog v-model="supplierDialogVisible" title="选择供应商" width="860px" :close-on-click-modal="false">
      <div class="search-panel">
        <el-form inline :model="supplierQuery" @submit.prevent="searchSupplier">
          <el-form-item label="关键词">
            <el-input v-model="supplierQuery.keyword" placeholder="供应商名称/编码" clearable @clear="resetSupplierQuery" @keyup.enter="searchSupplier" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="searchSupplier">查询</el-button>
            <el-button @click="resetSupplierQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
      <el-table
        v-loading="supplierLoading"
        :data="supplierList"
        border highlight-current-row
        @row-click="toggleSupplierSelection"
        row-key="id" max-height="420"
      >
        <el-table-column width="55" align="center">
          <template #default="{ row }">
            <el-radio :model-value="isSupplierSelected(row)" @click.stop />
          </template>
        </el-table-column>
        <el-table-column prop="code" label="供应商编码" width="140" />
        <el-table-column prop="name" label="供应商名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="category" label="类别" width="120" />
        <el-table-column prop="level" label="等级" width="100" />
        <el-table-column prop="contact" label="联系人" width="100" />
        <el-table-column prop="phone" label="电话" width="130" />
      </el-table>
      <el-pagination
        v-model:current-page="supplierQuery.pageNum" v-model:page-size="supplierQuery.pageSize"
        :total="supplierTotal" layout="total, prev, pager, next, sizes" class="mt-4"
        @current-change="onSupplierPageChange" @size-change="onSupplierPageSizeChange"
      />
      <div class="dialog-selection-info">
        <span v-if="tempSelectedSupplier">
          已选择: <strong>{{ tempSelectedSupplier.name }}</strong>（{{ tempSelectedSupplier.code }}）
        </span>
        <span v-else class="no-selection">点击行选择一个供应商</span>
      </div>
      <template #footer>
        <el-button @click="supplierDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmSupplierSelection">确认选择</el-button>
      </template>
    </el-dialog>

    <!-- 物料选择弹窗 -->
    <el-dialog v-model="materialDialogVisible" title="从物料库选择物料" width="860px" :close-on-click-modal="false">
      <div class="search-panel">
        <el-form inline :model="materialQuery" @submit.prevent="searchMaterial">
          <el-form-item label="关键词">
            <el-input v-model="materialQuery.keyword" placeholder="物料编码/名称" clearable @clear="resetMaterialQuery" @keyup.enter="searchMaterial" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="searchMaterial">查询</el-button>
            <el-button @click="resetMaterialQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
      <el-table
        v-loading="materialLoading"
        :data="materialList"
        border highlight-current-row
        @row-click="toggleMaterialSelection"
        row-key="id" max-height="420"
      >
        <el-table-column width="55" align="center">
          <template #default="{ row }">
            <el-radio :model-value="isMaterialSelected(row)" @click.stop />
          </template>
        </el-table-column>
        <el-table-column prop="code" label="物料编码" width="140" />
        <el-table-column prop="name" label="物料名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="spec" label="规格" width="130" show-overflow-tooltip />
        <el-table-column prop="unit" label="单位" width="80" />
        <el-table-column prop="category" label="分类" width="120" show-overflow-tooltip />
      </el-table>
      <el-pagination
        v-model:current-page="materialQuery.pageNum" v-model:page-size="materialQuery.pageSize"
        :total="materialTotal" layout="total, prev, pager, next, sizes" class="mt-4"
        @current-change="onMaterialPageChange" @size-change="onMaterialPageSizeChange"
      />
      <div class="dialog-selection-info">
        <span v-if="tempSelectedMaterial">
          已选择: <strong>{{ tempSelectedMaterial.name }}</strong>（{{ tempSelectedMaterial.code }}）
        </span>
        <span v-else class="no-selection">点击行选择一个物料</span>
      </div>
      <template #footer>
        <el-button @click="materialDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmMaterialSelection">确认选择</el-button>
      </template>
    </el-dialog>
  </PageContainer>
</template>

<style scoped>
.tab-label { display: flex; align-items: center; gap: 6px; }
.search-panel { background: #ffffff; padding: 16px; border-radius: 12px; margin-bottom: 16px; }
.card-table { background: #ffffff; border-radius: 12px; padding: 16px; }
.select-area { display: flex; flex-wrap: wrap; align-items: center; gap: 8px; width: 100%; }
.select-hint { color: #909399; font-size: 13px; }
.dialog-selection-info { margin-top: 10px; font-size: 13px; color: #606266; }
.dialog-selection-info .no-selection { color: #909399; }
</style>