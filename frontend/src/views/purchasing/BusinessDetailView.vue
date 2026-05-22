<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { UploadFilled } from '@element-plus/icons-vue'
import { mockApi } from '@/api/mockApi'
import PageContainer from '@/components/common/PageContainer.vue'
import StatusTag from '@/components/business/StatusTag.vue'
import type { TimelineItem } from '@/types/business'

const route = useRoute()
const router = useRouter()
const detail = ref<Record<string, unknown> | undefined>()
const timeline = ref<TimelineItem[]>([])

const moduleName = computed(() => route.meta.moduleName as string)
const backPath = computed(() => route.meta.activeMenu as string)
const status = computed(() => detail.value?.status as string)

const fields = computed(() => Object.entries(detail.value || {}).filter(([key]) => key !== 'id').slice(0, 12))

const loadData = async () => {
  const id = Number(route.params.id)
  if (moduleName.value === '供应商详情') detail.value = await mockApi.getSupplier(id) as unknown as Record<string, unknown>
  if (moduleName.value === '订单详情') detail.value = await mockApi.getOrder(id) as unknown as Record<string, unknown>
  if (moduleName.value === 'ASN详情') detail.value = await mockApi.getAsn(id) as unknown as Record<string, unknown>
  if (moduleName.value === '质量详情') detail.value = await mockApi.getQuality(id) as unknown as Record<string, unknown>
  if (moduleName.value === '对账详情') detail.value = await mockApi.getSettlement(id) as unknown as Record<string, unknown>
  timeline.value = await mockApi.getTimeline()
}

onMounted(loadData)
</script>

<template>
  <PageContainer :title="moduleName" subtitle="统一单据详情结构：基础信息、业务明细、附件占位、审批记录、操作日志">
    <template #actions><el-button @click="router.push(backPath)">返回列表</el-button><el-button type="primary">提交处理</el-button></template>
    <el-empty v-if="!detail" description="未找到虚拟数据" />
    <template v-else>
      <div class="detail-head">
        <div><div class="head-label">当前状态</div><StatusTag :value="status" /></div>
        <div><div class="head-label">单据来源</div><strong>前端虚拟数据</strong></div>
        <div><div class="head-label">业务节点</div><strong>采购方复核</strong></div>
      </div>
      <el-divider />
      <div class="detail-grid">
        <div v-for="[key, value] in fields" :key="key" class="detail-item">
          <div class="detail-label">{{ key }}</div>
          <div class="detail-value">{{ value }}</div>
        </div>
      </div>
      <el-divider />
      <el-tabs>
        <el-tab-pane label="业务明细">
          <el-table :data="[detail, detail]" border>
            <el-table-column type="index" width="60" />
            <el-table-column label="物料/事项" prop="name" min-width="180" />
            <el-table-column label="数量" width="120"><template #default="{ $index }">{{ ($index + 1) * 120 }}</template></el-table-column>
            <el-table-column label="备注" min-width="220">按业务模块展示核心明细，当前为虚拟数据占位</el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="附件">
          <el-upload drag action="#" :auto-upload="false"><el-icon class="el-icon--upload"><UploadFilled /></el-icon><div class="el-upload__text">拖拽文件到此处，或点击上传</div></el-upload>
        </el-tab-pane>
        <el-tab-pane label="操作日志">
          <el-timeline>
            <el-timeline-item v-for="item in timeline" :key="item.title" :timestamp="item.time" type="primary"><strong>{{ item.title }}</strong><div>{{ item.content }}</div></el-timeline-item>
          </el-timeline>
        </el-tab-pane>
      </el-tabs>
    </template>
  </PageContainer>
</template>

<style scoped>
.detail-head {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
}

.detail-head > div {
  padding: 16px;
  background: #f8fbff;
  border: 1px solid #e4ebf3;
  border-radius: 12px;
}

.head-label {
  margin-bottom: 8px;
  font-size: 12px;
  color: #718096;
}
</style>
