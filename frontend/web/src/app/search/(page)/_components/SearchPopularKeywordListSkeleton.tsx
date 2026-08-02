import SearchPopularKeywordListItemSkeleton from './SearchPopularKeywordListItemSkeleton'

export default function SearchPopularKeywordListSkeleton() {
  return (
    <ul className="flex flex-col gap-5 px-[15px]">
      {Array.from({ length: 10 }).map((_, i) => (
        <li key={i}>
          <SearchPopularKeywordListItemSkeleton />
        </li>
      ))}
    </ul>
  )
}
