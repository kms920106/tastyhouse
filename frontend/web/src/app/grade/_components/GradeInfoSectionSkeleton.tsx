import { Skeleton } from '@/components/ui/shadcn/skeleton'

function GradeInfoItemSkeleton() {
  return (
    <div className="flex items-center gap-4 p-5 border border-line">
      {/* 등급 아이콘 */}
      <Skeleton className="w-10 h-[42px] shrink-0 rounded-md" />
      <div className="flex flex-col gap-2.5">
        {/* 등급명 */}
        <Skeleton className="w-16 h-4" />
        {/* 리뷰 조건 텍스트 */}
        <Skeleton className="w-40 h-[14px]" />
      </div>
    </div>
  )
}

export default function GradeInfoSectionSkeleton() {
  return (
    <div className="px-[15px] py-[25px]">
      {/* 제목 */}
      <Skeleton className="w-36 h-[17px]" />
      {/* 설명 */}
      <Skeleton className="mt-2.5 w-52 h-3" />

      <div className="flex flex-col gap-[13px] mt-[25px]">
        {Array.from({ length: 5 }).map((_, i) => (
          <GradeInfoItemSkeleton key={i} />
        ))}
      </div>
    </div>
  )
}
