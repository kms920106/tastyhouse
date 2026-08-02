export async function register() {
  if (process.env.NEXT_RUNTIME === 'nodejs') {
    const { default: logger } = await import('@/lib/logger')
    logger.info('Logger initialized')
  }
}

// Next.js 15 공식 서버 요청 오류 훅
// SSR, Server Actions, Route Handlers에서 발생한 오류를 중앙에서 캐치합니다.
// 참고: https://nextjs.org/docs/app/api-reference/file-conventions/instrumentation#onrequesterror
export async function onRequestError(
  error: unknown,
  request: Readonly<{ path: string; method: string; headers: Record<string, string | string[] | undefined> }>,
  context: Readonly<{ routerKind: string; routePath: string; routeType: string }>,
) {
  if (process.env.NEXT_RUNTIME === 'nodejs') {
    const { default: logger } = await import('@/lib/logger')

    logger.error(
      {
        type: 'server_request_error',
        method: request.method,
        path: request.path,
        routeType: context.routeType,
        digest: (error as Error & { digest?: string }).digest,
      },
      '[SERVER_ERROR]',
    )
  }
}
