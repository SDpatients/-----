<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, Search, Notebook } from '@element-plus/icons-vue'
import { dictApi, type SysDict, type DictItem } from '@/api/dict'
import PageContainer from '@/components/common/PageContainer.vue'

const dictLoading = ref(false)
const dicts = ref<SysDict[]>([])
const dictFilter = ref('')
const selectedDict = ref<SysDict | null>(null)

const itemLoading = ref(false)
const items = ref<DictItem[]>([])

const loadDicts = async () => {
  dictLoading.value = true
  try {
    dicts.value = await dictApi.listAll()
    applyFilter()
    if (selectedDict.value) {
      const found = dicts.value.find(d => String(d.id) === String(selectedDict.value!.id))
      if (found) selectedDict.value = found
      else { selectedDict.value = null; items.value = [] }
    }
  } finally { dictLoading.value = false }
}

const filteredDicts = ref<SysDict[]>([])
const applyFilter = () => {
  const kw = dictFilter.value.trim().toLowerCase()
  if (!kw) { filteredDicts.value = dicts.value; return }
  filteredDicts.value = dicts.value.filter(d =>
    d.dictName.toLowerCase().includes(kw) || d.dictCode.toLowerCase().includes(kw))
}

const selectDict = async (dict: SysDict) => {
  selectedDict.value = dict
  await loadItems(dict.id)
}

const loadItems = async (dictId: number | string) => {
  itemLoading.value = true
  try { items.value = await dictApi.getItemsByDictId(dictId) }
  finally { itemLoading.value = false }
}

const dictCreateVisible = ref(false)
const dictForm = ref({ dictName: '', dictCode: '', description: '' })
const openDictCreate = () => {
  dictForm.value = { dictName: '', dictCode: '', description: '' }
  dictCreateVisible.value = true
}
const handleDictCreate = async () => {
  if (!dictForm.value.dictName || !dictForm.value.dictCode) {
    ElMessage.warning('字典名称和编码不能为空'); return
  }
  try {
    await dictApi.createDict(dictForm.value)
    ElMessage.success('字典创建成功')
    dictCreateVisible.value = false
    loadDicts()
  } catch { /* handled */ }
}

const handleDictDelete = async (row: SysDict) => {
  try {
    await ElMessageBox.confirm(`确定删除字典「${row.dictName}」及其所有字典项？此操作不可恢复！`, '删除确认', { type: 'warning' })
    await dictApi.deleteDict(row.id)
    ElMessage.success('字典已删除')
    if (selectedDict.value && String(selectedDict.value.id) === String(row.id)) {
      selectedDict.value = null; items.value = []
    }
    loadDicts()
  } catch { /* cancel */ }
}

const itemCreateVisible = ref(false)
const itemForm = ref({ itemLabel: '', itemValue: '', sort: 0, description: '' })
const openItemCreate = () => {
  if (!selectedDict.value) { ElMessage.warning('请先选择一个字典'); return }
  itemForm.value = { itemLabel: '', itemValue: '', sort: items.value.length + 1, description: '' }
  itemCreateVisible.value = true
}
const handleItemCreate = async () => {
  if (!itemForm.value.itemLabel || !itemForm.value.itemValue) {
    ElMessage.warning('标签和值不能为空'); return
  }
  try {
    await dictApi.createItem({ dictId: selectedDict.value!.id, ...itemForm.value })
    ElMessage.success('字典项创建成功')
    itemCreateVisible.value = false
    loadItems(selectedDict.value!.id)
  } catch { /* handled */ }
}

const itemEditVisible = ref(false)
const editingItem = ref<DictItem | null>(null)
const editForm = ref({ itemLabel: '', itemValue: '', sort: 0, description: '' })
const openItemEdit = (row: DictItem) => {
  editingItem.value = row
  editForm.value = { itemLabel: row.itemLabel, itemValue: row.itemValue, sort: row.sort, description: row.description || '' }
  itemEditVisible.value = true
}
const handleItemUpdate = async () => {
  if (!editForm.value.itemLabel || !editForm.value.itemValue) {
    ElMessage.warning('标签和值不能为空'); return
  }
  try {
    await dictApi.updateItem(editingItem.value!.id, editForm.value)
    ElMessage.success('字典项更新成功')
    itemEditVisible.value = false
    loadItems(selectedDict.value!.id)
  } catch { /* handled */ }
}

const handleItemDelete = async (row: DictItem) => {
  try {
    await ElMessageBox.confirm(`确定删除字典项「${row.itemLabel}」？`, '删除确认', { type: 'warning' })
    await dictApi.deleteItem(row.id)
    ElMessage.success('字典项已删除')
    loadItems(selectedDict.value!.id)
  } catch { /* cancel */ }
}

onMounted(() => { loadDicts() })
</script>

