import { request, type ApiPage } from '@/utils/request'
import { asPage } from '@/utils/apiNormalize'

/** 消息通道配置 */
export interface MessageChannelConfig {
  key: string
  name: string
  enabled: boolean
  remark: string
}

/** 消息模板 */
export interface MessageTemplate {
  id?: number
  templateCode: string
  templateName: string
  channel: number
  titleTemplate: string
  contentTemplate: string
  variables: string
  businessType: string
  status: number
  remark: string
}

/** 通道枚举 */
export const CHANNEL_OPTIONS = [
  { value: 1, label: '站内信' },
  { value: 2, label: '邮件' },
  { value: 3, label: '短信' },
  { value: 4, label: '企业微信' },
]

export const configApi = {
  /** 获取消息通道配置 */
  getChannelConfigs: async (): Promise<MessageChannelConfig[]> => {
    const CHANNEL_KEYS = ['message.channel.web.enabled', 'message.channel.mail.enabled', 'message.channel.sms.enabled', 'message.channel.wecom.enabled']
    const CHANNEL_NAMES: Record<string, string> = {
      'message.channel.web.enabled': '站内信',
      'message.channel.mail.enabled': '邮件',
      'message.channel.sms.enabled': '短信',
      'message.channel.wecom.enabled': '企业微信',
    }
    const results: MessageChannelConfig[] = []
    for (const key of CHANNEL_KEYS) {
      try {
        const value = await request.get<string, string>(`/v1/sys-config/${key}`)
        results.push({ key, name: CHANNEL_NAMES[key] || key, enabled: value === 'true', remark: '' })
      } catch { results.push({ key, name: CHANNEL_NAMES[key] || key, enabled: false, remark: '' }) }
    }
    return results
  },

  /** 更新通道配置 */
  updateChannelConfig: (key: string, enabled: boolean) =>
    request.post<void, void>('/v1/sys-config', { configKey: key, configValue: String(enabled), configName: key, remark: '' }),

  /** 分页查询消息模板 */
  templatePage: (params: { pageNum: number; pageSize: number; keyword?: string; channel?: number }) =>
    request.get<ApiPage<MessageTemplate>, ApiPage<MessageTemplate>>('/v1/message-templates', { params }).then((res) => asPage<MessageTemplate>(res, params.pageNum, params.pageSize)),

  /** 获取模板详情 */
  templateDetail: (id: number | string) => request.get<MessageTemplate, MessageTemplate>(`/v1/message-templates/${id}`),

  /** 新增模板 */
  templateCreate: (data: MessageTemplate) => request.post<number, number>('/v1/message-templates', data),

  /** 更新模板 */
  templateUpdate: (id: number | string, data: MessageTemplate) => request.put<void, void>(`/v1/message-templates/${id}`, data),

  /** 删除模板 */
  templateDelete: (id: number | string) => request.delete<void, void>(`/v1/message-templates/${id}`),

  /** 启用/停用 */
  templateToggle: (id: number | string) => request.post<void, void>(`/v1/message-templates/${id}/toggle`),
}

// ==================== 第三方采购订单接口配置 ====================

/** 第三方采购订单接口配置 */
export interface ThirdPartyPoApiConfig {
  id?: number
  configName: string
  apiType: string
  baseUrl: string
  httpMethod: string
  authType: string
  authCredentials?: string
  requestHeaders?: string
  requestBodyTemplate?: string
  timeoutSeconds: number
  retryCount: number
  enabled: number
  remark?: string
}

/** 接口类型选项 */
export const API_TYPE_OPTIONS = [
  { value: 'CREATE', label: '新增采购订单' },
  { value: 'GET_LATEST', label: '获取最新采购订单' },
]

/** 鉴权方式选项 */
export const AUTH_TYPE_OPTIONS = [
  { value: 'NONE', label: '无鉴权' },
  { value: 'BEARER', label: 'Bearer Token' },
  { value: 'BASIC', label: 'Basic Auth' },
  { value: 'API_KEY', label: 'API Key' },
]

/** HTTP方法选项 */
export const HTTP_METHOD_OPTIONS = [
  { value: 'GET', label: 'GET' },
  { value: 'POST', label: 'POST' },
  { value: 'PUT', label: 'PUT' },
]

export const thirdPartyPoApi = {
  /** 查询配置列表 */
  list: (apiType?: string) =>
    request.get<ThirdPartyPoApiConfig[], ThirdPartyPoApiConfig[]>('/v1/third-party-po-config', { params: { apiType } }),

  /** 查询配置详情 */
  detail: (id: number | string) => request.get<ThirdPartyPoApiConfig, ThirdPartyPoApiConfig>(`/v1/third-party-po-config/${id}`),

  /** 新增配置 */
  create: (data: ThirdPartyPoApiConfig) => request.post<number, number>('/v1/third-party-po-config', data),

  /** 更新配置 */
  update: (id: number | string, data: ThirdPartyPoApiConfig) => request.put<void, void>(`/v1/third-party-po-config/${id}`, data),

  /** 删除配置 */
  delete: (id: number | string) => request.delete<void, void>(`/v1/third-party-po-config/${id}`),

  /** 启用 */
  enable: (id: number | string) => request.post<void, void>(`/v1/third-party-po-config/${id}/enable`),

  /** 禁用 */
  disable: (id: number | string) => request.post<void, void>(`/v1/third-party-po-config/${id}/disable`),

  /** 推送订单到第三方 */
  pushToThirdParty: (configId: number, orderId: number) =>
    request.post<string, string>(`/v1/third-party-po/${configId}/push/${orderId}`),

  /** 从第三方获取最新订单 */
  fetchLatest: (configId: number, params?: Record<string, any>) =>
    request.post<string, string>(`/v1/third-party-po/${configId}/fetch-latest`, params || {}),
}