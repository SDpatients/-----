<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { DataBoard, DocumentChecked, Folder, Goods, Money, Tickets, Van, Collection } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import NotificationBell from '@/components/business/NotificationBell.vue'
import GlobalSearch from '@/components/business/GlobalSearch.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const menus = [
  { path: '/supplier/dashboard', title: '门户工作台', icon: DataBoard },
  { path: '/supplier/orders', title: '订单中心', icon: Tickets },
  { path: '/supplier/rfq', title: 'RFQ报价', icon: Collection },
  { path: '/supplier/deliveries', title: '发货中心', icon: Van },
  { path: '/supplier/profile', title: '资料中心', icon: Folder },
]

const activeMenu = computed(() => route.meta.activeMenu as string || route.path)

const logout = () => {
  userStore.logout()
  router.replace('/login')
}
</script>

<template>
  <el-container class="supplier-shell">
    <el-aside width="236px" class="supplier-aside">
      <div class="supplier-brand">
        <div class="supplier-mark"><el-icon><Goods /></el-icon></div>
        <div><div class="supplier-title">供应商门户</div><div class="supplier-subtitle">Vendor Workspace</div></div>
      </div>
      <el-menu :default-active="activeMenu" router background-color="transparent" text-color="#d8f7ef" active-text-color="#073b35">
        <el-menu-item v-for="item in menus" :key="item.path" :index="item.path">
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.title }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="supplier-header">
        <el-breadcrumb separator="/"><el-breadcrumb-item>供应商门户</el-breadcrumb-item><el-breadcrumb-item>{{ route.meta.title }}</el-breadcrumb-item></el-breadcrumb>
        <div class="supplier-user">
          <GlobalSearch />
          <NotificationBell />
          <el-tag type="success">{{ userStore.user?.department }}</el-tag>
          <div class="avatar" :style="{ background: userStore.user?.avatarColor || '#0bb783' }">{{ userStore.user?.realName?.slice(0, 1) }}</div>
          <el-button link type="primary" @click="logout">退出</el-button>
        </div>
      </el-header>
      <el-main class="supplier-main"><router-view /></el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.supplier-shell { height: 100vh; }
.supplier-aside { background: linear-gradient(180deg, #064e45, #072b28); box-shadow: 8px 0 28px rgba(8, 42, 37, 0.18); }
.supplier-brand { display: flex; gap: 12px; align-items: center; height: 76px; padding: 0 22px; color: #ffffff; }
.supplier-mark { display: grid; width: 42px; height: 42px; place-items: center; background: linear-gradient(135deg, #0bb783, #b8ff6a); border-radius: 12px; }
.supplier-title { font-size: 18px; font-weight: 800; }
.supplier-subtitle { font-size: 12px; color: #9ad7ca; }
.supplier-aside :deep(.el-menu) { border-right: 0; }
.supplier-aside :deep(.el-menu-item) { height: 48px; margin: 6px 12px; border-radius: 12px; }
.supplier-aside :deep(.el-menu-item.is-active) { background: linear-gradient(135deg, #d9fff2, #8ff4e3); }
.supplier-header { display: flex; align-items: center; justify-content: space-between; height: 64px; background: rgba(255, 255, 255, 0.94); border-bottom: 1px solid #dcefe9; }
.supplier-user { display: flex; gap: 12px; align-items: center; }
.avatar { display: grid; width: 36px; height: 36px; font-weight: 700; color: #ffffff; place-items: center; border-radius: 50%; }
.supplier-main { padding: 22px; overflow: auto; background: radial-gradient(circle at top right, rgba(11, 183, 131, 0.12), transparent 30%), #edf7f4; }
</style>