<template>
  <PageContainer title="字典管理" subtitle="管理系统数据字典，维护字典项的标签、值和排序">
    <div class="dict-layout">
      <div class="dict-left">
        <div class="dict-left-header">
          <span class="dict-left-title">字典列表</span>
          <el-button type="primary" :icon="Plus" size="small" @click="openDictCreate">新增</el-button>
        </div>
        <div class="dict-search">
          <el-input v-model="dictFilter" placeholder="搜索字典名称/编码" :prefix-icon="Search" clearable @input="applyFilter" @clear="applyFilter" size="small" />
        </div>
        <div class="dict-list" v-loading="dictLoading">
          <div
            v-for="d in (dictFilter ? filteredDicts : dicts)"
            :key="d.id"
            class="dict-item"
            :class="{ active: selectedDict && String(selectedDict.id) === String(d.id) }"
            @click="selectDict(d)"
          >
            <div class="dict-item-info">
              <div class="dict-item-name">{{ d.dictName }}</div>
              <div class="dict-item-code">{{ d.dictCode }}</div>
            </div>
            <el-tag :type="d.status === 1 ? 'success' : 'info'" size="small">{{ d.status === 1 ? '启用' : '禁用' }}</el-tag>
          </div>
          <el-empty v-if="dicts.length === 0 && !dictLoading" description="暂无字典" :image-size="60" />
        </div>
      </div>

      <div class="dict-right">
        <template v-if="selectedDict">
          <div class="dict-right-header">
            <div>
              <span class="dict-right-title">{{ selectedDict.dictName }}</span>
              <el-tag size="small" type="info" style="margin-left:8px">{{ selectedDict.dictCode }}</el-tag>
              <span v-if="selectedDict.description" class="dict-right-desc">{{ selectedDict.description }}</span>
            </div>
            <div>
              <el-button type="primary" :icon="Plus" size="small" @click="openItemCreate">新增字典项</el-button>
              <el-button type="danger" :icon="Delete" size="small" @click="handleDictDelete(selectedDict)">删除字典</el-button>
            </div>
          </div>
          <el-table :data="items" border v-loading="itemLoading" style="margin-top:12px">
            <el-table-column prop="itemLabel" label="标签" width="160" />
            <el-table-column prop="itemValue" label="值" width="160" />
            <el-table-column prop="sort" label="排序" width="80" align="center" />
            <el-table-column prop="description" label="描述" min-width="180" show-overflow-tooltip />
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" :icon="Edit" @click="openItemEdit(row)">编辑</el-button>
                <el-button link type="danger" :icon="Delete" @click="handleItemDelete(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </template>
        <el-empty v-else description="请在左侧选择一个字典查看详情" :image-size="100">
          <template #image>
            <el-icon :size="60" color="#c0c4cc"><Notebook /></el-icon>
          </template>
        </el-empty>
      </div>
    </div>

    <el-dialog v-model="dictCreateVisible" title="新增字典" width="480px" :close-on-click-modal="false">
      <el-form :model="dictForm" label-width="90px">
        <el-form-item label="字典名称" required>
          <el-input v-model="dictForm.dictName" placeholder="如：物料分类" />
        </el-form-item>
        <el-form-item label="字典编码" required>
          <el-input v-model="dictForm.dictCode" placeholder="如：material_category（英文下划线）" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="dictForm.description" placeholder="选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dictCreateVisible = false">取消</el-button>
        <el-button type="primary" @click="handleDictCreate">确认</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="itemCreateVisible" title="新增字典项" width="480px" :close-on-click-modal="false">
      <el-form :model="itemForm" label-width="90px">
        <el-form-item label="标签" required>
          <el-input v-model="itemForm.itemLabel" placeholder="显示名称，如：结构件" />
        </el-form-item>
        <el-form-item label="值" required>
          <el-input v-model="itemForm.itemValue" placeholder="存储值，如：结构件" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="排序号">
              <el-input-number v-model="itemForm.sort" :min="0" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="描述">
              <el-input v-model="itemForm.description" placeholder="选填" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="itemCreateVisible = false">取消</el-button>
        <el-button type="primary" @click="handleItemCreate">确认</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="itemEditVisible" title="编辑字典项" width="480px" :close-on-click-modal="false">
      <el-form :model="editForm" label-width="90px">
        <el-form-item label="标签" required>
          <el-input v-model="editForm.itemLabel" />
        </el-form-item>
        <el-form-item label="值" required>
          <el-input v-model="editForm.itemValue" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="排序号">
              <el-input-number v-model="editForm.sort" :min="0" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="描述">
              <el-input v-model="editForm.description" placeholder="选填" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="itemEditVisible = false">取消</el-button>
        <el-button type="primary" @click="handleItemUpdate">确认</el-button>
      </template>
    </el-dialog>
  </PageContainer>
</template>

<style scoped>
.dict-layout {
  display: flex;
  gap: 16px;
  min-height: 600px;
}
.dict-left {
  width: 320px;
  min-width: 280px;
  background: #fff;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  border: 1px solid #e4ebf3;
}
.dict-left-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 16px 8px;
}
.dict-left-title {
  font-size: 15px;
  font-weight: 700;
  color: #1a2b4c;
}
.dict-search {
  padding: 0 16px 8px;
}
.dict-list {
  flex: 1;
  overflow-y: auto;
  padding: 0 8px 8px;
}
.dict-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.15s;
  margin-bottom: 4px;
}
.dict-item:hover {
  background: #f0f5ff;
}
.dict-item.active {
  background: #e8f0fe;
  border: 1px solid #b3d0ff;
}
.dict-item-info {
  flex: 1;
  min-width: 0;
}
.dict-item-name {
  font-size: 14px;
  font-weight: 600;
  color: #1a2b4c;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.dict-item-code {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}
.dict-right {
  flex: 1;
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  border: 1px solid #e4ebf3;
}
.dict-right-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.dict-right-title {
  font-size: 18px;
  font-weight: 700;
  color: #1a2b4c;
}
.dict-right-desc {
  font-size: 13px;
  color: #909399;
  margin-left: 12px;
}
</style>