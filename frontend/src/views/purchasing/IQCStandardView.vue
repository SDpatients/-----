<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { inspectionStandardApi } from '@/api/quality'
import PageContainer from '@/components/common/PageContainer.vue'
import StatusTag from '@/components/business/StatusTag.vue'
import ExportDialog from '@/components/business/ExportDialog.vue'
import type { InspectionStandard } from '@/types/business'

const loading = ref(false)
const records = ref<InspectionStandard[]>([])
const total = ref(0)
const exportVisible = ref(false)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '', status: undefined as number | undefined })

const loadData = async () => {
  loading.value = true
  try {
    const params: Record<string, unknown> = { pageNum: query.pageNum, pageSize: query.pageSize }
    if (query.keyword) params.keyword = query.keyword
    if (query.status !== undefined && query.status !== null) params.status = query.status
    const result = await inspectionStandardApi.page(params as any)
    records.value = result.records
    total.value = result.total
    if (result.total === 0) query.pageNum = 1
  } finally { loading.value = false }
}

const resetQuery = () => { query.keyword = ''; query.status = undefined; loadData() }

// ========== 新增/编辑 ==========
const dialogVisible = ref(false)
const dialogTitle = ref('新建检验标准')
const editingId = ref<number | string | null>(null)
const formRef = ref()
const form = reactive<Partial<InspectionStandard>>({
  standardName: '', materialCode: '', materialName: '', checkItem: '', checkMethod: '',
  sampleRule: '', sampleQty: 0, acValue: 0, reValue: 0,
  lowerLimit: '', upperLimit: '', unit: '', severity: '一般',
})

const formRules = reactive({
  standardName: [{ required: true, message: '标准名称不能为空', trigger: 'blur' }],
  materialCode: [{ required: true, message: '物料编码不能为空', trigger: 'blur' }],
  checkItem: [{ required: true, message: '检验项目不能为空', trigger: 'blur' }],
  sampleRule: [{ required: true, message: '抽样规则不能为空', trigger: 'change' }],
})

const openCreate = () => {
  editingId.value = null
  dialogTitle.value = '新建检验标准'
  Object.assign(form, {
    standardName: '', materialCode: '', materialName: '', checkItem: '', checkMethod: '',
    sampleRule: '', sampleQty: 0, acValue: 0, reValue: 0,
    lowerLimit: '', upperLimit: '', unit: '', severity: '一般',
  })
  dialogVisible.value = true
}

const openEdit = (row: InspectionStandard) => {
  editingId.value = row.id
  dialogTitle.value = '编辑检验标准'
  Object.assign(form, {
    standardName: row.standardName, materialCode: row.materialCode, materialName: row.materialName,
    checkItem: row.checkItem, checkMethod: row.checkMethod || '',
    sampleRule: row.sampleRule, sampleQty: row.sampleQty,
    acValue: row.acValue, reValue: row.reValue,
    lowerLimit: row.lowerLimit, upperLimit: row.upperLimit,
    unit: row.unit, severity: row.severity,
  })
  dialogVisible.value = true
}

const submitForm = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  try {
    if (editingId.value) {
      await inspectionStandardApi.update(editingId.value, form)
      ElMessage.success('标准已更新')
    } else {
      await inspectionStandardApi.create(form)
      ElMessage.success('标准已创建')
    }
    dialogVisible.value = false
    loadData()
  } catch { /* handled */ }
}

const handleDelete = async (row: InspectionStandard) => {
  try {
    await ElMessageBox.confirm(`确认删除标准「${row.standardNo}」？`, '删除确认', { type: 'warning' })
    await inspectionStandardApi.delete(row.id)
    ElMessage.success('已删除')
    loadData()
  } catch { /* cancel */ }
}

const handleToggle = async (row: InspectionStandard) => {
  const action = row.status === 1 ? '停用' : '启用'
  try {
    await ElMessageBox.confirm(`确认${action}标准「${row.standardNo}」？`, `${action}确认`)
    await inspectionStandardApi.toggleStatus(row.id)
    ElMessage.success(`已${action}`)
    loadData()
  } catch { /* cancel */ }
}

