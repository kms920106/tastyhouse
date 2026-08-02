const PAGE_SIZE = 10

export function EventListSkeleton() {
  return (
    <div className="flex flex-col gap-2.5">
      {Array.from({ length: PAGE_SIZE }).map((_, i) => (
        <div key={i} className="animate-pulse">
          <div className="w-full aspect-[2/1] bg-gray-200" />
          <div className="px-[15px] py-5">
            <div className="h-4 bg-gray-200 rounded w-3/4" />
            <div className="mt-3 h-3 bg-gray-200 rounded w-1/2" />
          </div>
        </div>
      ))}
    </div>
  )
}
