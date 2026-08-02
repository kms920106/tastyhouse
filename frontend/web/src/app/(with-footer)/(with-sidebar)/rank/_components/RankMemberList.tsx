import EmptyState from '@/app/(with-footer)/(without-sidebar)/mypage/_components/EmptyState'
import FetchErrorState from '@/components/ui/FetchErrorState'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import { RankType } from '@/domains/rank'
import { rankRepository } from '@/domains/rank/rank.repository'
import type { RankTab } from './RankMemberTabs'
import RankMemberListItem from './RankMemberListItem'

const RANK_TAB_TYPE_MAP: Record<RankTab, RankType> = {
  all: 'ALL',
  monthly: 'MONTHLY',
}

export default async function RankMemberList({ tab }: { tab: RankTab }) {
  const { error, status, data } = await rankRepository.getRankMembers({
    type: RANK_TAB_TYPE_MAP[tab],
    limit: 100,
  })

  if ((error && status === 404) || !data) {
    return <FetchErrorState message={COMMON_ERROR_MESSAGES.FETCH_ERROR('랭킹')} />
  }

  if (error) {
    return <FetchErrorState message={COMMON_ERROR_MESSAGES.API_FETCH_ERROR} />
  }

  if (data.length === 0) {
    return <EmptyState message="랭킹 데이터가 없습니다." />
  }
  return data.map((rankMember) => (
    <RankMemberListItem key={rankMember.memberId} rankMember={rankMember} />
  ))
}
