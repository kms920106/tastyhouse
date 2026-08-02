import { FaStar } from 'react-icons/fa'

interface Props {
  starIndex: number
  rating: number
}

export default function RatingStar({ starIndex, rating }: Props) {
  const fullStars = Math.floor(rating)
  const decimal = rating - fullStars
  const fillPercentage = Math.round(decimal * 100)
  const hasPartialStar = decimal > 0

  if (starIndex <= fullStars) {
    // 전체 별
    return (
      <div className="relative">
        <FaStar size={14} fill="var(--main-color)" />
      </div>
    )
  } else if (starIndex === fullStars + 1 && hasPartialStar) {
    // 부분 별 (10%, 20%, ... 90%)
    const rightPercentage = 100 - fillPercentage
    return (
      <div className="relative w-[14px] h-[14px]">
        <FaStar size={14} fill="#efefef" className="absolute inset-0" />
        <div
          className="absolute inset-0 overflow-hidden"
          style={{ clipPath: `inset(0 ${rightPercentage}% 0 0)` }}
        >
          <FaStar size={14} fill="var(--main-color)" />
        </div>
      </div>
    )
  } else {
    // 빈 별
    return (
      <div className="relative">
        <FaStar size={14} fill="#efefef" />
      </div>
    )
  }
}
