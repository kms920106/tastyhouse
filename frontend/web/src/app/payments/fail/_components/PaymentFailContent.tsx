'use client'

import AppPrimaryButton from '@/components/ui/AppPrimaryButton'
import { useRouter } from 'next/navigation'

interface Props {
  errorCode?: string
  errorMessage?: string
}

export default function PaymentFailContent({ errorCode, errorMessage }: Props) {
  const router = useRouter()

  return (
    <div className="min-h-screen flex flex-col items-center justify-center bg-white px-[15px]">
      <h1 className="text-xl font-bold mb-4">결제에 실패했습니다</h1>
      {errorMessage && <p className="text-sm text-[#666666] mb-2">{errorMessage}</p>}
      {errorCode && <p className="text-xs text-[#999999] mb-8">에러 코드: {errorCode}</p>}
      <AppPrimaryButton className="w-full max-w-[300px]" onClick={() => router.back()}>
        돌아가기
      </AppPrimaryButton>
    </div>
  )
}
