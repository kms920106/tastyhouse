'use client'

import SearchInputHeader from '@/components/layouts/SearchInputHeader'
import { PAGE_PATHS } from '@/lib/paths'
import { useRouter } from 'next/navigation'

export default function SearchHeader() {
  const router = useRouter()

  return (
    <SearchInputHeader
      onSearch={(value) =>
        router.push(`${PAGE_PATHS.SEARCH_RESULT}?q=${encodeURIComponent(value)}&tab=all`)
      }
    />
  )
}
