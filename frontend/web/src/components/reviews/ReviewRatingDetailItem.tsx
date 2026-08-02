import RatingStar from '@/components/ui/RatingStar'
import { formatDecimal } from '@/lib/number'

interface Props {
  label: string
  rating: number
}

export default function ReviewRatingDetailItem({ label, rating }: Props) {
  return (
    <div className="flex items-center gap-[7px]">
      <span className="w-8 text-xs leading-[12px] text-[#666666]">{label}</span>
      <div className="flex items-center gap-[7px]">
        <div className="flex items-center">
          {[1, 2, 3, 4, 5].map((star) => (
            <RatingStar key={star} starIndex={star} rating={rating} />
          ))}
        </div>
        <span className="text-xs leading-[12px] text-main">{formatDecimal(rating, 1)}</span>
      </div>
    </div>
  )
}
