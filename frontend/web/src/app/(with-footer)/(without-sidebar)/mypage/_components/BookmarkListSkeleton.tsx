import { BookmarkListItemSkeleton } from '@/components/shops/BookmarkListItemSkeleton'

export function BookmarkListSkeleton() {
  return (
    <div className="flex flex-col gap-2.5 px-[15px] py-[20px]">
      {Array.from({ length: 3 }).map((_, index) => (
        <BookmarkListItemSkeleton key={index} />
      ))}
    </div>
  )
}
