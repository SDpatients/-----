<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const goDashboard = () => {
  console.debug('[ForbiddenView] 点击返回工作台', {
    userType: userStore.user?.userType,
    permissions: userStore.permissions,
    token: Boolean(userStore.token),
  })

  const userType = userStore.user?.userType
  if (userType === 'supplier') {
    console.debug('[ForbiddenView] 供应商用户，跳转 /supplier/dashboard')
    router.push('/supplier/dashboard')
  } else if (userType === 'internal') {
    console.debug('[ForbiddenView] 内部用户，跳转 /purchasing/dashboard')
    router.push('/purchasing/dashboard')
  } else if (!userStore.token) {
    console.debug('[ForbiddenView] 未登录，跳转 /login')
    router.push('/login')
  } else {
    console.debug('[ForbiddenView] 未知用户类型，跳转 /login')
    router.push('/login')
  }
}
</script>

<template>
  <div class="error-page"><h1>403</h1><p>当前账号暂无访问权限</p><el-button type="primary" @click="goDashboard">返回工作台</el-button></div>
</template>

<style scoped>
.error-page { display: grid; height: 100vh; place-content: center; text-align: center; background: #eef3f8; }
.error-page h1 { margin: 0; font-size: 92px; color: #1f5eff; }
.error-page p { margin: 8px 0 24px; color: #718096; }
</style>
