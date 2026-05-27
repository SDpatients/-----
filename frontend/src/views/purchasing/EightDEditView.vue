<script setup lang="ts">
import { onMounted, reactive, ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { WarningFilled } from '@element-plus/icons-vue'
import { eightDApi } from '@/api/qualityExtra'
import PageContainer from '@/components/common/PageContainer.vue'
import StatusTag from '@/components/business/StatusTag.vue'
import type { EightDReport, EightDTimeline } from '@/types/business'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const saving = ref(false)
const report = ref<EightDReport | null>(null)
const activeStep = ref(0)

/** D1-D8阶段定义 */
const steps = [
  { key: 'd1Team', title: 'D1 团队组建', desc: '组建跨职能问题解决团队' },
  { key: 'd2Problem', title: 'D2 问题描述', desc: '明确描述问题，5W2H分析' },
  { key: 'd3Containment', title: 'D3 遏制措施', desc: '制定并执行临时遏制措施' },
  { key: 'd4RootCause', title: 'D4 根因分析', desc: '识别并验证根本原因' },
  { key: 'd5CorrectiveAction', title: 'D5 纠正措施', desc: '制定永久性纠正措施方案' },
  { key: 'd6ValidateAction', title: 'D6 验证措施', desc: '实施并验证纠正措施有效性' },
  { key: 'd7PreventAction', title: 'D7 预防措施', desc: '预防再发生，标准化/系统化' },
  { key: 'd8CloseSummary', title: 'D8 总结关闭', desc: '团队总结、经验教训与关闭' },
]

interface FormData {
  d1Team: string; d2Problem: string; d3Containment: string
  d4RootCause: string; d5CorrectiveAction: string; d6ValidateAction: string
  d7PreventAction: string; d8CloseSummary: string; dueDate: string
}

const form = reactive<FormData>({
  d1Team: '', d2Problem: '', d3Containment: '',
  d4RootCause: '', d5CorrectiveAction: '', d6ValidateAction: '',
  d7PreventAction: '', d8CloseSummary: '', dueDate: '',
})

const id = computed(() => route.params.id as string)

const loadDetail = async () => {
  loading.value = true
  try {
    const detail = await eightDApi.detail(id.value)
    report.value = detail
    Object.assign(form, {
      d1Team: detail.d1Team || '',
      d2Problem: detail.d2Problem || '',
      d3Containment: detail.d3Containment || '',
      d4RootCause: detail.d4RootCause || '',
      d5CorrectiveAction: detail.d5CorrectiveAction || '',
      d6ValidateAction: detail.d6ValidateAction || '',
      d7PreventAction: detail.d7PreventAction || '',
      d8CloseSummary: detail.d8CloseSummary || '',
      dueDate: detail.dueDate || '',
    })
    // 自动定位到第一个未填写的阶段
    const idx = steps.findIndex((s) => !form[s.key as keyof FormData])
    if (idx >= 0) activeStep.value = idx
  } finally { loading.value = false }
}

const currentField = computed(() => steps[activeStep.value].key)

/** 构建时间线 */
const buildTimeline = computed<EightDTimeline[]>(() => {
  if (!report.value) return []
  const r = report.value
  const stageKeys = ['d1Team', 'd2Problem', 'd3Containment', 'd4RootCause', 'd5CorrectiveAction', 'd6ValidateAction', 'd7PreventAction', 'd8CloseSummary'] as const
  const stageNames = ['D1-团队组建', 'D2-问题描述', 'D3-遏制措施', 'D4-根因分析', 'D5-纠正措施', 'D6-验证措施', 'D7-预防措施', 'D8-总结关闭']

  // 进度决定: 0草稿→全部pending; 1-2已提交/审核中→按d1-d8正文是否填写判断
  const isClosed = r.reportStatus >= 4
  const now = new Date().toISOString().split('T')[0]

  return stageKeys.map((key, i) => {
    const content = r[key] as string || ''
    let status: EightDTimeline['status'] = 'pending'
    if (isClosed) {
      status = 'completed'
    } else if (content) {
      status = 'completed'
    } else if (i > 0) {
      const prevContent = r[stageKeys[i - 1]] as string | undefined
      if (prevContent) status = r.reportStatus >= 1 ? 'in_progress' : 'pending'
    } else {
      status = r.reportStatus >= 1 ? 'in_progress' : 'pending'
    }
    // overdue check
    if (r.dueDate && r.dueDate < now && status !== 'completed') {
      status = 'overdue'
    }
    return {
      stage: key,
      stageName: stageNames[i],
      content: content || '-',
      completedTime: (content && r.submitTime) ? r.submitTime : (isClosed ? (r.closeTime || '-') : '-'),
      dueDate: r.dueDate || '-',
      status,
      operator: isClosed && r.closeTime ? '系统' : '-',
    }
  })
})

const stageStatusTag = (status: EightDTimeline['status']) => {
  const map: Record<string, { type: string; label: string }> = {
    completed: { type: 'success', label: '已完成' },
    in_progress: { type: 'warning', label: '进行中' },
    pending: { type: 'info', label: '待处理' },
    overdue: { type: 'danger', label: '已逾期' },
  }
  return map[status] || { type: 'info', label: status }
}

const saveCurrentStep = async () => {
  saving.value = true
  try {
    await eightDApi.update(id.value, { [currentField.value]: form[currentField.value as keyof FormData] })
    ElMessage.success(`${steps[activeStep.value].title} 已保存`)
    if (report.value) {
      (report.value as any)[currentField.value] = form[currentField.value as keyof FormData]
    }
  } catch { /* handled */ }
  finally { saving.value = false }
}

const saveAll = async () => {
  saving.value = true
  try {
    await eightDApi.update(id.value, { ...form })
    ElMessage.success('8D报告全部保存')
  } catch { /* handled */ }
  finally { saving.value = false }
}

const handleSubmit = async () => {
  try {
    await ElMessageBox.confirm('提交后不可编辑，确认提交8D整改报告？', '确认提交', { type: 'info' })
    await eightDApi.submit(id.value)
    ElMessage.success('8D报告已提交')
    loadDetail()
  } catch { /* cancel */ }
}

const nextStep = () => {
  if (activeStep.value < steps.length - 1) activeStep.value++
}

const prevStep = () => {
  if (activeStep.value > 0) activeStep.value--
}

onMounted(loadDetail)
</script>

<template>
  <PageContainer :title="`8D报告编辑 - ${report?.reportNo || ''}`" subtitle="按D1-D8阶段逐步填写整改报告">
    <template #actions>
      <el-button @click="router.back()">返回</el-button>
      <el-button v-if="report?.reportStatus === 0" type="primary" @click="saveAll">全部保存</el-button>
      <el-button v-if="report?.reportStatus === 0" type="success" @click="handleSubmit">提交报告</el-button>
    </template>

    <el-row v-loading="loading" :gutter="24">
      <!-- 左侧：阶段导航+表单 -->
      <el-col :span="16">
        <el-card>
          <el-steps :active="activeStep" finish-status="success" align-center style="margin-bottom: 24px">
            <el-step v-for="(s, i) in steps" :key="s.key" :title="s.title.replace('D', '').split(' ')[0]" @click="activeStep = i" style="cursor: pointer" />
          </el-steps>

          <div class="step-form">
            <div class="step-header">
              <h3>{{ steps[activeStep].title }}</h3>
              <p class="step-desc">{{ steps[activeStep].desc }}</p>
            </div>

            <!-- D1 团队组建 -->
            <el-input
              v-if="activeStep === 0"
              v-model="form.d1Team"
              type="textarea"
              :rows="6"
              placeholder="列出跨职能团队成员：姓名、部门、角色。&#10;例如：&#10;组长：张三 - 质量部&#10;成员：李四 - 生产部、王五 - 技术部"
            />

            <!-- D2 问题描述 -->
            <el-input
              v-if="activeStep === 1"
              v-model="form.d2Problem"
              type="textarea"
              :rows="8"
              placeholder="使用5W2H方法描述问题：&#10;What - 什么问题？&#10;Who - 谁发现的？&#10;When - 何时发生？&#10;Where - 在哪里发生？&#10;Why - 为什么是问题？&#10;How - 如何发现的？&#10;How many - 影响范围？"
            />

            <!-- D3 遏制措施 -->
            <el-input
              v-if="activeStep === 2"
              v-model="form.d3Containment"
              type="textarea"
              :rows="6"
              placeholder="描述临时遏制措施：&#10;1. 隔离/标识不合格品&#10;2. 通知相关部门&#10;3. 临时检查方案&#10;4. 客户沟通（如涉及）"
            />

            <!-- D4 根因分析 -->
            <el-input
              v-if="activeStep === 3"
              v-model="form.d4RootCause"
              type="textarea"
              :rows="8"
              placeholder="使用鱼骨图/5Why等方法分析根本原因：&#10;1. 直接原因&#10;2. 根本原因&#10;3. 系统原因&#10;4. 验证方法"
            />

            <!-- D5 纠正措施 -->
            <el-input
              v-if="activeStep === 4"
              v-model="form.d5CorrectiveAction"
              type="textarea"
              :rows="8"
              placeholder="制定永久性纠正措施：&#10;1. 措施描述&#10;2. 责任部门/人&#10;3. 计划完成日期&#10;4. 所需资源"
            />

            <!-- D6 验证措施 -->
            <el-input
              v-if="activeStep === 5"
              v-model="form.d6ValidateAction"
              type="textarea"
              :rows="8"
              placeholder="验证纠正措施的有效性：&#10;1. 验证方法&#10;2. 验证数据&#10;3. 验证结论&#10;4. 是否需要调整措施"
            />

            <!-- D7 预防措施 -->
            <el-input
              v-if="activeStep === 6"
              v-model="form.d7PreventAction"
              type="textarea"
              :rows="6"
              placeholder="防止再发生的措施：&#10;1. FMEA更新&#10;2. 控制计划修订&#10;3. 作业指导书更新&#10;4. 培训计划&#10;5. 系统/流程变更"
            />

            <!-- D8 总结关闭 -->
            <el-input
              v-if="activeStep === 7"
              v-model="form.d8CloseSummary"
              type="textarea"
              :rows="6"
              placeholder="项目总结与关闭：&#10;1. 成果总结&#10;2. 经验教训&#10;3. 横向展开计划&#10;4. 团队表彰"
            />

            <!-- 截止日期（在D1阶段显示） -->
            <el-form-item v-if="activeStep === 0" label="截止日期" style="margin-top: 16px">
              <el-date-picker v-model="form.dueDate" type="date" placeholder="选择截止日期" value-format="YYYY-MM-DD" />
            </el-form-item>

            <div class="step-actions">
              <el-button :disabled="activeStep === 0" @click="prevStep">上一步</el-button>
              <el-button v-if="report?.reportStatus === 0" type="primary" :loading="saving" @click="saveCurrentStep">保存当前</el-button>
              <el-button v-if="activeStep < steps.length - 1" type="primary" @click="nextStep">下一步</el-button>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧：时间线 -->
      <el-col :span="8">
        <el-card>
          <template #header>
            <span style="font-weight: 600">阶段节点追踪</span>
            <el-tag v-if="report" size="small" style="margin-left: 8px">
              <StatusTag :value="report.reportStatus" prefix="8D" />
            </el-tag>
          </template>
          <el-timeline>
            <el-timeline-item
              v-for="item in buildTimeline"
              :key="item.stage"
              :timestamp="item.completedTime !== '-' ? item.completedTime : ''"
              :color="item.status === 'completed' ? '#67c23a' : item.status === 'overdue' ? '#f56c6c' : item.status === 'in_progress' ? '#e6a23c' : '#909399'"
              :type="item.status === 'completed' ? 'success' : item.status === 'overdue' ? 'danger' : item.status === 'in_progress' ? 'warning' : 'info'"
              :hollow="item.status === 'pending'"
            >
              <div class="timeline-item">
                <span class="timeline-title">{{ item.stageName }}</span>
                <el-tag :type="stageStatusTag(item.status).type as any" size="small">{{ stageStatusTag(item.status).label }}</el-tag>
                <div v-if="item.content !== '-'" class="timeline-content">{{ item.content }}</div>
                <div v-if="item.status === 'overdue' && item.dueDate !== '-'" class="timeline-overdue">
                  <el-icon><WarningFilled /></el-icon> 截止 {{ item.dueDate }}，已逾期
                </div>
              </div>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
    </el-row>
  </PageContainer>
</template>

<style scoped>
.step-form {
  padding: 0 20px;
}
.step-header {
  margin-bottom: 16px;
}
.step-header h3 {
  margin: 0 0 4px 0;
  font-size: 18px;
  color: #303133;
}
.step-desc {
  margin: 0;
  color: #909399;
  font-size: 13px;
}
.step-actions {
  margin-top: 20px;
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}
.timeline-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.timeline-title {
  font-weight: 500;
  font-size: 13px;
}
.timeline-content {
  font-size: 12px;
  color: #606266;
  word-break: break-all;
  max-height: 60px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
}
.timeline-overdue {
  font-size: 12px;
  color: #f56c6c;
  display: flex;
  align-items: center;
  gap: 4px;
}
</style>