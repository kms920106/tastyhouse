'use client'

import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { useState } from 'react'

export default function QueryProvider({ children }: { children: React.ReactNode }) {
  const [queryClient] = useState(
    () =>
      new QueryClient({
        defaultOptions: {
          queries: {
            staleTime: 60 * 1000, // 데이터를 fresh 상태로 유지하는 시간 (1분)
            gcTime: 5 * 60 * 1000, // 사용하지 않는 캐시 데이터를 메모리에 유지하는 시간 (5분)
            refetchOnWindowFocus: false, // 브라우저 탭에 다시 포커스해도 자동으로 refetch하지 않음
            retry: 1, // 쿼리 실패 시 1번만 재시도
          },
        },
      }),
  )

  return <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
}
