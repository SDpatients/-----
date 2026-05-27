import axios, { type AxiosError, type InternalAxiosRequestConfig, type AxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, removeToken } from './storage'

export interface ApiResult<T> {
  code: number
  message: string
  data: T
  timestamp?: number
  traceId?: string
}

export interface ApiPage<T> {
  records: T[]
  total: number
  pageSize: number
  pageNum: number
  pages: number
}

export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api'

export class ApiError extends Error {
  code: number
  traceId?: string
  status?: number

  constructor(message: string, code: number, traceId?: string, status?: number) {
    super(message)
    this.code = code
    this.traceId = traceId
    this.status = status
  }
}

const service = axios.create({
  baseURL: API_BASE_URL,
  timeout: 20000,
  transformResponse: [
    (data) => {
      // 解决大整数精度丢失问题：将 JSON 中的大整数转换为字符串
      if (typeof data === 'string') {
        // 使用一个更健壮的方法：逐字符解析方法
        // 避免使用简单的正则表达式
        let inString = false
        let escaped = false
        let result = ''
        let currentNumber = ''
        let inNumber = false
        
        for (let i = 0; i < data.length; i++) {
          const char = data[i]
          
          if (escaped) {
            result += char
            escaped = false
            continue
          }
          
          if (char === '\\') {
            result += char
            escaped = true
            continue
          }
          
          if (char === '"') {
            if (inNumber) {
              // 结束数字
              if (currentNumber.length >= 16) {
                // 是大整数，用引号包裹
                result += '"' + currentNumber + '"'
              } else {
                result += currentNumber
              }
              inNumber = false
              currentNumber = ''
            }
            inString = !inString
            result += char
            continue
          }
          
          if (inString) {
            result += char
            continue
          }
          
          // 不在字符串中，检查是否是数字
          if (/[0-9]/.test(char)) {
            inNumber = true
            currentNumber += char
          } else {
            if (inNumber) {
              // 结束数字
              if (currentNumber.length >= 16) {
                // 是大整数，用引号包裹
                result += '"' + currentNumber + '"'
              } else {
                result += currentNumber
              }
              inNumber = false
              currentNumber = ''
            }
            result += char
          }
        }
        
        // 处理最后可能剩余的数字
        if (inNumber) {
          if (currentNumber.length >= 16) {
            result += '"' + currentNumber + '"'
          } else {
            result += currentNumber
          }
        }
        
        return JSON.parse(result)
      }
      return data
    },
  ],
})

export const rawRequest = axios.create({
  baseURL: API_BASE_URL,
  timeout: 60000,
  responseType: 'blob',
})

rawRequest.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = getToken()
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

export const downloadBlob = async (config: AxiosRequestConfig, fileName = 'download') => {
  const response = await rawRequest.request<Blob>(config)
  const disposition = response.headers['content-disposition'] as string | undefined
  const matched = disposition?.match(/filename\*?=(?:UTF-8'')?"?([^";]+)"?/i)
  const finalName = matched ? decodeURIComponent(matched[1]) : fileName
  const url = window.URL.createObjectURL(response.data)
  const link = document.createElement('a')
  link.href = url
  link.download = finalName
  link.click()
  window.URL.revokeObjectURL(url)
}

service.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = getToken()
  if (token) config.headers.Authorization = `Bearer ${token}`
  console.debug(`[API] → ${config.method?.toUpperCase()} ${config.url}`, config.params || config.data)
  return config
})

const redirectLogin = () => {
  removeToken()
  if (window.location.pathname !== '/login') window.location.href = `/login?redirect=${encodeURIComponent(window.location.pathname)}`
}

const handleApiError = (error: ApiError) => {
  console.debug(`[API] ✗ 错误处理 code=${error.code} status=${error.status} message="${error.message}"`)
  if ([401, 40101, 40102].includes(error.code) || error.status === 401) {
    console.debug('[API] !! 401 未授权 → 跳转登录页')
    ElMessage.error('登录已失效，请重新登录')
    redirectLogin()
    return
  }
  if ([403, 40301, 40302].includes(error.code) || error.status === 403) {
    console.debug('[API] !! 403 无权限')
    ElMessage.error(error.message || '无权限访问')
    return
  }
  const isMockEnabled = import.meta.env.VITE_USE_MOCK === 'true'
  if (isMockEnabled && error.code === 502) {
    console.debug('[API] !! 502 HTML 响应（Mock 未拦截或后端不可达）')
  }
  const trace = error.traceId ? `，追踪号：${error.traceId}` : ''
  ElMessage.error(`${error.message || '请求失败'}${trace}`)
}

service.interceptors.response.use(
  (response) => {
    const result = response.data as ApiResult<unknown>
    if (result && typeof result.code === 'number') {
      if (result.code === 200) {
        console.debug(`[API] ← ${response.config.method?.toUpperCase()} ${response.config.url} ✓ 200`)
        return result.data
      }
      console.debug(`[API] ← ${response.config.method?.toUpperCase()} ${response.config.url} ✗ code=${result.code} "${result.message}"`)
      const error = new ApiError(result.message, result.code, result.traceId, response.status)
      handleApiError(error)
      return Promise.reject(error)
    }
    if (typeof response.data === 'string' && response.data.trim().toLowerCase().startsWith('<!doctype html')) {
      console.debug(`[API] ← ${response.config.method?.toUpperCase()} ${response.config.url} ✗ HTML（后端不可达或代理错误）`)
      const error = new ApiError('接口返回了前端页面 HTML，请检查 Vite 代理或后端服务地址', 502, undefined, response.status)
      handleApiError(error)
      return Promise.reject(error)
    }
    return response.data
  },
  (error: AxiosError<ApiResult<unknown>>) => {
    const data = error.response?.data
    const apiError = new ApiError(data?.message || error.message || '网络异常', data?.code || error.response?.status || 500, data?.traceId, error.response?.status)
    console.debug(`[API] ← ${error.config?.method?.toUpperCase()} ${error.config?.url} ✗ 网络/HTTP异常 status=${error.response?.status}`, error.message)
    handleApiError(apiError)
    return Promise.reject(apiError)
  },
)

export const request = service
