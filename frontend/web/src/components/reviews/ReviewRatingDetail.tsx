import { formatDecimal } from '@/lib/number'
import ReviewRatingDetailItem from './ReviewRatingDetailItem'

interface Props {
  averageAtmosphereRating: number
  averageKindnessRating: number
  averageTasteRating: number
  averageAmountRating: number
  averageHygieneRating: number
  averagePriceRating: number
  willRevisitPercentage: number
}

export default function ReviewRatingDetail({
  averageAtmosphereRating,
  averageKindnessRating,
  averageTasteRating,
  averageAmountRating,
  averageHygieneRating,
  averagePriceRating,
  willRevisitPercentage,
}: Props) {
  return (
    <div className="flex flex-row justify-between">
      <div className="space-y-2.5">
        <ReviewRatingDetailItem label="분위기" rating={averageAtmosphereRating} />
        <ReviewRatingDetailItem label="양" rating={averageAmountRating} />
        <ReviewRatingDetailItem label="위생" rating={averageHygieneRating} />
        <div className="flex-1 flex items-center">
          <div className="flex items-center w-full">
            <span className="w-20 text-xs leading-[12px] text-[#666666]">재방문의사</span>
            <span className="flex-1 flex items-center text-xs leading-[12px] text-main">
              있어요 ({formatDecimal(willRevisitPercentage, 0)}%)
            </span>
          </div>
        </div>
      </div>
      <div className="space-y-2.5">
        <ReviewRatingDetailItem label="친절" rating={averageKindnessRating} />
        <ReviewRatingDetailItem label="맛" rating={averageTasteRating} />
        <ReviewRatingDetailItem label="가격" rating={averagePriceRating} />
      </div>
    </div>
  )
}
