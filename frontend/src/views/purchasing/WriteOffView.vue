<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { logisticsApi } from '@/api/logistics'
import PageContainer from '@/components/common/PageContainer.vue'
import type { WriteOffRecord } from '@/types/business'
import dayjs from 'dayjs'

const router = useRouter()
const loading = ref(false)
const records = ref<WriteOffRecord[]>([])
const total = ref(0)
const dialogVisible = ref(false)

const query = reactive({ pageNum: 1, pageSize: 10, keyword: '' })

const form = reactive<Partial<WriteOffRecord>>({
  writeOffNo: '',
  asnNo: '',
  orderNo: '',
  supplierName: '',
  writeOffType: '',
  amount: 0,
  reason: '',
})

const loadData = async () => {
  loading.value = true
  try {
    const params: Record<string, unknown> = { pageNum: query.pageNum, pageSize: query.pageSize }
    if (query.keyword) params.keyword = query.keyword
    const result = await logisticsApi.writeOffPage(params as any)
    records.value = result.records
    total.value = result.total
    if (result.total === 0) query.pageNum = 1
  } finally {
    loading.value = false
  }
}

function generateNo() {
  const date = dayjs().format('YYYYMMDD')
  const rand = Math.floor(Math.random() * 9000 + 1000)
  form.writeOffNo = `WO${date}${rand}`
}

function openCreate() {
  form.writeOffNo = ''
  form.asnNo = ''
  form.orderNo = ''
  form.supplierName = ''
  form.writeOffType = '退货冲销'
  form.amount = 0
  form.reason = ''
  generateNo()
  dialogVisible.value = true
}

async function submit() {
  if (!form.asnNo) {
    ElMessage.warning('请输入关联的 ASN 号')
    return
  }
  if (!form.amount || form.amount <= 0) {
    ElMessage.warning('请输入冲销金额')
    return
  }
  loading.value = true
  try {
    await logisticsApi.createWriteOff(form)
    ElMessage.success('冲销单已创建')
    dialogVisible.value = false
    loadData()
  } finally {
    loading.value = false
  }
}

const statusMap: Record<string, string> = {
  '已提交': 'info',
  '审批中': 'warning',
  '已批准': 'success',
  '已完成': '',
  '已驳回': 'danger',
}

const resetQuery = () => {
  query.keyword = ''
  loadData()
}

onMounted(loadData)
</script>

<template>
  <PageContainer title="冲销/调整单管理" subtitle="创建和管理冲销单、调整单，处理收货差异与异常">
    <template #actions>
      <el-button type="primary" @click="openCreate">创建冲销单</el-button>
      <el-button @click="router.push('/purchasing/asn')">返回 ASN 列表</el-button>
    </template>

    <div class="search-panel">
      <el-form inline :model="query" @submit.prevent="loadData">
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="冲销单号/ASN/订单号" clearable @clear="loadData" @keyup.enter="loadData" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-table v-loading="loading" :data="records" border highlight-current-row>
      <el-table-column prop="writeOffNo" label="冲销单号" width="170" />
      <el-table-column prop="asnNo" label="关联ASN" width="170" />
      <el-table-column prop="orderNo" label="订单号" width="160" />
      <el-table-column prop="supplierName" label="供应商" min-width="180" />
      <el-table-column prop="writeOffType" label="类型" width="120">
        <template #default="{ row }">
          <el-tag :type="row.writeOffType === '退货冲销' ? 'warning' : 'info'" size="small">{{ row.writeOffType }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="amount" label="金额" width="120">
        <template #default="{ row }">{{ (row.amount || 0).toLocaleString() }}</template>
      </el-table-column>
      <el-table-column prop="reason" label="原因" min-width="140" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusMap[row.status] || 'info'" size="small">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170" />
    </el-table>

    <el-pagination
      v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
      :total="total" layout="total, prev, pager, next, sizes" class="mt-4"
      @current-change="() => { if (!loading) loadData() }" @size-change="() => { if (!loading) loadData() }"
    />

    <!-- 创建冲销单弹窗 -->
    <el-dialog v-model="dialogVisible" title="创建冲销/调整单" width="550px">
      <el-form :model="form" label-width="110px">
        <el-form-item label="冲销单号">
          <el-input v-model="form.writeOffNo" placeholder="自动生成">
            <template #append>
              <el-button @click="generateNo">生成</el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="关联ASN号" required>
          <el-input v-model="form.asnNo" placeholder="请输入 ASN 号" />
        </el-form-item>
        <el-form-item label="关联订单号">
          <el-input v-model="form.orderNo" placeholder="选填" />
        </el-form-item>
        <el-form-item label="供应商">
          <el-input v-model="form.supplierName" placeholder="选填" />
        </el-form-item>
        <el-form-item label="冲销类型" required>
          <el-select v-model="form.writeOffType" style="width: 100%">
            <el-option label="退货冲销" value="退货冲销" />
            <el-option label="价格调整" value="价格调整" />
            <el-option label="数量差异调整" value="数量差异调整" />
            <el-option label="破损扣减" value="破损扣减" />
            <el-option label="质量扣款" value="质量扣款" />
            <el-option label="其他调整" value="其他调整" />
          </el-select>
        </el-form-item>
        <el-form-item label="冲销金额" required>
          <el-input-number v-model="form.amount" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="冲销原因">
          <el-input v-model="form.reason" type="textarea" :rows="3" placeholder="请描述冲销原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="loading" @click="submit">提交</el-button>
      </template>
    </el-dialog>
  </PageContainer>
</template>

<style scoped>
.search-panel {
  margin-bottom: 12px;
}
</style>