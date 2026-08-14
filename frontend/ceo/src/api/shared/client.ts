import { cookies, headers } from "next/headers";
import { redirect } from "next/navigation";

import { AUTH_COOKIE_KEYS, LOGIN_PATH } from "@/lib/auth-config";
import logger from "@/lib/logger";
import { getEpochMs } from "@/lib/utils";

import type { ApiResponse } from "./types";

type RequestConfig<P extends object = object> = RequestInit & {
  params?: P;
  isFormData?: boolean;
  timeout?: number;
};

/**
 * 원본 브라우저 요청의 `user-agent` 와 클라이언트 IP 를 backend 로 전달할 헤더를 만든다.
 *
 * Server Action·Server Component 에서 backend 를 호출하면 그 fetch 는 **Node 런타임이 새로 만드는
 * 요청**이라 원본 요청의 헤더를 하나도 물려받지 않는다. 그대로 두면 backend 가 보는 `User-Agent` 는
 * Node 기본값(`node`)이고 IP 는 Next 서버의 주소가 되어, 개인정보처리시스템 접속기록이 접속자가 아니라
 * **웹서버 자신**을 기록한다(실제로 접속 기기 정보가 전부 `node` 로 남던 결함).
 *
 * `x-forwarded-for` 는 프록시 관례대로 기존 값 뒤에 이번 홉(Next 서버가 본 클라이언트 IP)을 잇는다 —
 * backend `ClientIpResolver` 가 첫 값을 원 클라이언트로 채택하므로 앞의 값을 덮어쓰지 않는 것이 중요하다.
 *
 * 요청 컨텍스트 밖(빌드 타임 프리렌더 등)에서 호출되면 `headers()` 가 throw 하므로 빈 객체를 돌려준다.
 */
async function getForwardedClientHeaders(): Promise<Record<string, string>> {
  try {
    const headerStore = await headers();
    const forwarded: Record<string, string> = {};

    const userAgent = headerStore.get("user-agent");
    if (userAgent) {
      forwarded["User-Agent"] = userAgent;
    }

    // 원본 XFF 가 있으면 그 체인을 보존하고, 없으면 Next 서버가 인식한 클라이언트 IP 로 시작한다.
    const existingForwardedFor = headerStore.get("x-forwarded-for");
    const clientIp = existingForwardedFor ?? headerStore.get("x-real-ip");
    if (clientIp) {
      forwarded["X-Forwarded-For"] = clientIp;
    }

    return forwarded;
  } catch {
    return {};
  }
}

export class ApiError extends Error {
  readonly status: number;
  readonly code?: string;

  constructor(message: string, status: number, code?: string) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.code = code;
  }
}

class ApiClient {
  private baseURL: string;
  private withAuth: boolean;

  constructor(baseURL: string = process.env.NEXT_PUBLIC_API_URL ?? "", withAuth = true) {
    this.baseURL = baseURL;
    this.withAuth = withAuth;
  }

  private async getRequestHeaders(headers?: HeadersInit, isFormData?: boolean): Promise<Record<string, string>> {
    const requestHeaders: Record<string, string> = isFormData ? {} : { "Content-Type": "application/json" };

    if (this.withAuth) {
      const cookieStore = await cookies();
      const accessToken = cookieStore.get(AUTH_COOKIE_KEYS.ACCESS_TOKEN)?.value;

      if (accessToken) {
        requestHeaders.Authorization = `Bearer ${accessToken}`;
      }
    }

    Object.assign(requestHeaders, await getForwardedClientHeaders());

    if (headers) {
      Object.assign(requestHeaders, headers);
    }

    return requestHeaders;
  }

  private async request<T, P extends object>(endpoint: string, config: RequestConfig<P> = {}): Promise<ApiResponse<T>> {
    const result = await this.executeRequest<T, P>(endpoint, config);

    // 인증 클라이언트의 401(인증 실패/만료)은 로그인 페이지로 리다이렉트한다.
    // redirect()는 NEXT_REDIRECT 예외를 throw하므로, executeRequest 내부의
    // try/catch 가 이를 삼키지 않도록 반드시 try/catch 바깥에서 호출한다.
    if (this.withAuth && result.status === 401) {
      redirect(LOGIN_PATH);
    }

    return result;
  }

