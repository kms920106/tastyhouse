import ReviewThumbnailSkeleton from '@/components/reviews/ReviewThumbnailSkeleton'

export default function SearchResultReviewListSkeleton({ count = 9 }: { count?: number }) {
  return (
    <div className="grid grid-cols-3 gap-2">
      {Array.from({ length: count }).map((_, i) => (
        <ReviewThumbnailSkeleton key={i} />
      ))}
    </div>
  )
}
