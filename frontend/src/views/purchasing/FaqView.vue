<script setup lang="ts">
import { ref, computed } from 'vue'
import { QuestionFilled } from '@element-plus/icons-vue'
import PageContainer from '@/components/common/PageContainer.vue'

interface FaqItem {
  question: string
  answer: string
  category: string
}

const faqs: FaqItem[] = [
  {
    category: '供应商管理',
    question: '供应商的风险等级是如何计算的？',
    answer:
      '供应商的风险等级是根据绩效评分自动判定的：\n\n' +
      '● 绩效评分 ≥ 80 分 → 低风险（绿色标签），表示该供应商整体表现良好，可以放心合作。\n' +
      '● 绩效评分 < 80 分 → 中风险（黄色标签），表示该供应商在某些方面需要关注和改善。\n\n' +
      '此外，如果供应商被列入黑名单，则直接显示为"已拉黑"（红色标签），无论绩效评分如何。\n\n' +
      '绩效评分每月自动计算一次，由质量评分、交付评分、服务评分和价格评分四个维度加权得出，具体计算方式请参考"供应商绩效评分是怎么算出来的"。',
  },
  {
    category: '供应商管理',
    question: '供应商绩效评分是怎么算出来的？',
    answer:
      '系统每月1号自动计算上个月的供应商绩效评分，满分100分，由四个维度加权得出：\n\n' +
      '1️⃣ 质量评分（占30%）—— 满分100分，每出现1次不合格报告（NCR）扣5分，扣到0分为止。\n' +
      '2️⃣ 交付评分（占30%）—— 满分100分，按收货准时率折算。例如10次送货中9次准时，交付评分就是90分。\n' +
      '3️⃣ 服务评分（占20%）—— 基础分80分，每出现1次不合格报告扣10分，扣到0分为止。\n' +
      '4️⃣ 价格评分（占20%）—— 目前默认满分100分，后续会根据价格竞争力进行调整。\n\n' +
      '综合评分 = 质量评分 × 30% + 交付评分 × 30% + 服务评分 × 20% + 价格评分 × 20%\n\n' +
      '举例：某供应商质量96分、交付94分、服务92分、价格88分，则综合评分 = 96×0.3 + 94×0.3 + 92×0.2 + 88×0.2 = 93分。',
  },
  {
    category: '供应商管理',
    question: '供应商如何注册和准入？',
    answer:
      '供应商准入需要经过以下步骤：\n\n' +
      '1️⃣ 注册 —— 供应商在注册页面填写公司名称、统一社会信用代码等基本信息，提交后状态变为"已注册"。\n' +
      '2️⃣ 初审 —— 采购方对供应商提交的信息进行初步审核。\n' +
      '3️⃣ 复审 —— 通过初审后进入复审阶段，采购方会进一步核实供应商的资质和能力。\n' +
      '4️⃣ 终审 —— 复审通过后进入终审，由更高级别的管理人员审批。\n' +
      '5️⃣ 准入 —— 终审通过后，供应商状态变为"已准入"，可以正式开展业务合作。\n\n' +
      '如果在审核过程中发现供应商不符合要求，可以驳回，供应商需要补充材料后重新提交。\n\n' +
      '供应商还需要上传相关资质文件（如营业执照、ISO认证等），资质到期前系统会自动提醒。',
  },
  {
    category: '供应商管理',
    question: '供应商黑名单是什么？怎么产生的？',
    answer:
      '黑名单是对严重违约或存在重大风险的供应商进行限制的机制。被列入黑名单后：\n\n' +
      '● 该供应商无法参与新的询价和报价。\n' +
      '● 正在进行的订单和业务会收到预警提醒。\n' +
      '● 在供应商列表中会显示红色"已拉黑"标签。\n\n' +
      '黑名单通常在以下情况下产生：\n' +
      '● 历史交付严重违约（如多次逾期、大批量不合格）。\n' +
      '● 质量问题长期未改善。\n' +
      '● 提供虚假资质或信息。\n\n' +
      '黑名单可以设置生效时间和解除时间。到期后自动解除，也可以由管理员手动提前解除。',
  },
  {
    category: '采购订单',
    question: '采购订单的状态有哪些？分别代表什么意思？',
    answer:
      '采购订单从创建到完成会经历以下状态：\n\n' +
      '● 草稿 —— 订单刚创建，尚未发送给供应商，采购方还可以修改。\n' +
      '● 待确认 —— 订单已发送给供应商，等待供应商确认接单。\n' +
      '● 已确认 —— 供应商已确认接单，双方按约定执行。\n' +
      '● 部分发货 —— 供应商已发出部分货物，还有剩余货物待发。\n' +
      '● 已完成 —— 订单所有货物已交付并验收完毕。\n' +
      '● 已取消 —— 采购方主动取消了该订单。\n' +
      '● 已拒单 —— 供应商拒绝接单（如产能不足、价格异议等）。\n\n' +
      '💡 小提示：如果订单超过2天未被供应商确认，系统会自动发送提醒通知。',
  },
  {
    category: '采购订单',
    question: '订单上的风险等级（高/中/低）是怎么判定的？',
    answer:
      '订单的风险等级是根据交货日期与当前日期的差距自动判定的：\n\n' +
      '🔴 高风险 —— 交货日期已经过了，但订单还未完成。说明已经逾期，需要立即关注和处理。\n' +
      '🟡 中风险 —— 交货日期在7天以内即将到期，但订单尚未完成。提醒您需要跟进进度。\n' +
      '🟢 低风险 —— 交货日期还有7天以上，或者订单已经取消/拒单，没有风险。\n\n' +
      '系统每天早上8点会自动扫描所有订单，对即将逾期和已经逾期的订单发送预警通知。',
  },
  {
    category: '物流与交付',
    question: '什么是ASN（发货通知）？',
    answer:
      'ASN的全称是 Advanced Shipping Notice，即"提前发货通知"，是供应商在发货前向采购方发送的送货预告。\n\n' +
      'ASN包含以下关键信息：\n' +
      '● 送货单号 —— 唯一标识这次发货。\n' +
      '● 物料信息 —— 本次发货的物料编码、名称和数量。\n' +
      '● 计划送货日期 —— 预计到达仓库的时间。\n' +
      '● 包装和运输信息 —— 箱号、托盘号等。\n\n' +
      '通过ASN，仓库可以提前做好收货准备（安排人手、库位等），避免货物到了却没人接收的情况。\n\n' +
      'ASN的状态流转：待发货 → 在途 → 已到达 → 已收货 → 已完成。',
  },
  {
    category: '物流与交付',
    question: '收货时发现数量不符或质量问题怎么办？',
    answer:
      '收货时如果发现异常，可以按以下方式处理：\n\n' +
      '📦 数量不符：\n' +
      '● 在收货记录中填写实际收货数量，系统会自动计算差异数量。\n' +
      '● 需要填写差异原因（如运输破损导致短收等）。\n' +
      '● 系统会自动触发质检流程。\n\n' +
      '🔍 质量问题：\n' +
      '● 收货时可以记录拒收数量和拒收原因。\n' +
      '● 系统会自动创建质检单，由质检部门进行检验。\n' +
      '● 如果质检不合格，会生成不合格报告（NCR），供应商需要整改。\n\n' +
      '💡 所有收货差异和质量问题都会影响供应商的绩效评分，所以请如实记录。',
  },
  {
    category: '质量协同',
    question: '质量检验不合格会怎样？',
    answer:
      '当质检结果为"不合格"时，系统会按以下流程处理：\n\n' +
      '1️⃣ 生成NCR（不合格报告） —— 系统自动记录不合格的物料、数量和原因。\n' +
      '2️⃣ 通知供应商 —— 供应商会收到站内信和邮件通知，了解不合格的详情。\n' +
      '3️⃣ 供应商整改 —— 供应商需要在规定时间内提交整改方案。\n' +
      '4️⃣ 8D报告 —— 对于较严重的质量问题，供应商需要提交8D报告（一种结构化的问题解决方法）。\n' +
      '5️⃣ 验证关闭 —— 采购方确认整改有效后，关闭NCR。\n\n' +
      '质检结果有三种：\n' +
      '● 合格 —— 正常入库。\n' +
      '● 不合格 —— 拒收，触发NCR流程。\n' +
      '● 让步接收 —— 有轻微瑕疵但经审批同意使用，仍会记录在案。\n\n' +
      '⚠️ 每次不合格都会影响供应商的质量评分和服务评分。',
  },
  {
    category: '财务结算',
    question: '对账单有异议怎么办？',
    answer:
      '如果您对对账单上的金额或明细有疑问，可以按以下步骤处理：\n\n' +
      '1️⃣ 在对账单详情中点击"提出异议"。\n' +
      '2️⃣ 填写异议原因和具体说明（如某笔费用计算有误、某笔送货未收到等）。\n' +
      '3️⃣ 提交后，对账单状态变为"有异议"，采购方会收到通知。\n' +
      '4️⃣ 双方沟通协商，采购方可以修改对账单后重新发送。\n' +
      '5️⃣ 确认无误后，对账单状态变为"已确认"，进入开票和付款环节。\n\n' +
      '对账单的状态流转：草稿 → 已发送 → 已确认/有异议 → 已完成。\n\n' +
      '💡 建议在提出异议时尽量详细说明原因，并附上相关证据（如签收单、质检报告等），这样处理效率更高。',
  },
  {
    category: '财务结算',
    question: '发票和付款的流程是怎样的？',
    answer:
      '对账确认后的发票和付款流程如下：\n\n' +
      '📄 发票流程：\n' +
      '● 待开票 → 供应商上传发票 → 已上传 → 财务验真 → 已验真 → 税务认证 → 已认证\n' +
      '● 如果发票信息有误，可以作废后重新开具。\n\n' +
      '💰 付款流程：\n' +
      '● 待付款 → 部分付款/全额付款 → 已付款\n' +
      '● 如果发现问题，财务可以拒绝付款。\n\n' +
      '💡 发票上传后系统会自动进行验真，确保发票的真实性和有效性。',
  },
  {
    category: '询价与报价',
    question: 'RFQ询价流程是怎样的？',
    answer:
      'RFQ（Request For Quotation）是采购方向供应商发起询价的流程：\n\n' +
      '1️⃣ 创建RFQ —— 采购方填写询价标题、物料需求、报价截止日期等信息。\n' +
      '2️⃣ 发布RFQ —— 发布后，被邀请的供应商会收到通知。\n' +
      '3️⃣ 供应商报价 —— 供应商在截止日期前提交报价（含单价、交期等）。\n' +
      '4️⃣ 报价截止 —— 到达截止日期后，不再接受新的报价。\n' +
      '5️⃣ 报价对比 —— 采购方可以在"报价对比"页面横向比较各供应商的报价。\n' +
      '6️⃣ 定价 —— 采购方选择最优报价，完成定价。\n\n' +
      '💡 报价对比功能支持按价格、交期、质量评分等多维度排序，帮助您做出最佳决策。',
  },
  {
    category: 'VMI与预测',
    question: 'VMI预测是什么意思？',
    answer:
      'VMI（Vendor Managed Inventory）即"供应商管理库存"，是一种库存管理模式：\n\n' +
      '● 采购方提供未来一段时间的需求预测，供应商根据预测主动备货和补货。\n' +
      '● 库存放在采购方仓库，但由供应商负责管理和补充。\n' +
      '● 只有在实际领用/消耗时才结算费用。\n\n' +
      '系统中的VMI功能包括：\n' +
      '● 需求预测 —— 采购方发布未来物料需求，供应商可以查看并响应。\n' +
      '● 库存监控 —— 实时查看VMI仓库的库存数量、安全库存和最大库存。\n' +
      '● 库存预警 —— 当库存低于安全库存时，系统自动发出预警（黄色标签）；当库存为零时显示缺料（红色标签）。\n\n' +
      '💡 VMI模式可以减少采购方的库存压力，同时帮助供应商更好地安排生产计划。',
  },
  {
    category: '系统通知',
    question: '系统会自动发送哪些通知提醒？',
    answer:
      '系统会在以下关键业务节点自动发送通知（通过站内信，部分支持邮件和企业微信）：\n\n' +
      '📋 订单相关：\n' +
      '● 新订单下发时通知供应商。\n' +
      '● 订单超过2天未确认，提醒供应商尽快处理。\n' +
      '● 订单交期已过但未完成，发出逾期预警。\n\n' +
      '🚚 物流相关：\n' +
      '● 送货计划已逾期但未发货，发出逾期预警。\n' +
      '● 送货计划在未来1-2天内到期，发出即将到期提醒。\n\n' +
      '🔍 质量相关：\n' +
      '● 质检不合格时通知供应商和采购质量工程师。\n' +
      '● NCR创建时通知相关方。\n\n' +
      '💰 财务相关：\n' +
      '● 对账单发送时通知供应商。\n' +
      '● 对账单有异议时通知采购方。\n\n' +
      '🔔 所有通知都可以在页面右上角的消息铃铛中查看，也可以进入消息中心统一管理。',
  },
  {
    category: '系统通知',
    question: '工作台上的风险预警指标代表什么？',
    answer:
      '工作台（仪表盘）上展示的风险预警指标有四类：\n\n' +
      '⚠️ 待确认订单积压 —— 状态为"待确认"的订单数量。如果积压过多，说明供应商响应不及时。\n\n' +
      '🔴 计划送货已逾期 —— 送货通知的计划送货日期已过，但实际还未发货的数量。这是最紧急的风险，需要立即处理。\n\n' +
      '🔴 不合格质检单 —— 质检结果为"不合格"或"让步接收"的单据数量。说明近期存在质量问题。\n\n' +
      '⚠️ 有异议对账单 —— 供应商对对账金额提出异议的数量。需要尽快协商解决，避免影响付款。\n\n' +
      '💡 建议每天上班后先查看工作台的风险预警，优先处理红色（危险）级别的项目。',
  },
]

