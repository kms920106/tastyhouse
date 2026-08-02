import FetchErrorState from '@/components/ui/FetchErrorState'
import { RankType } from '@/domains/rank'
import { rankRepository } from '@/domains/rank/rank.repository'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import type { RankTab } from './RankMemberTabs'
import RankMemberInfo from './RankMemberInfo'

const RANK_TAB_TYPE_MAP: Record<RankTab, RankType> = {
  all: 'ALL',
  monthly: 'MONTHLY',
}

export default async function RankMyInfo({ tab }: { tab: RankTab }) {
  const { error, status, data } = await rankRepository.getRankMembersMe({
    type: RANK_TAB_TYPE_MAP[tab],
  })

  if ((error && status === 404) || !data) {
    return <FetchErrorState message={COMMON_ERROR_MESSAGES.FETCH_ERROR('내 랭킹')} />
  }

  if (error) {
    return <FetchErrorState message={COMMON_ERROR_MESSAGES.API_FETCH_ERROR} />
  }

  const { nickname, profileImageUrl, reviewCount, rankNo, grade } = data

  return (
    <RankMemberInfo
      rankNo={rankNo}
      profileImageUrl={profileImageUrl}
      nickname={nickname}
      grade={grade}
      reviewCount={reviewCount}
      isMe
    />
  )
}
