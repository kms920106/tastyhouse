import { Skeleton } from '@/components/ui/shadcn/skeleton'

export default function GradeCurrentSectionSkeleton() {
  return (
    <div className="px-[15px] pt-10 pb-5 text-center">
      {/* 닉네임 텍스트 줄 */}
      <div className="flex justify-center">
        <Skeleton className="w-48 h-4" />
      </div>

      {/* 등급 아이콘 */}
      <div className="flex justify-center mt-[23px]">
        <Skeleton className="w-[73px] h-[75px] rounded-md" />
      </div>

      {/* 등급명 */}
      <div className="flex justify-center mt-5">
        <Skeleton className="w-20 h-[23px]" />
      </div>

      {/* 다음 등급 달성 조건 박스 */}
      <div className="mt-[30px] py-5 bg-[#fcfcfc] border border-line box-border flex flex-col items-center gap-2.5">
        <Skeleton className="w-56 h-[14px]" />
        <Skeleton className="w-36 h-[12px]" />
      </div>
    </div>
  )
}