const categories = computed(() => {
  const set = new Set(faqs.map((f) => f.category))
  return Array.from(set)
})

const activeCategory = ref('全部')
const searchKeyword = ref('')

const filteredFaqs = computed(() => {
  let result = faqs
  if (activeCategory.value !== '全部') {
    result = result.filter((f) => f.category === activeCategory.value)
  }
  if (searchKeyword.value.trim()) {
    const kw = searchKeyword.value.trim().toLowerCase()
    result = result.filter(
      (f) =>
        f.question.toLowerCase().includes(kw) || f.answer.toLowerCase().includes(kw),
    )
  }
  return result
})

const activeNames = ref<string[]>([])
</script>

<template>
  <PageContainer title="常见问题" subtitle="关于供应商协同系统的使用疑问，在这里找到答案">
    <template #actions>
      <el-input
        v-model="searchKeyword"
        placeholder="搜索问题关键词"
        clearable
        style="width: 260px"
        :prefix-icon="QuestionFilled"
      />
    </template>

    <div class="faq-tabs">
      <el-check-tag
        :checked="activeCategory === '全部'"
        @change="activeCategory = '全部'"
        class="faq-tab"
      >全部</el-check-tag>
      <el-check-tag
        v-for="cat in categories"
        :key="cat"
        :checked="activeCategory === cat"
        @change="activeCategory = cat"
        class="faq-tab"
      >{{ cat }}</el-check-tag>
    </div>

    <el-empty v-if="filteredFaqs.length === 0" description="没有找到匹配的问题，请尝试其他关键词" />

    <el-collapse v-else v-model="activeNames" class="faq-collapse">
      <el-collapse-item
        v-for="(faq, idx) in filteredFaqs"
        :key="idx"
        :name="String(idx)"
      >
        <template #title>
          <div class="faq-title">
            <el-tag size="small" type="info" effect="plain" class="faq-cat-tag">{{ faq.category }}</el-tag>
            <span class="faq-question">{{ faq.question }}</span>
          </div>
        </template>
        <div class="faq-answer" v-html="faq.answer.replace(/\n/g, '<br/>')" />
      </el-collapse-item>
    </el-collapse>
  </PageContainer>
</template>

<style scoped>
.faq-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 20px;
}

.faq-tab {
  font-size: 14px;
  padding: 6px 16px;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.2s;
}

.faq-collapse {
  border: none;
}

.faq-collapse :deep(.el-collapse-item__header) {
  height: auto;
  min-height: 52px;
  padding: 10px 0;
  line-height: 1.6;
  border-bottom: 1px solid #eef2f7;
  font-size: 15px;
}

.faq-collapse :deep(.el-collapse-item__wrap) {
  border-bottom: none;
}

.faq-collapse :deep(.el-collapse-item__content) {
  padding: 8px 0 20px;
}

.faq-title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.faq-cat-tag {
  flex-shrink: 0;
  border-radius: 12px;
}

.faq-question {
  font-weight: 600;
  color: #1a2b4c;
}

.faq-answer {
  font-size: 14px;
  line-height: 2;
  color: #4a5568;
  padding: 12px 20px;
  background: #f8fbff;
  border-radius: 10px;
  border-left: 3px solid #1f5eff;
}
</style>
