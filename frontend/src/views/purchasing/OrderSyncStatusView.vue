<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { syncTaskApi } from '@/api/integration'
import PageContainer from '@/components/common/PageContainer.vue'
import StatusTag from '@/components/business/StatusTag.vue'

const router = useRouter()
const loading = ref(false)
const records = ref<any[]>([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '', taskStatus: undefined as number | undefined })

const taskStatusMap: Record<number, string> = { 0: '待处理', 1: '处理中', 2: '已完成', 3: '失败', 4: '已暂停' }

const loadTasks = async () => {
  loading.value = true
  try {
    const params: Record<string, unknown> = { pageNum: query.pageNum, pageSize: query.pageSize }
    if (query.keyword) params.keyword = query.keyword
    if (query.taskStatus !== undefined && query.taskStatus !== null) params.taskStatus = query.taskStatus
    // Filter for order-related tasks only
    params.taskType = 'ERP_ORDER_SYNC'
    const res = await syncTaskApi.page(params)
    records.value = res.records
    total.value = res.total
    if (res.total === 0) query.pageNum = 1
  } finally { loading.value = false }
}

const resetQuery = () => {
  query.keyword = ''
  query.taskStatus = undefined
  loadTasks()
}

const handleRetry = async (row: any) => {
  const { ElMessage } = await import('element-plus')
  try {
    await syncTaskApi.retry(row.id)
    ElMessage.success('重试已提交')
    loadTasks()
  } catch { /* */ }
}

const syncStats = ref({ total: 0, success: 0, failed: 0, pending: 0 })
const loadStats = async () => {
  try {
    const allRes = await syncTaskApi.page({ pageNum: 1, pageSize: 1000, taskType: 'ERP_ORDER_SYNC' } as any)
    const all = allRes.records
    syncStats.value = {
      total: all.length,
      success: all.filter((r: any) => r.taskStatus === 2).length,
      failed: all.filter((r: any) => r.taskStatus === 3).length,
      pending: all.filter((r: any) => r.taskStatus === 0 || r.taskStatus === 1).length,
    }
  } catch { /* */ }
}

onMounted(() => { loadTasks(); loadStats() })
</script>

<template>
  <PageContainer title="ERP 订单同步状态" subtitle="监控采购订单与 ERP 系统之间的数据同步任务和日志">
    <template #actions>
      <el-button @click="router.push('/purchasing/integration')">返回集成网关</el-button>
      <el-button type="primary" @click="loadTasks">刷新</el-button>
    </template>

    <!-- 同步概览卡片 -->
    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-value">{{ syncStats.total }}</div>
        <div class="stat-label">同步任务总数</div>
      </div>
      <div class="stat-card stat-success">
        <div class="stat-value">{{ syncStats.success }}</div>
        <div class="stat-label">同步成功</div>
      </div>
      <div class="stat-card stat-warning">
        <div class="stat-value">{{ syncStats.pending }}</div>
        <div class="stat-label">待处理/处理中</div>
      </div>
      <div class="stat-card stat-danger">
        <div class="stat-value">{{ syncStats.failed }}</div>
        <div class="stat-label">同步失败</div>
      </div>
    </div>

    <div class="search-panel">
      <el-form inline :model="query" @submit.prevent="loadTasks">
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="任务号/外部单号" clearable @clear="loadTasks" @keyup.enter="loadTasks" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.taskStatus" placeholder="全部" clearable style="width: 140px" @change="loadTasks">
            <el-option label="待处理" :value="0" />
            <el-option label="处理中" :value="1" />
            <el-option label="已完成" :value="2" />
            <el-option label="失败" :value="3" />
            <el-option label="已暂停" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadTasks">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-table v-loading="loading" :data="records" border highlight-current-row>
      <el-table-column prop="taskNo" label="任务号" width="180" />
      <el-table-column prop="externalNo" label="外部单号" width="160" />
      <el-table-column prop="eventType" label="事件类型" width="130" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag size="small" :type="row.taskStatus === 2 ? 'success' : row.taskStatus === 3 ? 'danger' : row.taskStatus === 1 ? 'warning' : 'info'">
            {{ taskStatusMap[row.taskStatus] || '未知' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="retryCount" label="重试次数" width="90" />
      <el-table-column label="错误信息" min-width="200">
        <template #default="{ row }">{{ row.errorMessage ? row.errorMessage.slice(0, 60) : '-' }}</template>
      </el-table-column>
      <el-table-column prop="nextRetryTime" label="下次重试" width="160" />
      <el-table-column prop="createTime" label="创建时间" width="160" />
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.taskStatus === 3 || row.taskStatus === 4" link type="primary" @click="handleRetry(row)">重试</el-button>
          <span v-else class="text-muted">-</span>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
      :total="total" layout="total, prev, pager, next, sizes" class="mt-4"
      @current-change="() => { if (!loading) loadTasks() }" @size-change="() => { if (!loading) loadTasks() }"
    />
  </PageContainer>
</template>

<style scoped>
.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 18px;
}
.stat-card {
  background: #f8fbff;
  border: 1px solid #e4ebf3;
  border-radius: 12px;
  padding: 18px;
  text-align: center;
}
.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #2c3e50;
}
.stat-label {
  font-size: 12px;
  color: #718096;
  margin-top: 4px;
}
.stat-success .stat-value { color: #67c23a; }
.stat-warning .stat-value { color: #e6a23c; }
.stat-danger .stat-value { color: #f56c6c; }
.search-panel {
  background: #ffffff;
  padding: 16px;
  border-radius: 12px;
  margin-bottom: 16px;
}
.text-muted {
  color: #c0c4cc;
  font-size: 13px;
}
</style>