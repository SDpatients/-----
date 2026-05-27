import { commonApi } from '@/api/common'

/** 获取一次性幂等令牌，返回 headers 对象可直接合并到 axios config */
export const getIdempotentHeaders = async (): Promise<Record<string, string>> => {
  try {
    const token = await commonApi.getIdempotentToken()
    if (token) return { 'X-Idempotent-Token': token }
  } catch {
    // 获取失败时降级：不阻止提交，但后端 @Idempotent 注解会拦截
    console.warn('获取幂等令牌失败，提交可能被拒绝')
  }
  return {}
}