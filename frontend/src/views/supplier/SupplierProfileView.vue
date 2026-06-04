<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useUserStore } from '@/stores/user'
import { supplierApi } from '@/api/supplier'
import type { SupplierCategoryItem } from '@/api/supplier'
import { qualificationApi } from '@/api/qualification'
import { toSupplier } from '@/api/adapters'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageContainer from '@/components/common/PageContainer.vue'
import AttachmentPanel from '@/components/business/AttachmentPanel.vue'
import StatusTag from '@/components/business/StatusTag.vue'
import type { Supplier, SupplierQualificationItem } from '@/types/business'
import dayjs from 'dayjs'

const userStore = useUserStore()
const supplier = ref<Supplier | null>(null)
const loading = ref(false)

const isSupplierUser = computed(() => userStore.user?.userType === 'supplier')
const supplierId = computed<string | null>(() => {
  const dept = userStore.user?.department
  if (dept?.startsWith('供应商ID：')) return dept.replace('供应商ID：', '')
  return null
})

const loadSupplier = async () => {
  loading.value = true
  try {
    if (supplierId.value) {
      const data = await supplierApi.detail(supplierId.value)
      supplier.value = toSupplier(data)
    }
  } finally {
    loading.value = false
  }
}

const loadAll = async () => {
  if (!isSupplierUser.value && !supplierId.value) return
  await Promise.all([loadSupplier(), loadQualifications()])
}

onMounted(loadAll)

// ==================== 编辑功能 ====================
const showEditDialog = ref(false)
const editForm = reactive({
  supplierName: '',
  supplierShortName: '',
  categoryId: undefined as number | undefined,
  supplierType: undefined as number | undefined,
  creditCode: '',
  legalPerson: '',
  contactName: '',
  contactPhone: '',
  contactEmail: '',
  province: '',
  city: '',
  district: '',
  address: '',
  bankName: '',
  bankAccount: '',
  taxNumber: '',
  remark: '',
})

const categoryList = ref<SupplierCategoryItem[]>([])

/** 供应类型选项（与后端 enum 对齐） */
const supplierTypeOptions = [
  { value: 1, label: '原材料' },
  { value: 2, label: '辅材' },
  { value: 3, label: '设备' },
  { value: 4, label: '服务' },
  { value: 5, label: '其他' },
]

const openEdit = async () => {
  if (!supplier.value) return
  // 加载品类列表
  try {
    categoryList.value = await supplierApi.listCategories()
  } catch { /* 非必须 */ }
  const s = supplier.value
  editForm.supplierName = s.name
  editForm.supplierShortName = s.shortName || ''
  editForm.categoryId = (s.categoryId as number | undefined) || undefined
  editForm.supplierType = s.supplierType || undefined
  editForm.creditCode = s.creditCode || ''
  editForm.legalPerson = s.legalPerson || ''
  editForm.contactName = s.contact || ''
  editForm.contactPhone = s.phone || ''
  editForm.contactEmail = s.contactEmail || ''
  editForm.province = s.province || ''
  editForm.city = s.city || ''
  editForm.district = s.district || ''
  editForm.address = s.address || ''
  editForm.bankName = s.bankName || ''
  editForm.bankAccount = s.bankAccount || ''
  editForm.taxNumber = s.taxNumber || ''
  editForm.remark = s.remark || ''
  showEditDialog.value = true
}

const submitEdit = async () => {
  if (!supplierId.value) return
  if (!editForm.supplierName) {
    ElMessage.warning('企业名称不能为空')
    return
  }
  try {
    await supplierApi.update(supplierId.value, editForm as unknown as Record<string, unknown>)
    ElMessage.success('资料更新成功')
    showEditDialog.value = false
    loadSupplier()
  } catch { /* 拦截器处理 */ }
}

// ==================== 资质证书管理 ====================
const qualLoading = ref(false)
const qualifications = ref<SupplierQualificationItem[]>([])
const showQualDialog = ref(false)
const qualEditing = ref<SupplierQualificationItem | null>(null)

