import RatingStar from '@/components/ui/RatingStar'
import { formatDecimal, formatNumber } from '@/lib/number'

interface Props {
  rating: number
  reviewCount: number
}

export default function ReviewRatingScore({ rating, reviewCount }: Props) {
  return (
    <div className="flex flex-col items-center">
      <div className="flex items-baseline gap-1 mb-[15px]">
        <span className="text-[32px] leading-[32px] tracking-[-1.6px]">
          {formatDecimal(rating, 1)}
        </span>
        <span className="text-base leading-[16px] text-[#999999] tracking-[-1.6px]">/</span>
        <span className="text-base leading-[16px] text-[#999999] tracking-[-1.6px]">5</span>
      </div>
      <div className="flex items-center gap-0 mb-2">
        {[1, 2, 3, 4, 5].map((star) => (
          <RatingStar key={star} starIndex={star} rating={rating} />
        ))}
      </div>
      <p className="text-[10px] leading-[10px] text-[#aaaaaa] tracking-[-0.5px]">
        {formatNumber(reviewCount)} 개의 리뷰
      </p>
    </div>
  )
}
