import EmptyState from '@/app/(with-footer)/(without-sidebar)/mypage/_components/EmptyState'
import BookmarkListItem from '@/components/shops/BookmarkListItem'
import ViewMoreButton from '@/components/ui/ViewMoreButton'
import { PAGE_PATHS } from '@/lib/paths'

export interface BookmarkedShop {
  shopId: number
  bookmarkId: number
  shopName: string
  stationName: string
  rating: number
  imageUrl: string
  bookmarked: boolean
}

interface Props {
  bookmarks: BookmarkedShop[]
  hasMoreBookmarks: boolean
}

export default function BookmarkList({ bookmarks, hasMoreBookmarks }: Props) {
  if (bookmarks.length === 0) {
    return (
      <>
        <EmptyState message="저장된 즐겨찾기가 없습니다." />
        <div className="h-[70px]" />
      </>
    )
  }

  return (
    <>
      <div className="flex flex-col gap-2.5 px-[15px] py-[20px]">
        {bookmarks.map((bookmark) => (
          <BookmarkListItem
            key={bookmark.shopId}
            shopId={bookmark.shopId}
            shopImage={bookmark.imageUrl}
            region={bookmark.stationName}
            shopName={bookmark.shopName}
            rating={bookmark.rating}
            isBookmarked={bookmark.bookmarked}
          />
        ))}
      </div>
      {hasMoreBookmarks && (
        <div className="flex justify-center py-5">
          <ViewMoreButton href={PAGE_PATHS.MY_BOOKMARKS} label="더 보러가기" />
        </div>
      )}
      <div className="h-[70px]" />
    </>
  )
}