const qualForm = reactive({
  qualType: '',
  qualName: '',
  qualNo: '',
  qualOrg: '',
  validStart: '',
  validEnd: '',
  remindDays: 30,
  remark: '',
})

const qualTypeMap: Record<string, string> = {
  business_license: '营业执照',
  iso9001: 'ISO9001',
  iso14001: 'ISO14001',
  iso45001: 'ISO45001',
  iatf16949: 'IATF16949',
  rohs: 'RoHS',
  reach: 'REACH',
  food_license: '食品许可',
  medical_license: '医疗器械许可',
  production_license: '生产许可证',
  other: '其他',
}

const qualStatusMap: Record<number, { text: string; type: string }> = {
  0: { text: '待审核', type: 'warning' },
  1: { text: '有效', type: 'success' },
  2: { text: '即将过期', type: 'warning' },
  3: { text: '已过期', type: 'danger' },
}

const loadQualifications = async () => {
  qualLoading.value = true
  try {
    // 供应商用户不传 supplierId，由后端从 token 中解析，避免前后端不一致导致"数据越权"
    const params: any = { pageNum: 1, pageSize: 100 }
    if (!isSupplierUser.value && supplierId.value) params.supplierId = supplierId.value
    const result = await qualificationApi.page(params)
    qualifications.value = result.records
  } finally {
    qualLoading.value = false
  }
}

const openQualCreate = () => {
  qualEditing.value = null
  qualForm.qualType = ''
  qualForm.qualName = ''
  qualForm.qualNo = ''
  qualForm.qualOrg = ''
  qualForm.validStart = ''
  qualForm.validEnd = ''
  qualForm.remindDays = 30
  qualForm.remark = ''
  showQualDialog.value = true
}

const openQualEdit = (row: SupplierQualificationItem) => {
  qualEditing.value = row
  qualForm.qualType = row.qualType
  qualForm.qualName = row.qualName
  qualForm.qualNo = row.qualNo || ''
  qualForm.qualOrg = row.qualOrg || ''
  qualForm.validStart = row.validStart || ''
  qualForm.validEnd = row.validEnd || ''
  qualForm.remindDays = row.remindDays
  qualForm.remark = row.remark || ''
  showQualDialog.value = true
}

const submitQual = async () => {
  if (!supplierId.value) return
  if (!qualForm.qualName) { ElMessage.warning('请输入资质名称'); return }
  try {
    const data: any = {
      ...qualForm,
      supplierId: supplierId.value,
    }
    if (qualEditing.value?.id) {
      await qualificationApi.update(qualEditing.value.id, data)
      ElMessage.success('资质证书更新成功')
    } else {
      await qualificationApi.create(data)
      ElMessage.success('资质证书添加成功')
    }
    showQualDialog.value = false
    loadQualifications()
  } catch { /* */ }
}

const handleQualDelete = (row: SupplierQualificationItem) => {
  ElMessageBox.confirm(`确定删除资质「${row.qualName}」？`, '删除确认', { type: 'warning' })
    .then(() => qualificationApi.delete(row.id))
    .then(() => { ElMessage.success('已删除'); loadQualifications() })
    .catch(() => {})
}

const getQualStatus = (row: SupplierQualificationItem) => {
  if (!row.validEnd) return qualStatusMap[1]
  const now = dayjs()
  const end = dayjs(row.validEnd)
  if (end.isBefore(now)) return qualStatusMap[3]
  const daysLeft = end.diff(now, 'day')
  if (daysLeft <= row.remindDays) return qualStatusMap[2]
  return qualStatusMap[1]
}

