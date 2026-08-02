interface Props {
  query: string
  label?: string
}

export default function SearchResultEmptyState({ query, label }: Props) {
  const message = label
    ? `'${query}'에 대한 ${label} 검색 결과가 없습니다.`
    : `'${query}'에 대한 검색 결과가 없습니다.`

  return (
    <div className="min-h-[50vh] flex items-center justify-center px-4">
      <p className="text-sm text-[#aaaaaa] text-center">{message}</p>
    </div>
  )
}
