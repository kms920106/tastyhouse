'use client'

import { SocialMember } from '@/domains/member'
import { useFollowMutation, useMemberSearch } from '@/domains/follow/follow.hook'
import { useIntersectionObserver } from '@/hooks/useIntersectionObserver'
import Image from 'next/image'
import { useEffect } from 'react'
import MemberSearchResultItem from './MemberSearchResultItem'
import { MemberSearchResultListSkeleton } from './MemberSearchResultListSkeleton'

interface Props {
  query: string
}

export default function MemberSearchResultList({ query }: Props) {
  const { handleFollowToggle } = useFollowMutation()

  const { data, fetchNextPage, hasNextPage, isFetchingNextPage, isLoading } = useMemberSearch(query)

  const { targetRef, isIntersecting, resetIntersecting } = useIntersectionObserver({
    threshold: 0.1,
    rootMargin: '100px',
    enabled: hasNextPage && !isFetchingNextPage,
  })

  useEffect(() => {
    if (isIntersecting && hasNextPage && !isFetchingNextPage) {
      resetIntersecting()
      fetchNextPage()
    }
  }, [isIntersecting, hasNextPage, isFetchingNextPage, fetchNextPage, resetIntersecting])

  if (!query.trim()) {
    return null
  }

  if (isLoading) {
    return <MemberSearchResultListSkeleton />
  }

  const allMembers = data?.pages.flatMap((page) => page.data ?? []) ?? []

  if (allMembers.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center flex-1 pb-[70px]">
        <div className="relative w-[35px] h-[40px]">
          <Image src="/images/mypage/logo-gray.png" alt="로고" width={35} height={40} />
        </div>
        <div className="mt-[15px]">
          <p className="text-sm leading-[14px] text-[#aaaaaa]">검색 결과가 없습니다.</p>
        </div>
      </div>
    )
  }

  return (
    <>
      <div className="flex flex-col gap-[30px] py-[30px]">
        {allMembers.map((member: SocialMember) => (
          <MemberSearchResultItem
            key={member.memberId}
            member={member}
            onFollowToggle={handleFollowToggle}
          />
        ))}
      </div>
      {isFetchingNextPage && <MemberSearchResultListSkeleton />}
      <div ref={targetRef} className="h-1" aria-hidden="true" />
    </>
  )
}