  private async executeRequest<T, P extends object>(
    endpoint: string,
    config: RequestConfig<P> = {},
  ): Promise<ApiResponse<T>> {
    const { params, headers, isFormData, timeout = 30000, ...restConfig } = config;
    const method = restConfig.method ?? "GET";
    const correlationId = crypto.randomUUID().slice(0, 8);
    const requestLogger = logger.child({
      correlationId,
      method,
      path: endpoint,
    });

    let url = `${this.baseURL}${endpoint}`;
    if (params) {
      const searchParams = new URLSearchParams();

      for (const [key, value] of Object.entries(params)) {
        if (value == null) continue;

        if (Array.isArray(value)) {
          for (const item of value) searchParams.append(key, String(item));
        } else {
          searchParams.append(key, String(value));
        }
      }

      const queryString = searchParams.toString();
      if (queryString) {
        url += `?${queryString}`;
      }
    }

    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), timeout);
    const startTime = getEpochMs();

    requestLogger.info("[API REQUEST]");

    try {
      const requestHeaders = await this.getRequestHeaders(headers, isFormData);

      const response = await fetch(url, {
        headers: requestHeaders,
        cache: "no-store",
        signal: controller.signal,
        ...restConfig,
      });

      const status = response.status;
      const durationMs = getEpochMs() - startTime;
      const json = await response.json().catch(() => null);

      if (!response.ok) {
        // 성공/비즈니스 에러(ApiResponse)는 message, RFC7807 ProblemDetail(4xx/5xx)은 detail 에
        // 문구가 담기므로 두 필드를 모두 확인한다. (예: 409 COUPON_ALREADY_ISSUED)
        const errorMessage = json?.message || json?.detail;
        const logPayload = { status, durationMs, message: errorMessage || response.statusText };

        if (status >= 500) {
          requestLogger.error(logPayload, "[API RESPONSE]");
        } else {
          requestLogger.warn(logPayload, "[API RESPONSE]");
        }

        // 백엔드 errorCode/message 를 보존해 호출부가 코드 기반으로 분기하고
        // 사용자에게 노출 가능한 한국어 메시지를 그대로 쓸 수 있게 한다.
        return {
          error: errorMessage || "오류가 발생했습니다. 다시 시도해 주세요.",
          ...(json?.errorCode ? { errorCode: json.errorCode } : {}),
          ...(errorMessage ? { message: errorMessage } : {}),
          status,
        };
      }

      // 백엔드 응답 { success, errorCode, data, message, pagination } 구조를 자동 언래핑
      if (json && typeof json === "object" && "success" in json) {
        if (!json.success) {
          requestLogger.warn({ status, durationMs, message: json.message }, "[API RESPONSE]");
          // HTTP 는 2xx 지만 success:false 인 비즈니스 에러 — errorCode/message 보존
          return {
            error: json.message || "오류가 발생했습니다. 다시 시도해 주세요.",
            ...(json.errorCode ? { errorCode: json.errorCode } : {}),
            ...(json.message ? { message: json.message } : {}),
            status,
          };
        }

        requestLogger.debug({ body: json.data }, "[API RESPONSE BODY]");
        requestLogger.info({ status, durationMs }, "[API RESPONSE]");

        return {
          data: json.data as T,
          status,
          ...(json.pagination ? { pagination: json.pagination } : {}),
        };
      }

      requestLogger.info({ status, durationMs }, "[API RESPONSE]");
      requestLogger.debug({ body: json }, "[API RESPONSE BODY]");

      return { data: json as T, status };
    } catch (error) {
      const durationMs = getEpochMs() - startTime;

      if (error instanceof DOMException && error.name === "AbortError") {
        requestLogger.error({ durationMs, timeoutMs: timeout }, "[TIMEOUT]");
        return { error: "요청 시간이 초과되었습니다.", status: 0 };
      }

      requestLogger.error({ err: error, durationMs }, "[ERROR]");
      return {
        error: error instanceof Error ? error.message : "Network error",
        status: 0,
      };
    } finally {
      clearTimeout(timeoutId);
    }
  }

  async get<T = unknown, P extends object = object>(
    endpoint: string,
    config?: RequestConfig<P>,
  ): Promise<ApiResponse<T>> {
    return this.request<T, P>(endpoint, { method: "GET", ...config });
  }

  async post<T = unknown, P extends object = object>(
    endpoint: string,
    body?: unknown,
    config?: RequestConfig<P>,
  ): Promise<ApiResponse<T>> {
    return this.request<T, P>(endpoint, {
      method: "POST",
      body: body ? JSON.stringify(body) : undefined,
      ...config,
    });
  }

  async put<T = unknown, P extends object = object>(
    endpoint: string,
    body?: unknown,
    config?: RequestConfig<P>,
  ): Promise<ApiResponse<T>> {
    return this.request<T, P>(endpoint, {
      method: "PUT",
      body: body ? JSON.stringify(body) : undefined,
      ...config,
    });
  }

  async patch<T = unknown, P extends object = object>(
    endpoint: string,
    body?: unknown,
    config?: RequestConfig<P>,
  ): Promise<ApiResponse<T>> {
    return this.request<T, P>(endpoint, {
      method: "PATCH",
      body: body ? JSON.stringify(body) : undefined,
      ...config,
    });
  }

  async upload<T = unknown, P extends object = object>(
    endpoint: string,
    formData: FormData,
    config?: RequestConfig<P>,
  ): Promise<ApiResponse<T>> {
    return this.request<T, P>(endpoint, {
      method: "POST",
      ...config,
      body: formData,
      isFormData: true,
    });
  }

  // PUT multipart/form-data — 콘텐츠보드 수정처럼 파일 첨부와 함께 갱신하는 요청에 사용한다.
  async uploadPut<T = unknown, P extends object = object>(
    endpoint: string,
    formData: FormData,
    config?: RequestConfig<P>,
  ): Promise<ApiResponse<T>> {
    return this.request<T, P>(endpoint, {
      method: "PUT",
      ...config,
      body: formData,
      isFormData: true,
    });
  }

  async delete<T = unknown, P extends object = object>(
    endpoint: string,
    body?: unknown,
    config?: RequestConfig<P>,
  ): Promise<ApiResponse<T>> {
    return this.request<T, P>(endpoint, {
      method: "DELETE",
      body: body ? JSON.stringify(body) : undefined,
      ...config,
    });
  }
}

export const api = new ApiClient();
export const publicApi = new ApiClient(undefined, false);
