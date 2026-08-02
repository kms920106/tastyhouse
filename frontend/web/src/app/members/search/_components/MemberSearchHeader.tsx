'use client'

import SearchInputHeader from '@/components/layouts/SearchInputHeader'
import { useRouter, useSearchParams } from 'next/navigation'

export default function MemberSearchHeader() {
  const router = useRouter()
  const searchParams = useSearchParams()

  const handleSearch = (value: string) => {
    const params = new URLSearchParams(searchParams.toString())
    params.set('q', value)
    router.replace(`?${params.toString()}`)
  }

  const handleClear = () => {
    const params = new URLSearchParams(searchParams.toString())
    params.delete('q')
    router.replace(`?${params.toString()}`)
  }

  return (
    <SearchInputHeader
      initialValue={searchParams.get('q') ?? ''}
      placeholder="닉네임을 입력해 주세요."
      onSearch={handleSearch}
      onClear={handleClear}
    />
  )
}
