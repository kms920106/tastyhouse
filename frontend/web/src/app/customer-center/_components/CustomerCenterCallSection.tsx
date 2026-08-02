import AppButton from '@/components/ui/AppButton'
import Link from 'next/link'

export default function CustomerCenterCallSection() {
  return (
    <div className="px-[15px] py-10 pb-[30px]">
      <p className="text-[26px] leading-[26px] text-center">1234-5678</p>
      <div className="pt-5 pb-[30px] flex flex-col gap-[7px] items-center">
        <p className="text-sm leading-[14px] text-[#666666]">
          평일 09:00 ~ 18:00 (점심시간 13:00 ~ 14:00)
        </p>
        <p className="text-sm leading-[14px] text-[#666666]">(토/일/공휴일 휴무)</p>
      </div>
      <AppButton
        asChild
        className="w-full h-[50px] bg-[#333333] text-base leading-[16px] text-white"
      >
        <Link href="tel:1234-5678">전화 문의</Link>
      </AppButton>
    </div>
  )
}
