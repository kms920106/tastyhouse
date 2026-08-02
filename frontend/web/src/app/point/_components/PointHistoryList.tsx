import { PointType } from '@/domains/member'
import PointHistoryItem from './PointHistoryItem'

interface PointHistory {
  reason: string
  date: string
  pointType: PointType
  pointAmount: number
}

interface PointHistoryListProps {
  histories: PointHistory[]
}

export default function PointHistoryList({ histories }: PointHistoryListProps) {
  if (histories.length === 0) {
    return null
  }

  return histories.map((history, index) => (
    <PointHistoryItem
      key={index}
      reason={history.reason}
      date={history.date}
      pointType={history.pointType}
      pointAmount={history.pointAmount}
    />
  ))
}
