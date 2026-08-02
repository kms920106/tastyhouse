import HashTagSkeleton from '@/components/ui/HashTagSkeleton'

export default function SearchRecommendedKeywordListSkeleton() {
  return (
    <div className="flex flex-wrap gap-2">
      {Array.from({ length: 10 }).map((_, i) => (
        <HashTagSkeleton key={i} />
      ))}
    </div>
  )
}
