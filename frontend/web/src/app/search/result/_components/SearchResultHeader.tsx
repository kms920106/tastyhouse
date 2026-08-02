'use client'

import SearchInputHeader from '@/components/layouts/SearchInputHeader'
import { PAGE_PATHS } from '@/lib/paths'
import { useRouter } from 'next/navigation'

interface Props {
  query: string
}

export default function SearchResultHeader({ query }: Props) {
  const router = useRouter()

  return (
    <SearchInputHeader
      initialValue={query}
      showBorder={false}
      onSearch={(value) =>
        router.replace(`${PAGE_PATHS.SEARCH_RESULT}?q=${encodeURIComponent(value)}&tab=all`)
      }
    />
  )
}
