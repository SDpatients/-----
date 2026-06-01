import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'
import PurchasingLayout from '@/layouts/PurchasingLayout.vue'
import SupplierLayout from '@/layouts/SupplierLayout.vue'
import LoginView from '@/views/LoginView.vue'
import MessageCenterView from '@/views/MessageCenterView.vue'
import DashboardView from '@/views/purchasing/DashboardView.vue'
import SupplierListView from '@/views/purchasing/SupplierListView.vue'
import OrderListView from '@/views/purchasing/OrderListView.vue'
import OrderChangeListView from '@/views/purchasing/OrderChangeListView.vue'
import OrderSyncStatusView from '@/views/purchasing/OrderSyncStatusView.vue'
import AsnListView from '@/views/purchasing/AsnListView.vue'
import AsnCreateView from '@/views/purchasing/AsnCreateView.vue'
import ScanReceivingView from '@/views/purchasing/ScanReceivingView.vue'
import WriteOffView from '@/views/purchasing/WriteOffView.vue'
import QualityListView from '@/views/purchasing/QualityListView.vue'
import PurchasingQualityView from '@/views/purchasing/PurchasingQualityView.vue'
import IQCStandardView from '@/views/purchasing/IQCStandardView.vue'
import EightDEditView from '@/views/purchasing/EightDEditView.vue'
import SettlementListView from '@/views/purchasing/SettlementListView.vue'
import PurchasingFinanceView from '@/views/purchasing/PurchasingFinanceView.vue'
import RfqListView from '@/views/purchasing/RfqListView.vue'
import RfqDetailView from '@/views/purchasing/RfqDetailView.vue'
import QuoteCompareView from '@/views/purchasing/QuoteCompareView.vue'
import VmiForecastView from '@/views/purchasing/VmiForecastView.vue'
import IntegrationConfigView from '@/views/purchasing/IntegrationConfigView.vue'
import ExchangeRateView from '@/views/purchasing/ExchangeRateView.vue'
import BusinessDetailView from '@/views/purchasing/BusinessDetailView.vue'
import SystemConfigView from '@/views/purchasing/SystemConfigView.vue'
import SupplierDashboardView from '@/views/supplier/SupplierDashboardView.vue'
import SupplierOrderCenterView from '@/views/supplier/SupplierOrderCenterView.vue'
import SupplierDeliveryCenterView from '@/views/supplier/SupplierDeliveryCenterView.vue'
import SupplierDeliveryCreateView from '@/views/supplier/SupplierDeliveryCreateView.vue'
import SupplierQualityCenterView from '@/views/supplier/SupplierQualityCenterView.vue'
import SupplierSettlementCenterView from '@/views/supplier/SupplierSettlementCenterView.vue'
import SupplierRfqQuoteView from '@/views/supplier/SupplierRfqQuoteView.vue'
import SupplierProfileView from '@/views/supplier/SupplierProfileView.vue'
import SupplierRegisterView from '@/views/supplier/SupplierRegisterView.vue'
import BlacklistView from '@/views/purchasing/BlacklistView.vue'
import ForbiddenView from '@/views/error/ForbiddenView.vue'
import NotFoundView from '@/views/error/NotFoundView.vue'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/purchasing/dashboard',
  },
  {
    path: '/orders',
    redirect: '/purchasing/orders',
  },
  {
    path: '/login',
    name: 'login',
    component: LoginView,
    meta: { title: '登录', public: true },
  },
  {
    path: '/register',
    name: 'register',
    component: SupplierRegisterView,
    meta: { title: '供应商注册', public: true },
  },
  {
    path: '/messages',
    component: MessageCenterView,
    meta: { title: '消息待办', permission: 'message:view' },
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
      { path: 'order-changes', component: OrderChangeListView, meta: { title: '订单变更(PCO)', permission: 'order:change' } },
      { path: 'order-sync', component: OrderSyncStatusView, meta: { title: 'ERP订单同步', activeMenu: '/purchasing/integration', permission: 'integration:view' } },
      { path: 'asn', component: AsnListView, meta: { title: '物流与交付', permission: 'asn:view' } },
      { path: 'asn/create', component: AsnCreateView, meta: { title: '创建 ASN', activeMenu: '/purchasing/asn', permission: 'asn:create' } },
      { path: 'asn/scan-receive', component: ScanReceivingView, meta: { title: '扫码收货', activeMenu: '/purchasing/asn', permission: 'asn:view' } },
      { path: 'asn/write-off', component: WriteOffView, meta: { title: '冲销调整', activeMenu: '/purchasing/asn', permission: 'asn:view' } },
      { path: 'asn/:id', component: BusinessDetailView, meta: { title: 'ASN详情', moduleName: 'ASN详情', activeMenu: '/purchasing/asn', permission: 'asn:view' } },
      { path: 'quality', component: QualityListView, meta: { title: '质量检验', permission: 'quality:view' } },
      { path: 'quality-ext', component: PurchasingQualityView, meta: { title: '全面质量管理', permission: 'quality:view' } },
      { path: 'quality-iqc', component: IQCStandardView, meta: { title: 'IQC检验标准', permission: 'quality:view' } },
      { path: 'quality/8d/:id/edit', component: EightDEditView, meta: { title: '8D报告编辑', moduleName: '8D报告编辑', activeMenu: '/purchasing/quality-ext', permission: 'quality:view' } },
      { path: 'quality/:id', component: BusinessDetailView, meta: { title: '质量详情', moduleName: '质量详情', activeMenu: '/purchasing/quality', permission: 'quality:view' } },
      { path: 'settlements', component: SettlementListView, meta: { title: '对账管理', permission: 'settlement:view' } },
      { path: 'finance', component: PurchasingFinanceView, meta: { title: '全面财务管理', permission: 'finance:view' } },
      { path: 'settlements/:id', component: BusinessDetailView, meta: { title: '对账详情', moduleName: '对账详情', activeMenu: '/purchasing/settlements', permission: 'settlement:view' } },
      { path: 'rfq', component: RfqListView, meta: { title: 'RFQ询价', permission: 'rfq:view' } },
      { path: 'rfq/:id', component: RfqDetailView, meta: { title: 'RFQ详情', activeMenu: '/purchasing/rfq', permission: 'rfq:view' } },
      { path: 'quotes', component: QuoteCompareView, meta: { title: '报价对比', permission: 'quote:view' } },
      { path: 'vmi', component: VmiForecastView, meta: { title: 'VMI与需求预测', permission: 'inventory:view' } },
      { path: 'integration', component: IntegrationConfigView, meta: { title: '集成网关', permission: 'integration:view' } },
      { path: 'exchange-rates', component: ExchangeRateView, meta: { title: '汇率配置', permission: 'config:view' } },
      { path: 'settings', component: SystemConfigView, meta: { title: '系统配置', permission: 'config:view' } },
      { path: 'blacklist', component: BlacklistView, meta: { title: '黑名单管理', activeMenu: '/purchasing/suppliers', permission: 'supplier:view' } },
    ],
  },
  {
    path: '/supplier',
    component: SupplierLayout,
    redirect: '/supplier/dashboard',
    meta: { userType: 'supplier' },
    children: [
      { path: 'dashboard', component: SupplierDashboardView, meta: { title: '门户工作台', permission: 'supplier:dashboard:view' } },
      { path: 'orders', component: SupplierOrderCenterView, meta: { title: '订单中心', permission: 'supplier:order:view' } },
      { path: 'deliveries', component: SupplierDeliveryCenterView, meta: { title: '发货中心', permission: 'supplier:delivery:view' } },
      { path: 'deliveries/create', component: SupplierDeliveryCreateView, meta: { title: '创建发货通知', activeMenu: '/supplier/deliveries', permission: 'supplier:delivery:create' } },
      { path: 'quality', component: SupplierQualityCenterView, meta: { title: '质量中心', permission: 'supplier:quality:view' } },
      { path: 'settlements', component: SupplierSettlementCenterView, meta: { title: '财务中心', permission: 'supplier:settlement:view' } },
      { path: 'rfq', component: SupplierRfqQuoteView, meta: { title: 'RFQ报价', permission: 'supplier:rfq:view' } },
      { path: 'profile', component: SupplierProfileView, meta: { title: '资料中心', permission: 'supplier:profile:view' } },
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
  console.debug(`[Router] → ${to.fullPath}`, { public: to.meta.public, permission: to.meta.permission, userType: to.matched.find((item) => item.meta.userType)?.meta.userType })
  if (to.meta.public) {
    console.debug('[Router] ✓ 公开路由，放行')
    return true
  }
  if (!userStore.token) {
    console.debug(`[Router] !! 无 token → 重定向 /login?redirect=${to.fullPath}`)
    return `/login?redirect=${to.fullPath}`
  }
  if (!userStore.tokenExpiresAt) {
    userStore.restoreTokenExpiry()
  }
  if (userStore.tokenExpiresAt && Date.now() > userStore.tokenExpiresAt) {
    console.debug('[Router] !! token 已过期 → 重定向 /login')
    userStore.logout()
    return `/login?redirect=${to.fullPath}`
  }
  if (!userStore.user) {
    console.debug('[Router] … 加载用户信息')
    await userStore.loadCurrentUser()
  }
  const routeUserType = to.matched.find((item) => item.meta.userType)?.meta.userType
  if (routeUserType && userStore.user?.userType !== routeUserType) {
    console.debug(`[Router] !! 用户类型不匹配 (需要=${routeUserType}, 实际=${userStore.user?.userType}) → /403`)
    return '/403'
  }
  // 细粒度权限检查：验证用户是否拥有路由所需的 permission
  const requiredPermission = to.meta.permission as string | undefined
  if (requiredPermission) {
    if (userStore.permissions.length === 0) {
      // 权限数据为空（后端尚未配置或权限码尚未对齐），降级为仅依赖 userType 隔离
      console.warn(`[Router] ⚠ 权限列表为空，路由 ${to.fullPath} 需要 ${requiredPermission} 但无法校验，降级放行（仅 userType 隔离生效）`)
    } else if (!userStore.permissions.includes(requiredPermission)) {
      console.debug(`[Router] !! 缺少权限 ${requiredPermission} → /403`)
      return '/403'
    }
  }
  console.debug(`[Router] ✓ 放行 → ${to.fullPath}`)
  document.title = `${to.meta.title || '供应商协同'} - 供应商协同系统`
  return true
})

export default router
