<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { DataBoard, Document, Goods, Money, OfficeBuilding, SetUp, Van, Tools, Collection, Box, Connection } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import NotificationBell from '@/components/business/NotificationBell.vue'
import GlobalSearch from '@/components/business/GlobalSearch.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const menus = [
  { path: '/purchasing/dashboard', title: '工作台', icon: DataBoard },
  { path: '/purchasing/suppliers', title: '供应商管理', icon: OfficeBuilding },
  { path: '/purchasing/rfq', title: 'RFQ询价', icon: Collection },
  { path: '/purchasing/quotes', title: '报价对比', icon: Money },
  { path: '/purchasing/orders', title: '采购订单', icon: Document },
  { path: '/purchasing/asn', title: '物流与交付', icon: Van },
  { path: '/purchasing/vmi', title: 'VMI预测', icon: Box },
  { path: '/purchasing/quality', title: '质量协同', icon: SetUp },
  { path: '/purchasing/settlements', title: '财务结算', icon: Goods },
  { path: '/purchasing/integration', title: '集成网关', icon: Connection },
  { path: '/purchasing/settings', title: '系统配置', icon: Tools },
]

const activeMenu = computed(() => route.meta.activeMenu as string || route.path)

const logout = () => {
  userStore.logout()
  router.replace('/login')
}
</script>

<template>
  <el-container class="layout-shell">
    <el-aside width="248px" class="layout-aside">
      <div class="brand">
        <div class="brand-mark"><el-icon><Goods /></el-icon></div>
        <div>
          <div class="brand-title">供应商协同</div>
          <div class="brand-subtitle">Supplier Hub</div>
        </div>
      </div>
      <el-menu :default-active="activeMenu" router background-color="transparent" text-color="#c9d7ee" active-text-color="#ffffff">
        <el-menu-item v-for="item in menus" :key="item.path" :index="item.path">
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.title }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="layout-header">
        <div>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item>采购方后台</el-breadcrumb-item>
            <el-breadcrumb-item>{{ route.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-user">
          <GlobalSearch />
          <NotificationBell />
          <el-tag type="success" effect="light">实时数据</el-tag>
          <div class="avatar" :style="{ background: userStore.user?.avatarColor || '#1f5eff' }">{{ userStore.user?.realName?.slice(0, 1) || '管' }}</div>
          <div>
            <div class="user-name">{{ userStore.user?.realName }}</div>
            <div class="user-role">{{ userStore.user?.department }}</div>
          </div>
          <el-button link type="primary" @click="logout">退出</el-button>
        </div>
      </el-header>
      <el-main class="layout-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.layout-shell {
  height: 100vh;
}

.layout-aside {
  background: linear-gradient(180deg, #10233f, #071323);
  box-shadow: 8px 0 28px rgba(8, 22, 42, 0.18);
}

.brand {
  display: flex;
  gap: 12px;
  align-items: center;
  height: 76px;
  padding: 0 22px;
  color: #ffffff;
}

.brand-mark {
  display: grid;
  width: 42px;
  height: 42px;
  place-items: center;
  background: linear-gradient(135deg, #1f5eff, #00c2a8);
  border-radius: 12px;
}

.brand-title {
  font-size: 18px;
  font-weight: 800;
}

.brand-subtitle {
  font-size: 12px;
  color: #8ea7c7;
}

.layout-aside :deep(.el-menu) {
  border-right: 0;
}

.layout-aside :deep(.el-menu-item) {
  height: 48px;
  margin: 6px 12px;
  border-radius: 12px;
}

.layout-aside :deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, #1f5eff, #0bb783);
}

.layout-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
  background: rgba(255, 255, 255, 0.94);
  border-bottom: 1px solid #e4ebf3;
}

.header-user {
  display: flex;
  gap: 12px;
  align-items: center;
}

.avatar {
  display: grid;
  width: 36px;
  height: 36px;
  font-weight: 700;
  color: #ffffff;
  place-items: center;
  border-radius: 50%;
}

.user-name {
  font-size: 14px;
  font-weight: 700;
}

.user-role {
  font-size: 12px;
  color: #718096;
}

.layout-main {
  padding: 22px;
  overflow: auto;
  background: radial-gradient(circle at top right, rgba(31, 94, 255, 0.08), transparent 30%), #eef3f8;
}
</style>
