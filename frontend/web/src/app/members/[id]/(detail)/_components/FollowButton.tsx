'use client'

import Link from 'next/link'
import { Skeleton } from '@/components/ui/shadcn/skeleton'
import { useFollowMutation, useIsFollowing } from '@/domains/follow/follow.hook'
import { PAGE_PATHS } from '@/lib/paths'
import { IoPersonAdd, IoPersonRemove } from 'react-icons/io5'

interface Props {
  memberId: number
  isLoggedIn: boolean
}

export default function FollowButton({ memberId, isLoggedIn }: Props) {
  const { handleFollowToggle } = useFollowMutation()

  const { data, isLoading } = useIsFollowing(memberId, isLoggedIn)
  const following = data?.following ?? false

  if (isLoggedIn && isLoading) {
    return <Skeleton className="w-10 h-10 rounded-full" />
  }

  if (!isLoggedIn) {
    return (
      <Link href={PAGE_PATHS.AUTH_LOGIN} className="flex items-center justify-center w-10 h-10">
        <IoPersonAdd size={24} className="text-white" />
      </Link>
    )
  }

  return (
    <button
      onClick={() => handleFollowToggle({ memberId, following })}
      className="flex items-center justify-center w-10 h-10 cursor-pointer"
    >
      {following ? (
        <IoPersonRemove size={24} className="text-white" />
      ) : (
        <IoPersonAdd size={24} className="text-white" />
      )}
    </button>
  )
}
