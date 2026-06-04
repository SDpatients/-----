<script setup lang="ts">
import { onMounted, reactive, ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { materialApi } from '@/api/material'
import { useDictStore } from '@/stores/dict'
import PageContainer from '@/components/common/PageContainer.vue'
import ExportDialog from '@/components/business/ExportDialog.vue'
import type { Material } from '@/types/business'
import dayjs from 'dayjs'

const dictStore = useDictStore()
const DICT_CODE = 'material_category'
const categoryOptions = computed(() => dictStore.getItems(DICT_CODE))

const loading = ref(false)
const records = ref<Material[]>([])
const total = ref(0)
const exportVisible = ref(false)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '', category: '' })

const loadData = async () => {
  loading.value = true
  try {
    const result = await materialApi.page({
      pageNum: query.pageNum,
      pageSize: query.pageSize,
      keyword: query.keyword || undefined,
      category: query.category || undefined,
    })
    records.value = result.records
    total.value = result.total
    if (result.total === 0) query.pageNum = 1
  } finally { loading.value = false }
}

const resetQuery = () => { query.keyword = ''; query.category = ''; loadData() }

const categoryLabel = (value: string | undefined) => {
  if (!value) return ''
  return dictStore.getLabel(DICT_CODE, value)
}

const showCreateDialog = ref(false)
const isEdit = ref(false)
const editId = ref<number | string>('')
const formRef = ref()
const formRules = {
  materialCode: [{ required: true, message: '物料编码不能为空', trigger: 'blur' }],
  materialName: [{ required: true, message: '物料名称不能为空', trigger: 'blur' }],
}
const createForm = reactive({
  materialCode: '', materialName: '', spec: '', unit: 'PCS', category: '', remark: '',
})

const openCreate = () => {
  isEdit.value = false
  editId.value = ''
  createForm.materialCode = ''
  createForm.materialName = ''
  createForm.spec = ''
  createForm.unit = 'PCS'
  createForm.category = ''
  createForm.remark = ''
  showCreateDialog.value = true
}

const generateMaterialCode = () => {
  const date = dayjs().format('YYYYMMDD')
  const rand = Math.floor(Math.random() * 9000 + 1000)
  createForm.materialCode = `MAT${date}${rand}`
}

const openEdit = (row: Material) => {
  isEdit.value = true
  editId.value = row.id
  createForm.materialCode = row.code || ''
  createForm.materialName = row.name || ''
  createForm.spec = row.spec || ''
  createForm.unit = row.unit || 'PCS'
  createForm.category = row.category || ''
  createForm.remark = ''
  showCreateDialog.value = true
}

const submitForm = async () => {
  try { await formRef.value?.validate() } catch { return }
  try {
    const data = { ...createForm }
    if (isEdit.value) {
      await materialApi.update(editId.value, data)
      ElMessage.success('物料更新成功')
    } else {
      await materialApi.create(data)
      ElMessage.success('物料创建成功')
    }
    showCreateDialog.value = false
    loadData()
  } catch { /* 拦截器处理 */ }
}

const handleDelete = async (row: Material) => {
  try {
    await ElMessageBox.confirm(`确认删除物料「${row.name}（${row.code}）」？`, '删除确认', { type: 'warning' })
    await materialApi.delete(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch { /* 取消或错误 */ }
}

onMounted(() => {
  dictStore.loadDict(DICT_CODE)
  loadData()
})
</script>

<template>
  <PageContainer title="物料管理" subtitle="维护物料主数据，支持编码、名称、规格、单位、分类">
    <template #actions>
      <el-button type="primary" @click="openCreate">新增物料</el-button>
      <el-button @click="exportVisible = true">导出</el-button>
    </template>
    <div class="search-panel">
      <el-form inline :model="query" @submit.prevent="loadData">
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="物料编码/名称" clearable @clear="loadData" @keyup.enter="loadData" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="query.category" placeholder="全部分类" clearable style="width: 160px" @change="loadData">
            <el-option v-for="item in categoryOptions" :key="item.itemValue" :label="item.itemLabel" :value="item.itemValue" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    <el-table v-loading="loading" :data="records" border highlight-current-row>
      <el-table-column prop="code" label="物料编码" width="150" />
      <el-table-column prop="name" label="物料名称" min-width="180" show-overflow-tooltip />
      <el-table-column prop="spec" label="规格" width="140" show-overflow-tooltip />
      <el-table-column prop="unit" label="单位" width="80" />
      <el-table-column label="分类" width="120">
        <template #default="{ row }">
          <el-tag v-if="row.category" type="info" size="small">{{ categoryLabel(row.category) }}</el-tag>
          <span v-else class="text-muted">—</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="80" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
      :total="total" layout="total, prev, pager, next, sizes" class="mt-4"
      @current-change="() => { if (!loading) loadData() }" @size-change="() => { if (!loading) loadData() }"
    />

    <el-dialog v-model="showCreateDialog" :title="isEdit ? '编辑物料' : '新增物料'" width="560px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="createForm" :rules="formRules" label-width="90px">
        <el-form-item label="物料编码" prop="materialCode">
          <el-input v-model="createForm.materialCode" placeholder="请输入物料编码">
            <template #append>
              <el-button @click="generateMaterialCode">自动生成</el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="物料名称" prop="materialName">
          <el-input v-model="createForm.materialName" placeholder="请输入物料名称" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="规格">
              <el-input v-model="createForm.spec" placeholder="选填" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="单位">
              <el-select v-model="createForm.unit" style="width:100%">
                <el-option label="PCS" value="PCS" />
                <el-option label="KG" value="KG" />
                <el-option label="M" value="M" />
                <el-option label="SET" value="SET" />
                <el-option label="BOX" value="BOX" />
                <el-option label="ROLL" value="ROLL" />
                <el-option label="L" value="L" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="分类">
          <el-select v-model="createForm.category" placeholder="请选择分类" clearable style="width:100%">
            <el-option v-for="item in categoryOptions" :key="item.itemValue" :label="item.itemLabel" :value="item.itemValue" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="createForm.remark" type="textarea" :rows="2" placeholder="选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="submitForm">{{ isEdit ? '确认更新' : '确认新增' }}</el-button>
      </template>
    </el-dialog>
    <ExportDialog v-model="exportVisible" />
  </PageContainer>
</template>

<style scoped>
.search-panel { background: #ffffff; padding: 16px; border-radius: 12px; margin-bottom: 16px; }
.text-muted { color: #909399; }
</style>