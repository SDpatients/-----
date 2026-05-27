import type { Directive, DirectiveBinding } from 'vue'
import { useUserStore } from '@/stores/user'

/**
 * v-permission 指令 —— 按钮级权限控制
 *
 * 用法：
 *   <el-button v-permission="'order:create'">创建订单</el-button>
 *   <el-button v-permission="['order:create', 'order:edit']">任一权限可见</el-button>
 *   <el-button v-permission:all="['order:create', 'order:edit']">全部权限可见</el-button>
 *
 * 无权限时元素被移除（display: none）。
 */
const permissionDirective: Directive<HTMLElement> = {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    check(el, binding)
  },
  updated(el: HTMLElement, binding: DirectiveBinding) {
    check(el, binding)
  },
}

function check(el: HTMLElement, binding: DirectiveBinding) {
  const userStore = useUserStore()
  const required = binding.value

  // 未传入权限码 → 直接显示
  if (!required) return

  const userPermissions = userStore.permissions || []

  // 权限列表为空 → 降级显示（与路由逻辑一致）
  if (userPermissions.length === 0) return

  const requiredList = Array.isArray(required) ? required : [required]

  let hasPermission: boolean
  if (binding.arg === 'all') {
    // 需要全部满足
    hasPermission = requiredList.every((p) => userPermissions.includes(p))
  } else {
    // 任一满足即可
    hasPermission = requiredList.some((p) => userPermissions.includes(p))
  }

  if (!hasPermission) {
    // 隐藏元素（保留 DOM 占位避免布局抖动也可以用 visibility: hidden）
    el.style.display = 'none'
  } else {
    el.style.display = ''
  }
}

export default permissionDirective