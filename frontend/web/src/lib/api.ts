import { AUTH_COOKIE_KEYS } from '@/lib/auth-config'
import { getEpochMs } from '@/lib/date'
import { env } from '@/lib/env'
import logger from '@/lib/logger'
import { cookies } from 'next/headers'

type RequestConfig<P extends object = object> = RequestInit & {
  params?: P
  isFormData?: boolean
  timeout?: number
}

type PageInfo = {
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export interface ApiResponse<T = unknown> {
  data?: T
  error?: string
  // 백엔드가 내려주는 비즈니스 에러 코드 (e.g. DUPLICATE_RESERVATION). 성공 응답에서는 항상 없음.
  errorCode?: string
  // 백엔드가 내려주는 사용자 노출용 메시지 (에러/성공 무관, 있을 때만)
  message?: string
  status: number
  pagination?: PageInfo
}

class ApiClient {
  private baseURL: string
  private withAuth: boolean

  constructor(baseURL: string = env.NEXT_PUBLIC_API_URL, withAuth: boolean = true) {
    this.baseURL = baseURL
    this.withAuth = withAuth
  }

  private async getRequestHeaders(
    headers?: HeadersInit,
    isFormData?: boolean,
  ): Promise<Record<string, string>> {
    const requestHeaders: Record<string, string> = isFormData
      ? {}
      : { 'Content-Type': 'application/json' }

    if (this.withAuth) {
      const cookieStore = await cookies()
      const accessToken = cookieStore.get(AUTH_COOKIE_KEYS.ACCESS_TOKEN)?.value

      if (accessToken) {
        requestHeaders['Authorization'] = `Bearer ${accessToken}`
      }
    }

    if (headers) {
      Object.assign(requestHeaders, headers)
    }

    return requestHeaders
  }

  private async request<T, P extends object>(
    endpoint: string,
    config: RequestConfig<P> = {},
  ): Promise<ApiResponse<T>> {
    const { params, headers, isFormData, timeout = 3000, ...restConfig } = config
    // const { params, headers, isFormData, timeout = 30000, ...restConfig } = config
    const method = restConfig.method ?? 'GET'
    const correlationId = crypto.randomUUID().slice(0, 8)
    const requestLogger = logger.child({ correlationId, method, path: endpoint })

    let url = `${this.baseURL}${endpoint}`
    if (params) {
      const searchParams = new URLSearchParams()

      Object.entries(params).forEach(([key, value]) => {
        if (value == null) return

        if (Array.isArray(value)) {
          value.forEach((item) => searchParams.append(key, String(item)))
        } else {
          searchParams.append(key, String(value))
        }
      })

      const queryString = searchParams.toString()
      if (queryString) {
        url += `?${queryString}`
      }
    }

    const controller = new AbortController()
    const timeoutId = setTimeout(() => controller.abort(), timeout)
    const startTime = getEpochMs()

    requestLogger.info('[API REQUEST]')

    try {
      const requestHeaders = await this.getRequestHeaders(headers, isFormData)

      const response = await fetch(url, {
        headers: requestHeaders,
        cache: 'no-store',
        signal: controller.signal,
        ...restConfig,
      })

      const status = response.status
      const durationMs = getEpochMs() - startTime
      const json = await response.json().catch(() => null)

      if (!response.ok) {
        const message = json?.message || response.statusText
        const logPayload = { status, durationMs, message }

        if (status >= 500) {
          requestLogger.error(logPayload, '[API RESPONSE]')
        } else {
          requestLogger.warn(logPayload, '[API RESPONSE]')
        }

        // 백엔드 errorCode/message를 보존해 호출부가 코드 기반으로 분기하고
        // 사용자에게 노출 가능한 한국어 메시지를 그대로 쓸 수 있게 한다.
        return {
          error: json?.message || '오류가 발생했습니다. 다시 시도해 주세요.',
          ...(json?.errorCode ? { errorCode: json.errorCode } : {}),
          ...(json?.message ? { message: json.message } : {}),
          status,
        }
      }

      // 백엔드 응답 { success, errorCode, data, message, pagination } 구조를 자동 언래핑
      if (json && typeof json === 'object' && 'success' in json) {
        if (!json.success) {
          requestLogger.warn({ status, durationMs, message: json.message }, '[API RESPONSE]')
          // HTTP는 2xx지만 success:false인 비즈니스 에러 — errorCode/message 보존
          return {
            error: json.message || '오류가 발생했습니다. 다시 시도해 주세요.',
            ...(json.errorCode ? { errorCode: json.errorCode } : {}),
            ...(json.message ? { message: json.message } : {}),
            status,
          }
        }

        requestLogger.debug({ body: json.data }, '[API RESPONSE BODY]')
        requestLogger.info({ status, durationMs }, '[API RESPONSE]')

        return {
          data: json.data as T,
          status,
          ...(json.pagination ? { pagination: json.pagination } : {}),
        }
      }

      requestLogger.info({ status, durationMs }, '[API RESPONSE]')
      requestLogger.debug({ body: json }, '[API RESPONSE BODY]')

      return { data: json as T, status }
    } catch (error) {
      const durationMs = getEpochMs() - startTime

      if (error instanceof DOMException && error.name === 'AbortError') {
        requestLogger.error({ durationMs, timeoutMs: timeout }, '[TIMEOUT]')
        return { error: '요청 시간이 초과되었습니다.', status: 0 }
      }

      requestLogger.error({ err: error, durationMs }, '[ERROR]')
      return { error: error instanceof Error ? error.message : 'Network error', status: 0 }
    } finally {
      clearTimeout(timeoutId)
    }
  }

  async get<T = unknown, P extends object = object>(
    endpoint: string,
    config?: RequestConfig<P>,
  ): Promise<ApiResponse<T>> {
    return this.request<T, P>(endpoint, {
      method: 'GET',
      ...config,
    })
  }

  async post<T = unknown, P extends object = object>(
    endpoint: string,
    body?: unknown,
    config?: RequestConfig<P>,
  ): Promise<ApiResponse<T>> {
    return this.request<T, P>(endpoint, {
      method: 'POST',
      body: body ? JSON.stringify(body) : undefined,
      ...config,
    })
  }

  async put<T = unknown, P extends object = object>(
    endpoint: string,
    body?: unknown,
    config?: RequestConfig<P>,
  ): Promise<ApiResponse<T>> {
    return this.request<T, P>(endpoint, {
      method: 'PUT',
      body: body ? JSON.stringify(body) : undefined,
      ...config,
    })
  }

  async patch<T = unknown, P extends object = object>(
    endpoint: string,
    body?: unknown,
    config?: RequestConfig<P>,
  ): Promise<ApiResponse<T>> {
    return this.request<T, P>(endpoint, {
      method: 'PATCH',
      body: body ? JSON.stringify(body) : undefined,
      ...config,
    })
  }

  async upload<T = unknown, P extends object = object>(
    endpoint: string,
    formData: FormData,
    config?: RequestConfig<P>,
  ): Promise<ApiResponse<T>> {
    return this.request<T, P>(endpoint, {
      ...config,
      method: 'POST',
      body: formData,
      isFormData: true,
    })
  }

  async delete<T = unknown, P extends object = object>(
    endpoint: string,
    body?: unknown,
    config?: RequestConfig<P>,
  ): Promise<ApiResponse<T>> {
    return this.request<T, P>(endpoint, {
      method: 'DELETE',
      body: body ? JSON.stringify(body) : undefined,
      ...config,
    })
  }
}

export const api = new ApiClient()
export const publicApi = new ApiClient(undefined, false)
