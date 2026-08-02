'use server'

import { followRepository } from '@/domains/follow/follow.repository'

export async function getFollowingList(memberId: number, { page, size }: { page: number; size: number }) {
  return followRepository.getFollowingList(memberId, { page, size })
}

export async function getFollowerList(memberId: number, { page, size }: { page: number; size: number }) {
  return followRepository.getFollowerList(memberId, { page, size })
}

export async function followMember(memberId: number) {
  return followRepository.followMember(memberId)
}

export async function unfollowMember(memberId: number) {
  return followRepository.unfollowMember(memberId)
}

export async function removeFollower(followerId: number) {
  return followRepository.removeFollower(followerId)
}

export async function searchMembersByNickname(
  nickname: string,
  page: number = 0,
  size: number = 20,
) {
  return followRepository.searchMembersByNickname(nickname, { page, size })
}

export async function getIsFollowing(memberId: number) {
  return followRepository.isFollowing(memberId)
}

export async function getPublicFollowingList(
  memberId: number,
  { page, size }: { page: number; size: number },
) {
  return followRepository.getPublicFollowingList(memberId, { page, size })
}

export async function getPublicFollowerList(
  memberId: number,
  { page, size }: { page: number; size: number },
) {
  return followRepository.getPublicFollowerList(memberId, { page, size })
}
