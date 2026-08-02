import BorderedSection from '@/components/ui/BorderedSection'
import FetchErrorState from '@/components/ui/FetchErrorState'
import SectionStack from '@/components/ui/SectionStack'
import { memberRepository } from '@/domains/member/member.repository'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import PointBalance from './PointBalance'
import PointHistoryList from './PointHistoryList'

export default async function PointContent() {
  const { error, status, data } = await memberRepository.getMyPointHistory()

  if ((error && status === 404) || !data) {
    return <FetchErrorState message={COMMON_ERROR_MESSAGES.FETCH_ERROR('포인트')} />
  }

  if (error) {
    return <FetchErrorState message={COMMON_ERROR_MESSAGES.API_FETCH_ERROR} />
  }

  const { availablePoints, expiredThisMonth, histories } = data

  return (
    <SectionStack>
      <BorderedSection>
        <PointBalance availablePoints={availablePoints} expiredThisMonth={expiredThisMonth} />
      </BorderedSection>
      <BorderedSection>
        <div className="px-4 divide-y divide-[#f2f2f2]">
          <PointHistoryList histories={histories} />
        </div>
      </BorderedSection>
    </SectionStack>
  )
}
