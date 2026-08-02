'use client'

import { SocialMember } from '@/domains/member'
import { useFollowMutation, useFollowing } from '@/domains/follow/follow.hook'
import { useIntersectionObserver } from '@/hooks/useIntersectionObserver'
import Image from 'next/image'
import { useEffect } from 'react'
import { useRouter } from 'next/navigation'
import MemberFollowListItem from './MemberFollowListItem'
import { MemberFollowListSkeleton } from './MemberFollowListSkeleton'

interface Props {
  memberId: number
  searchQuery: string
  isLoggedIn: boolean
}

export default function MemberFollowFollowingTabContent({
  memberId,
  searchQuery,
  isLoggedIn,
}: Props) {
  const router = useRouter()

  const { handleFollowToggle } = useFollowMutation()

  const { data, fetchNextPage, hasNextPage, isFetchingNextPage, isLoading } = useFollowing(
    memberId,
    isLoggedIn,
  )

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

  if (isLoading) {
    return <MemberFollowListSkeleton />
  }

  const allMembers = data?.pages.flatMap((page) => page.data ?? []) ?? []
  const filtered = allMembers.filter((member) =>
    member.nickname.toLowerCase().includes(searchQuery.toLowerCase()),
  )

  if (filtered.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center flex-1 pb-[70px]">
        <div className="relative w-[35px] h-[40px]">
          <Image src="/images/mypage/logo-gray.png" alt="로고" width={35} height={40} />
        </div>
        <div className="mt-[15px]">
          <p className="text-sm leading-[14px] text-[#aaaaaa]">
            {searchQuery ? '검색 결과가 없습니다.' : '팔로잉이 없습니다.'}
          </p>
        </div>
      </div>
    )
  }

  return (
    <>
      <div className="flex flex-col gap-[30px] py-[30px]">
        {filtered.map((member: SocialMember) => (
          <MemberFollowListItem
            key={member.memberId}
            member={member}
            isOwner={false}
            onFollowToggle={isLoggedIn ? handleFollowToggle : () => router.push('/auth/login')}
            onRemoveFollower={() => {}}
          />
        ))}
      </div>
      {isFetchingNextPage && <MemberFollowListSkeleton />}
      <div ref={targetRef} className="h-1" aria-hidden="true" />
    </>
  )
}
