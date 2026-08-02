import AppPrimaryButton from '@/components/ui/AppPrimaryButton'
import { PAGE_PATHS } from '@/lib/paths'
import Link from 'next/link'

interface Props {
  title: string
  description: string
}

export default function GuestLoginBanner({ title, description }: Props) {
  return (
    <div className="flex items-center justify-between gap-3">
      <div className="flex items-center gap-3">
        <div className="flex flex-col gap-2">
          <p className="text-sm font-bold leading-[14px]">{title}</p>
          <p className="text-xs leading-[12px] text-[#999999]">{description}</p>
        </div>
      </div>
      <AppPrimaryButton asChild className="w-auto h-[38px] px-4 shrink-0 text-sm">
        <Link href={PAGE_PATHS.AUTH_LOGIN}>로그인</Link>
      </AppPrimaryButton>
    </div>
  )
}
