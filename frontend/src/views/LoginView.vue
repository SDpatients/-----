<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const form = reactive({ username: 'admin', password: '123456' })

const submit = async () => {
  loading.value = true
  await userStore.login()
  loading.value = false
  ElMessage.success('登录成功，当前使用虚拟数据')
  router.replace('/purchasing/dashboard')
}
</script>

<template>
  <div class="login-page">
    <div class="login-hero">
      <div class="hero-badge">SCM Collaboration Platform</div>
      <h1>供应商协同系统</h1>
      <p>统一承载供应商准入、订单协同、交付跟踪、质量整改与财务对账，让采购全流程清晰可追溯。</p>
      <div class="hero-grid">
        <div>准入审核</div>
        <div>订单协同</div>
        <div>ASN 收货</div>
        <div>质量闭环</div>
      </div>
    </div>
    <div class="login-card">
      <h2>采购方后台登录</h2>
      <p>第一、二阶段框架演示，任意输入均进入虚拟数据环境。</p>
      <el-form :model="form" label-position="top" size="large" @submit.prevent>
        <el-form-item label="账号">
          <el-input v-model="form.username" placeholder="admin" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password placeholder="123456" />
        </el-form-item>
        <el-button type="primary" size="large" :loading="loading" class="login-button" @click="submit">进入系统</el-button>
      </el-form>
      <div class="login-tip">虚拟账号：admin / 123456</div>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  display: grid;
  grid-template-columns: 1.15fr 420px;
  gap: 60px;
  align-items: center;
  min-height: 100vh;
  padding: 64px 9vw;
  background: linear-gradient(135deg, #071323, #10233f 45%, #0a716b);
}

.login-hero {
  color: #ffffff;
}

.hero-badge {
  display: inline-flex;
  padding: 8px 14px;
  margin-bottom: 22px;
  font-size: 12px;
  color: #8ff4e3;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: 999px;
}

.login-hero h1 {
  margin: 0;
  font-size: 52px;
  font-weight: 900;
  letter-spacing: 2px;
}

.login-hero p {
  max-width: 640px;
  margin: 22px 0 34px;
  font-size: 17px;
  line-height: 1.9;
  color: #c8d8ef;
}

.hero-grid {
  display: grid;
  grid-template-columns: repeat(4, 112px);
  gap: 14px;
}

.hero-grid div {
  padding: 18px 12px;
  text-align: center;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.16);
  border-radius: 16px;
}

.login-card {
  padding: 34px;
  background: rgba(255, 255, 255, 0.96);
  border-radius: 22px;
  box-shadow: 0 30px 80px rgba(0, 0, 0, 0.28);
}

.login-card h2 {
  margin: 0 0 8px;
  font-size: 26px;
  color: #10233f;
}

.login-card p {
  margin: 0 0 28px;
  color: #718096;
}

.login-button {
  width: 100%;
}

.login-tip {
  margin-top: 18px;
  font-size: 13px;
  color: #718096;
  text-align: center;
}
</style>
