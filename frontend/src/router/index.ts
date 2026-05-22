import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'
import PurchasingLayout from '@/layouts/PurchasingLayout.vue'
import LoginView from '@/views/LoginView.vue'
import DashboardView from '@/views/purchasing/DashboardView.vue'
import SupplierListView from '@/views/purchasing/SupplierListView.vue'
import OrderListView from '@/views/purchasing/OrderListView.vue'
import AsnListView from '@/views/purchasing/AsnListView.vue'
import AsnCreateView from '@/views/purchasing/AsnCreateView.vue'
import QualityListView from '@/views/purchasing/QualityListView.vue'
import SettlementListView from '@/views/purchasing/SettlementListView.vue'
import BusinessDetailView from '@/views/purchasing/BusinessDetailView.vue'
import ForbiddenView from '@/views/error/ForbiddenView.vue'
import NotFoundView from '@/views/error/NotFoundView.vue'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/purchasing/dashboard',
  },
  {
    path: '/login',
    name: 'login',
    component: LoginView,
    meta: { title: '登录', public: true },
  },
  {
    path: '/purchasing',
    component: PurchasingLayout,
    redirect: '/purchasing/dashboard',
    meta: { userType: 'internal' },
    children: [
      { path: 'dashboard', component: DashboardView, meta: { title: '工作台', permission: 'dashboard:view' } },
      { path: 'suppliers', component: SupplierListView, meta: { title: '供应商管理', permission: 'supplier:view' } },
      { path: 'suppliers/:id', component: BusinessDetailView, meta: { title: '供应商详情', moduleName: '供应商详情', activeMenu: '/purchasing/suppliers', permission: 'supplier:view' } },
      { path: 'orders', component: OrderListView, meta: { title: '采购订单', permission: 'order:view' } },
      { path: 'orders/:id', component: BusinessDetailView, meta: { title: '订单详情', moduleName: '订单详情', activeMenu: '/purchasing/orders', permission: 'order:view' } },
      { path: 'asn', component: AsnListView, meta: { title: '物流与交付', permission: 'asn:view' } },
      { path: 'asn/create', component: AsnCreateView, meta: { title: '创建 ASN', activeMenu: '/purchasing/asn', permission: 'asn:create' } },
      { path: 'asn/:id', component: BusinessDetailView, meta: { title: 'ASN详情', moduleName: 'ASN详情', activeMenu: '/purchasing/asn', permission: 'asn:view' } },
      { path: 'quality', component: QualityListView, meta: { title: '质量协同', permission: 'quality:view' } },
      { path: 'quality/:id', component: BusinessDetailView, meta: { title: '质量详情', moduleName: '质量详情', activeMenu: '/purchasing/quality', permission: 'quality:view' } },
      { path: 'settlements', component: SettlementListView, meta: { title: '财务结算', permission: 'settlement:view' } },
      { path: 'settlements/:id', component: BusinessDetailView, meta: { title: '对账详情', moduleName: '对账详情', activeMenu: '/purchasing/settlements', permission: 'settlement:view' } },
    ],
  },
  {
    path: '/403',
    component: ForbiddenView,
    meta: { title: '无权限', public: true },
  },
  {
    path: '/:pathMatch(.*)*',
    component: NotFoundView,
    meta: { title: '页面不存在', public: true },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach(async (to) => {
  const userStore = useUserStore()
  if (to.meta.public) return true
  if (!userStore.token) return `/login?redirect=${to.fullPath}`
  if (!userStore.user) await userStore.loadCurrentUser()
  const permission = to.meta.permission as string | undefined
  if (permission && !userStore.permissions.includes(permission)) return '/403'
  document.title = `${to.meta.title || '供应商协同'} - 供应商协同系统`
  return true
})

export default router
