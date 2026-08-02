import { PAGE_PATHS } from '@/lib/paths'
import Link from 'next/link'

export default function LoginNavLinks() {
  return (
    <div className="flex items-center justify-center gap-4 mt-5">
      <Link href={PAGE_PATHS.AUTH_SIGNUP} className="text-sm leading-[14px] text-[#666666]">
        회원가입
      </Link>
      <span className="text-sm leading-[14px] text-[#666666]">|</span>
      <Link href={PAGE_PATHS.AUTH_FORGOT_PASSWORD} className="text-sm leading-[14px] text-[#666666]">
        비밀번호 찾기
      </Link>
    </div>
  )
}
