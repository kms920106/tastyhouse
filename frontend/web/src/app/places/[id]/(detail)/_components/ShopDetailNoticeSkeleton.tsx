import { Skeleton } from '@/components/ui/shadcn/skeleton'

export function ShopDetailNoticeSkeleton() {
  return (
    <div className="relative mt-[13px] px-[15px] py-[23px] pb-4 bg-[#f9f9f9] border border-[#cccccc] box-border rounded-[5px]">
      <div className="absolute -top-3 left-[10px] inline-block px-3.5 py-[6.5px] mb-3 bg-main text-xs leading-[12px] text-white rounded-full">
        사장님 공지
      </div>
      <Skeleton className="h-4 w-full" />
      <Skeleton className="h-4 w-2/3 mt-2" />
    </div>
  )
}
