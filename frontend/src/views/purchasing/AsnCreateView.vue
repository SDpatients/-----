<script setup lang="ts">
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import PageContainer from '@/components/common/PageContainer.vue'

const router = useRouter()
const form = reactive({ orderNo: 'PO-202605-0002', supplierName: '星河电子科技股份有限公司', warehouse: '华南中心仓', shipDate: '2026-05-22', eta: '2026-05-24', quantity: 800 })

const submit = () => {
  ElMessage.success('ASN 虚拟创建成功')
  router.push('/purchasing/asn')
}
</script>

<template>
  <PageContainer title="创建 ASN" subtitle="模拟供应商发货通知创建、收货仓库与预计到货信息维护">
    <el-form :model="form" label-width="120px" style="max-width: 760px">
      <el-form-item label="订单号"><el-input v-model="form.orderNo" /></el-form-item>
      <el-form-item label="供应商"><el-input v-model="form.supplierName" /></el-form-item>
      <el-form-item label="收货仓库"><el-input v-model="form.warehouse" /></el-form-item>
      <el-form-item label="发货日期"><el-date-picker v-model="form.shipDate" value-format="YYYY-MM-DD" /></el-form-item>
      <el-form-item label="预计到货"><el-date-picker v-model="form.eta" value-format="YYYY-MM-DD" /></el-form-item>
      <el-form-item label="发货数量"><el-input-number v-model="form.quantity" :min="1" /></el-form-item>
      <el-form-item><el-button type="primary" @click="submit">保存 ASN</el-button><el-button @click="router.back()">取消</el-button></el-form-item>
    </el-form>
  </PageContainer>
</template>