// 资质预警统计
const expiredQuals = computed(() => qualifications.value.filter(q => {
  if (!q.validEnd) return false
  return dayjs(q.validEnd).isBefore(dayjs())
}))
const expiringQuals = computed(() => qualifications.value.filter(q => {
  if (!q.validEnd) return false
  const daysLeft = dayjs(q.validEnd).diff(dayjs(), 'day')
  return daysLeft > 0 && daysLeft <= q.remindDays
}))
const hasQualWarnings = computed(() => expiredQuals.value.length > 0 || expiringQuals.value.length > 0)

/** 省市区合并显示 */
const regionText = computed(() => {
  const s = supplier.value
  if (!s) return ''
  const parts = [s.province, s.city, s.district].filter(Boolean)
  return parts.length > 0 ? parts.join(' ') : ''
})
</script>

<template>
  <PageContainer title="供应商资料中心" subtitle="维护企业资料、联系人、资质证书与到期提醒">
    <template #actions>
      <el-button type="primary" @click="openEdit">编辑资料</el-button>
    </template>
    <div v-loading="loading">
      <template v-if="supplier">
        <!-- 基本信息区 -->
        <div class="section-title">基本信息</div>
        <div class="detail-grid">
          <div class="detail-item"><div class="detail-label">企业名称</div><div class="detail-value">{{ supplier.name }}</div></div>
          <div class="detail-item"><div class="detail-label">企业简称</div><div class="detail-value">{{ supplier.shortName || '-' }}</div></div>
          <div class="detail-item"><div class="detail-label">统一社会信用代码</div><div class="detail-value">{{ supplier.creditCode || '-' }}</div></div>
          <div class="detail-item"><div class="detail-label">法定代表人</div><div class="detail-value">{{ supplier.legalPerson || '-' }}</div></div>
          <div class="detail-item"><div class="detail-label">供应品类</div><div class="detail-value">{{ supplier.category }}</div></div>
          <div class="detail-item"><div class="detail-label">状态</div><div class="detail-value"><StatusTag :value="supplier.status" /></div></div>
          <div class="detail-item"><div class="detail-label">等级</div><div class="detail-value">{{ supplier.level }}</div></div>
          <div class="detail-item"><div class="detail-label">绩效分</div><div class="detail-value">{{ supplier.performanceScore }}</div></div>
          <div class="detail-item"><div class="detail-label">风险等级</div><div class="detail-value"><StatusTag :value="supplier.riskLevel" kind="risk" /></div></div>
        </div>

        <!-- 联系信息区 -->
        <div class="section-title">联系信息</div>
        <div class="detail-grid">
          <div class="detail-item"><div class="detail-label">联系人</div><div class="detail-value">{{ supplier.contact }}</div></div>
          <div class="detail-item"><div class="detail-label">联系电话</div><div class="detail-value">{{ supplier.phone }}</div></div>
          <div class="detail-item"><div class="detail-label">邮箱</div><div class="detail-value">{{ supplier.contactEmail || '-' }}</div></div>
          <div class="detail-item"><div class="detail-label">所在地区</div><div class="detail-value">{{ regionText || '-' }}</div></div>
          <div class="detail-item" style="grid-column: span 2;"><div class="detail-label">详细地址</div><div class="detail-value">{{ supplier.address }}</div></div>
        </div>

        <!-- 财务信息区 -->
        <div class="section-title">财务信息</div>
        <div class="detail-grid">
          <div class="detail-item"><div class="detail-label">开户银行</div><div class="detail-value">{{ supplier.bankName || '-' }}</div></div>
          <div class="detail-item"><div class="detail-label">银行账号</div><div class="detail-value">{{ supplier.bankAccount || '-' }}</div></div>
          <div class="detail-item"><div class="detail-label">纳税人识别号</div><div class="detail-value">{{ supplier.taxNumber || '-' }}</div></div>
          <div class="detail-item" v-if="supplier.remark"><div class="detail-label">备注</div><div class="detail-value">{{ supplier.remark }}</div></div>
        </div>
      </template>
      <el-empty v-else description="暂无供应商资料" />
    </div>

    <!-- 资质证书管理 -->
    <el-divider>资质证书</el-divider>
    <div class="qual-section">
      <!-- 资质预警横幅 -->
      <el-alert
        v-if="expiredQuals.length > 0"
        :title="`${expiredQuals.length} 项资质证书已过期，请尽快更新`"
        type="error"
        :closable="false"
        show-icon
        class="qual-alert"
      >
        <template #default>
          <span v-for="q in expiredQuals" :key="q.id" class="qual-alert-item">
            {{ q.qualName }}（有效期至 {{ q.validEnd }}）
          </span>
        </template>
      </el-alert>
      <el-alert
        v-if="expiringQuals.length > 0"
        :title="`${expiringQuals.length} 项资质证书即将过期`"
        type="warning"
        :closable="false"
        show-icon
        class="qual-alert"
      >
        <template #default>
          <span v-for="q in expiringQuals" :key="q.id" class="qual-alert-item">
            {{ q.qualName }}（有效期至 {{ q.validEnd }}，剩余 {{ dayjs(q.validEnd).diff(dayjs(), 'day') }} 天）
          </span>
        </template>
      </el-alert>
      <div class="qual-toolbar">
        <el-button type="primary" size="small" @click="openQualCreate">添加证书</el-button>
      </div>
      <el-table v-loading="qualLoading" :data="qualifications" border highlight-current-row size="small">
        <el-table-column label="类型" width="120">
          <template #default="{ row }">{{ qualTypeMap[row.qualType] || row.qualType }}</template>
        </el-table-column>
        <el-table-column prop="qualName" label="资质名称" min-width="180" />
        <el-table-column prop="qualNo" label="证书编号" width="160" />
        <el-table-column prop="qualOrg" label="颁发机构" min-width="160" />
        <el-table-column label="有效期" width="200">
          <template #default="{ row }">
            <span v-if="row.validStart || row.validEnd">
              {{ row.validStart || '-' }} ~ {{ row.validEnd || '-' }}
            </span>
            <span v-else class="text-muted">长期有效</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="getQualStatus(row).type">
              {{ getQualStatus(row).text }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="到期提醒" width="90">
          <template #default="{ row }">{{ row.remindDays }}天</template>
        </el-table-column>
        <el-table-column label="备注" min-width="140" show-overflow-tooltip>
          <template #default="{ row }">{{ row.remark || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openQualEdit(row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleQualDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!qualLoading && qualifications.length === 0" description="暂无资质证书，请添加" />
    </div>

    <el-divider>资料附件</el-divider>
    <AttachmentPanel business-type="supplier" :business-id="supplierId ?? undefined" :editable="true" />

    <!-- 编辑资料弹窗（全字段） -->
    <el-dialog v-model="showEditDialog" title="编辑供应商资料" width="680px" :close-on-click-modal="false">
      <el-form :model="editForm" label-width="110px">
        <!-- 基本信息 -->
        <div class="form-section-label">基本信息</div>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="企业名称" required>
              <el-input v-model="editForm.supplierName" placeholder="请输入企业名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="企业简称">
              <el-input v-model="editForm.supplierShortName" placeholder="选填" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="统一社会信用代码">
              <el-input v-model="editForm.creditCode" placeholder="选填" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="法定代表人">
              <el-input v-model="editForm.legalPerson" placeholder="选填" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="供应品类">
              <el-select v-model="editForm.categoryId" placeholder="请选择" style="width:100%" clearable>
                <el-option v-for="cat in categoryList" :key="cat.id" :label="cat.categoryName" :value="cat.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="供应类型">
              <el-select v-model="editForm.supplierType" placeholder="请选择" style="width:100%" clearable>
                <el-option v-for="opt in supplierTypeOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 联系信息 -->
        <div class="form-section-label">联系信息</div>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="联系人">
              <el-input v-model="editForm.contactName" placeholder="选填" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="联系电话">
              <el-input v-model="editForm.contactPhone" placeholder="选填" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="邮箱">
              <el-input v-model="editForm.contactEmail" placeholder="选填" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="省份">
              <el-input v-model="editForm.province" placeholder="选填" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="城市">
              <el-input v-model="editForm.city" placeholder="选填" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="区县">
              <el-input v-model="editForm.district" placeholder="选填" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="详细地址">
          <el-input v-model="editForm.address" type="textarea" :rows="1" placeholder="选填" />
        </el-form-item>

        <!-- 财务信息 -->
        <div class="form-section-label">财务信息</div>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="开户银行">
              <el-input v-model="editForm.bankName" placeholder="选填" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="银行账号">
              <el-input v-model="editForm.bankAccount" placeholder="选填" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="纳税人识别号">
              <el-input v-model="editForm.taxNumber" placeholder="选填" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="备注">
              <el-input v-model="editForm.remark" placeholder="选填" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="submitEdit">保存修改</el-button>
      </template>
    </el-dialog>

    <!-- 资质证书弹窗 -->
    <el-dialog v-model="showQualDialog" :title="qualEditing?.id ? '编辑资质证书' : '添加资质证书'" width="550px" :close-on-click-modal="false">
      <el-form :model="qualForm" label-width="100px">
        <el-form-item label="资质类型">
          <el-select v-model="qualForm.qualType" placeholder="请选择" style="width:100%" clearable>
            <el-option v-for="(label, key) in qualTypeMap" :key="key" :label="label" :value="key" />
          </el-select>
        </el-form-item>
        <el-form-item label="资质名称" required>
          <el-input v-model="qualForm.qualName" placeholder="如：ISO9001质量管理体系认证" />
        </el-form-item>
        <el-form-item label="证书编号">
          <el-input v-model="qualForm.qualNo" placeholder="选填" />
        </el-form-item>
        <el-form-item label="颁发机构">
          <el-input v-model="qualForm.qualOrg" placeholder="选填" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="有效期起">
              <el-date-picker v-model="qualForm.validStart" type="date" value-format="YYYY-MM-DD" style="width:100%" placeholder="选填" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="有效期止">
              <el-date-picker v-model="qualForm.validEnd" type="date" value-format="YYYY-MM-DD" style="width:100%" placeholder="选填" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="到期提醒">
          <el-input-number v-model="qualForm.remindDays" :min="1" :max="365" style="width:100%" />
          <div class="form-tip">到期前 N 天发送提醒通知</div>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="qualForm.remark" type="textarea" :rows="2" placeholder="选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showQualDialog = false">取消</el-button>
        <el-button type="primary" @click="submitQual">确认</el-button>
      </template>
    </el-dialog>
  </PageContainer>
</template>

<style scoped>
.detail-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
}
.detail-item {
  padding: 14px;
  background: #fafbfc;
  border: 1px solid #edf0f4;
  border-radius: 8px;
}
.detail-label {
  font-size: 11px;
  color: #8a98aa;
  margin-bottom: 6px;
}
.detail-value {
  font-size: 14px;
  font-weight: 600;
  color: #2c3e50;
}
.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #2c3e50;
  margin: 20px 0 10px;
  padding-left: 10px;
  border-left: 3px solid #409eff;
}
.form-section-label {
  font-size: 13px;
  font-weight: 600;
  color: #409eff;
  margin-bottom: 12px;
  padding-bottom: 6px;
  border-bottom: 1px solid #e8edf2;
}
.qual-section {
  background: #ffffff;
  border-radius: 12px;
  padding: 16px;
}
.qual-alert {
  margin-bottom: 12px;
}
.qual-alert-item {
  display: block;
  margin-top: 4px;
  font-size: 13px;
}
.qual-toolbar {
  margin-bottom: 12px;
  display: flex;
  justify-content: flex-end;
}
.text-muted {
  color: #909399;
}
.form-tip {
  margin-top: 4px;
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
}
</style>