onMounted(loadData)
</script>

<template>
  <PageContainer title="IQC 检验标准" subtitle="维护来料检验的检验项目、抽样规则与判定标准">
    <template #actions>
      <el-button type="primary" @click="openCreate">新建标准</el-button>
      <el-button @click="exportVisible = true">导出</el-button>
    </template>

    <div class="search-panel">
      <el-form inline :model="query" @submit.prevent="loadData">
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="物料编码/检验项目" clearable @clear="loadData" @keyup.enter="loadData" style="width: 220px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 140px" @change="loadData">
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-table v-loading="loading" :data="records" border highlight-current-row>
      <el-table-column prop="standardNo" label="标准编号" width="150" />
      <el-table-column prop="standardName" label="标准名称" min-width="160" show-overflow-tooltip />
      <el-table-column prop="materialCode" label="物料编码" width="120" />
      <el-table-column prop="materialName" label="物料名称" min-width="140" show-overflow-tooltip />
      <el-table-column prop="checkItem" label="检验项目" min-width="150" show-overflow-tooltip />
      <el-table-column prop="sampleRule" label="抽样规则" width="120" />
      <el-table-column label="样本量" width="90"><template #default="{ row }">{{ row.sampleQty }}</template></el-table-column>
      <el-table-column label="AC/RE" width="100">
        <template #default="{ row }">{{ row.acValue }} / {{ row.reValue }}</template>
      </el-table-column>
      <el-table-column label="规格范围" width="180">
        <template #default="{ row }">{{ row.lowerLimit || '-' }} ~ {{ row.upperLimit || '-' }} {{ row.unit }}</template>
      </el-table-column>
      <el-table-column label="严重度" width="90">
        <template #default="{ row }"><StatusTag :value="row.severity" kind="risk" /></template>
      </el-table-column>
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="updateTime" label="更新时间" width="160" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link :type="row.status === 1 ? 'warning' : 'success'" @click="handleToggle(row)">
            {{ row.status === 1 ? '停用' : '启用' }}
          </el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
      :total="total" layout="total, prev, pager, next, sizes" class="mt-4"
      @current-change="() => { if (!loading) loadData() }" @size-change="() => { if (!loading) loadData() }"
    />

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="680px" destroy-on-close :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="标准名称" prop="standardName">
              <el-input v-model="form.standardName" placeholder="如：检验标准-123" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="物料编码" prop="materialCode">
              <el-input v-model="form.materialCode" placeholder="请输入物料编码" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="物料名称">
              <el-input v-model="form.materialName" placeholder="请输入物料名称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="检验项目" prop="checkItem">
              <el-input v-model="form.checkItem" placeholder="如：外观检查、尺寸公差" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="检验方法">
              <el-input v-model="form.checkMethod" placeholder="如：目视、卡尺、硬度计" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="抽样规则" prop="sampleRule">
              <el-select v-model="form.sampleRule" style="width: 100%" placeholder="请选择">
                <el-option label="GB/T 2828.1 一般检验水平II" value="GB/T 2828.1-II" />
                <el-option label="GB/T 2828.1 特殊检验水平S-3" value="GB/T 2828.1-S3" />
                <el-option label="全检 (100%)" value="全检" />
                <el-option label="固定抽样" value="固定抽样" />
                <el-option label="免检" value="免检" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="样本量">
              <el-input-number v-model="form.sampleQty" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="AC值">
              <el-input-number v-model="form.acValue" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="RE值">
              <el-input-number v-model="form.reValue" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="严重度">
              <el-select v-model="form.severity" style="width: 100%">
                <el-option label="关键" value="关键" />
                <el-option label="重要" value="重要" />
                <el-option label="一般" value="一般" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="下限">
              <el-input v-model="form.lowerLimit" placeholder="规格下限" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="上限">
              <el-input v-model="form.upperLimit" placeholder="规格上限" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="单位">
              <el-input v-model="form.unit" placeholder="mm/%/HB" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>

    <ExportDialog v-model="exportVisible" />
  </PageContainer>
</template>