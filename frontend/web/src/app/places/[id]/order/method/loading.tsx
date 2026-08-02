import { Skeleton } from '@/components/ui/shadcn/skeleton'
import OrderMethodHeader from './_components/OrderMethodHeader'

export default function Loading() {
  return (
    <div className="flex flex-col min-h-screen">
      <OrderMethodHeader />
      <div className="flex-1 flex flex-col justify-center px-[15px]">
        <div className="flex flex-col gap-[21px] text-center">
          <h2 className="text-[23px] leading-[23px]">원하시는 주문방법을 선택해 주세요.</h2>
          <p className="text-sm leading-[21px] text-[#999999]">
            가게 사정에 따라 가능한 주문방법이 달라질 수 있으며,
            <br />
            자세한 사항은 가게 정보를 확인해 주세요.
          </p>
        </div>
        <div className="grid grid-cols-2 gap-[15px] mt-[60px]">
          {Array.from({ length: 4 }).map((_, i) => (
            <Skeleton key={i} className="rounded-none" style={{ aspectRatio: '165/100' }} />
          ))}
        </div>
      </div>
      <div className="px-[15px] py-2.5">
        <Skeleton className="w-full h-[52px] rounded-none" />
      </div>
    </div>
  )
}
