// Next.js 15 공식 파일 컨벤션
// 클라이언트 번들의 가장 앞단 (React hydration 이전)에서 실행됩니다.
// 최상위 코드가 바로 실행되며, register() 함수 export는 서버 측 instrumentation.ts 전용입니다.
// 참고: https://nextjs.org/docs/app/api-reference/file-conventions/instrumentation-client

import browserLogger from '@/lib/logger-browser'

// 처리되지 않은 JS 런타임 오류 캐치
window.addEventListener('error', (event) => {
  browserLogger.error(
    { type: 'uncaught_error' },
    '[CLIENT ERROR] %s',
    event.message,
  )
})

// 처리되지 않은 Promise rejection 캐치
window.addEventListener('unhandledrejection', (event) => {
  browserLogger.error(
    { type: 'unhandled_rejection' },
    '[CLIENT ERROR] %s',
    String(event.reason),
  )
})

// React 렌더링 중 발생하는 에러 캐치
// React는 렌더링 에러를 console.error로 출력하므로 이를 가로채서 로깅합니다.
const originalConsoleError = console.error
console.error = (...args: unknown[]) => {
  originalConsoleError.apply(console, args)

  const message = args
    .map((arg) => (arg instanceof Error ? arg.message : String(arg)))
    .join(' ')

  // pino write에서 출력한 포맷된 메시지의 재귀 호출 방지
  if (message.startsWith('[') && message.includes('] ERROR:')) return

  browserLogger.error(
    { type: 'console_error' },
    '[CLIENT ERROR] %s',
    message,
  )
}
