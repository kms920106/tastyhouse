'use client'

import { useFollowMutation, useFollowers, useRemoveFollower } from '@/domains/follow/follow.hook'
import { useIntersectionObserver } from '@/hooks/useIntersectionObserver'
import Image from 'next/image'
import { useRouter } from 'next/navigation'
import { useEffect } from 'react'
import MemberFollowListItem from './MemberFollowListItem'
import { MemberFollowListSkeleton } from './MemberFollowListSkeleton'

interface Props {
  memberId: number
  searchQuery: string
  isLoggedIn: boolean
  isOwner: boolean
}

export default function MemberFollowFollowerTabContent({
  memberId,
  searchQuery,
  isLoggedIn,
  isOwner,
}: Props) {
  const router = useRouter()

  const { handleFollowToggle } = useFollowMutation()
  const { mutate: removeFollowerMutate } = useRemoveFollower(memberId, isLoggedIn)

  const { data, fetchNextPage, hasNextPage, isFetchingNextPage, isLoading } = useFollowers(
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

  const handleRemoveFollower = (targetMemberId: number) => {
    removeFollowerMutate(targetMemberId)
  }

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
            {searchQuery ? '검색 결과가 없습니다.' : '팔로워가 없습니다.'}
          </p>
        </div>
      </div>
    )
  }

  return (
    <>
      <div className="flex flex-col gap-[30px] py-[30px]">
        {filtered.map((member) => (
          <MemberFollowListItem
            key={member.memberId}
            member={member}
            isOwner={isOwner}
            onFollowToggle={isLoggedIn ? handleFollowToggle : () => router.push('/auth/login')}
            onRemoveFollower={isLoggedIn ? handleRemoveFollower : () => router.push('/auth/login')}
          />
        ))}
      </div>
      {isFetchingNextPage && <MemberFollowListSkeleton />}
      <div ref={targetRef} className="h-1" aria-hidden="true" />
    </>
  )
}
