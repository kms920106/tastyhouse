import { cn } from '@/lib/utils'

const PAGE_SIZE = 10

export function NoticeListSkeleton() {
  return (
    <div className="flex flex-col">
      {Array.from({ length: PAGE_SIZE }).map((_, i) => (
        <div
          key={i}
          className={cn(
            'px-[16px] py-[18px] animate-pulse border-b border-line last:border-b-0',
            i === 0 && 'pt-0',
          )}
        >
          <div className="h-3.5 bg-gray-200 rounded w-3/4" />
          <div className="mt-3 h-3 bg-gray-200 rounded w-1/4" />
        </div>
      ))}
    </div>
  )
}
