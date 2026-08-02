'use client'

import AppPrimaryButton from '@/components/ui/AppPrimaryButton'
import { useRouter } from 'next/navigation'

export default function AuthSignupCompleteFooter() {
  const router = useRouter()

  return (
    <div className="px-[15px] py-2.5">
      <AppPrimaryButton onClick={() => router.replace('/')}>확인</AppPrimaryButton>
    </div>
  )
}
