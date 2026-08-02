import { rankRepository } from '@/domains/rank/rank.repository'
import {
  formatDate,
  formatRemainingTime,
  getCurrentYearMonth,
  getMonthRange,
  getTimeDifference,
} from '@/lib/date'
import type { RankTab } from './RankMemberTabs'

function getMonthlySchedule() {
  const { year, month } = getCurrentYearMonth()
  return getMonthRange(year, month)
}

export default async function RankSchedule({ tab }: { tab: RankTab }) {
  if (tab === 'monthly') {
    const { startAt, endAt } = getMonthlySchedule()
    const remainingTime = formatRemainingTime(getTimeDifference(endAt))
    const startDateFormatted = formatDate(startAt, 'YYYY.MM.DD')
    const endDateFormatted = formatDate(endAt, 'MM.DD')
    const dateRange = `${startDateFormatted} ~ ${endDateFormatted}`

    return (
      <div className="flex flex-col gap-1.5">
        <p className="text-sm leading-[14px] text-right">남은 기간 : {remainingTime}</p>
        <p className="text-sm leading-[14px] text-[#aaaaaa] text-right">({dateRange})</p>
      </div>
    )
  }

  const { error, data } = await rankRepository.getRankDuration()

  if (error || !data) {
    return <div>-</div>
  }

  const { startAt, endAt } = data
  const remainingTime = formatRemainingTime(getTimeDifference(endAt))
  const startDateFormatted = formatDate(startAt, 'YYYY.MM.DD')
  const endDateFormatted = formatDate(endAt, 'MM.DD')
  const dateRange = `${startDateFormatted} ~ ${endDateFormatted}`

  return (
    <div className="flex flex-col gap-1.5">
      <p className="text-sm leading-[14px] text-right">남은 기간 : {remainingTime}</p>
      <p className="text-sm leading-[14px] text-[#aaaaaa] text-right">({dateRange})</p>
    </div>
  )
}
