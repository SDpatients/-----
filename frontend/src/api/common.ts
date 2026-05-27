import { request } from '@/utils/request'

export const commonApi = {
  /** 获取一次性幂等令牌，用于防重复提交 */
  getIdempotentToken: () => request.get<string, string>('/common/idempotent-token'),
}