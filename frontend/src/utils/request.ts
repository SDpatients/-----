import axios, { type AxiosError, type InternalAxiosRequestConfig, type AxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, removeToken } from './storage'

export interface ApiResult<T> {
  code: number
  message: string
  data: T
  timestamp?: string
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
  paramsSerializer: { indexes: null },
  transformResponse: [
    (data) => {
      if (typeof data === 'string') {
        try {
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
                if (currentNumber.length >= 16) {
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

            if (/[0-9]/.test(char)) {
              inNumber = true
              currentNumber += char
            } else {
              if (inNumber) {
                if (currentNumber.length >= 16) {
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

          if (inNumber) {
            if (currentNumber.length >= 16) {
              result += '"' + currentNumber + '"'
            } else {
              result += currentNumber
            }
          }

          return JSON.parse(result)
        } catch {
          try {
            return JSON.parse(data)
          } catch {
            return data
          }
        }
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
  let finalName = fileName

  if (disposition) {
    // Try RFC 5987 format: filename*=UTF-8''encoded_name
    const utf8Match = disposition.match(/filename\*=UTF-8''(.+?)(?:;|$)/i)
    if (utf8Match) {
      finalName = decodeURIComponent(utf8Match[1])
    } else {
      // Try standard quoted filename: filename="name"
      const quotedMatch = disposition.match(/filename="([^"]+)"/i)
      if (quotedMatch) {
        finalName = decodeURIComponent(quotedMatch[1])
      } else {
        // Try simple filename: filename=name
        const simpleMatch = disposition.match(/filename=([^;]+)/i)
        if (simpleMatch) {
          finalName = decodeURIComponent(simpleMatch[1].trim())
        }
      }
    }

    // Handle Q-encoding: =?UTF-8?Q?encoded?=
    const qEncodedMatch = finalName.match(/=\?UTF-8\?Q\?(.+?)\?=/i)
    if (qEncodedMatch) {
      finalName = qEncodedMatch[1]
        .replace(/_/g, ' ')
        .replace(/=([0-9A-Fa-f]{2})/g, (_, hex) => String.fromCharCode(parseInt(hex, 16)))
    }
  }

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
  if (config.data && typeof config.data === 'object' && !(config.data instanceof FormData) && !config.headers?.['Content-Type']) {
    config.headers['Content-Type'] = 'application/json'
  }
  return config
})

const redirectLogin = () => {
  removeToken()
  if (window.location.pathname !== '/login') window.location.href = `/login?redirect=${encodeURIComponent(window.location.pathname)}`
}

const handleApiError = (error: ApiError) => {
  if ([401, 40101, 40102].includes(error.code) || error.status === 401) {
    ElMessage.error(error.code === 40102 ? '登录令牌已过期，请重新登录' : '登录已失效，请重新登录')
    redirectLogin()
    return
  }
  if ([403, 40301, 40302].includes(error.code) || error.status === 403) {
    ElMessage.error(error.code === 40302 ? '无权访问该数据' : error.message || '无权限访问')
    return
  }
  if (error.code === 40001) {
    ElMessage.error(error.message || '请求参数错误，请检查输入')
    return
  }
  if (error.code === 40002) {
    ElMessage.error(error.message || '资源不存在，可能已被删除')
    return
  }
  if (error.code === 40900) {
    ElMessage.error(error.message || '业务处理异常')
    return
  }
  if (error.code === 40901) {
    ElMessage.error(error.message || '当前状态不允许此操作')
    return
  }
  if (error.code === 40902) {
    ElMessage.warning(error.message || '请勿重复提交')
    return
  }
  if (error.code === 50001) {
    ElMessage.error('系统异常，请联系管理员')
    return
  }
  if (error.code === 50002) {
    ElMessage.error('外部系统异常，请稍后重试')
    return
  }
  if (error.code === 50003) {
    ElMessage.error('缓存服务未启动，请联系管理员检查Redis服务')
    return
  }
  const isMockEnabled = import.meta.env.VITE_USE_MOCK === 'true'
  const trace = error.traceId ? `，追踪号：${error.traceId}` : ''
  ElMessage.error(`${error.message || '请求失败'}${trace}`)
}

service.interceptors.response.use(
  (response) => {
    const result = response.data as ApiResult<unknown>
    if (result && typeof result.code === 'number') {
      if (result.code === 200) {
        return result.data
      }
      const error = new ApiError(result.message, result.code, result.traceId, response.status)
      handleApiError(error)
      return Promise.reject(error)
    }
    if (typeof response.data === 'string' && response.data.trim().toLowerCase().startsWith('<!doctype html')) {
      const error = new ApiError('接口返回了前端页面 HTML，请检查 Vite 代理或后端服务地址', 502, undefined, response.status)
      handleApiError(error)
      return Promise.reject(error)
    }
    return response.data
  },
  (error: AxiosError<ApiResult<unknown>>) => {
    const data = error.response?.data
    const apiError = new ApiError(data?.message || error.message || '网络异常', data?.code || error.response?.status || 500, data?.traceId, error.response?.status)
    handleApiError(apiError)
    return Promise.reject(apiError)
  },
)

export const request = service
