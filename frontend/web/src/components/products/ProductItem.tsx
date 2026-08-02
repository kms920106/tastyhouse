import Icon from '@/components/ui/Icon'
import ImageContainer from '@/components/ui/ImageContainer'
import Rating from '@/components/ui/Rating'
import { formatDecimal, formatNumber } from '@/lib/number'

interface Props {
  imageUrl: string
  spiciness: number | null
  name: string
  originalPrice: number
  discountPrice: number
  discountRate: number | null
  rating: number | null
  reviewCount: number | null
}

export default function ProductItem({
  imageUrl,
  spiciness,
  name,
  originalPrice,
  discountPrice,
  discountRate,
  rating,
  reviewCount,
}: Props) {
  return (
    <div className="flex items-center gap-[15px]">
      <ImageContainer src={imageUrl} alt="메뉴 이미지" size={65} />
      <div className="flex-1 flex flex-col min-w-0">
        <div className="flex items-center justify-between gap-2.5">
          <div className="flex flex-col min-w-0">
            {spiciness && (
              <div className="flex gap-[3px] mb-[7px]">
                {Array.from({ length: spiciness }).map((_, i) => (
                  <Icon name="product/spiciness" key={i} />
                ))}
              </div>
            )}
            <h4 className="mb-[9px] text-sm leading-[14px] truncate">{name}</h4>
            {discountRate == null ? (
              <p className="text-sm leading-[14px]">{formatNumber(originalPrice)}원</p>
            ) : (
              <div className="flex items-end leading-[21px]">
                <p className="text-sm leading-[14px]">{formatNumber(discountPrice)}원</p>
                <p className="ml-[7px] text-xs leading-[12px] text-[#aaaaaa] line-through">
                  {formatNumber(originalPrice)}원
                </p>
                <p className="ml-[11px] text-sm leading-[14px] text-main">
                  {formatDecimal(discountRate, 0)}%
                </p>
              </div>
            )}
          </div>
          {rating && reviewCount && (
            <div className="flex flex-col items-center gap-2.5">
              <Rating as="p" value={rating} />
              <p className="text-xs leading-[12px] text-[#999999] tracking-tighter">
                리뷰 ({reviewCount})
              </p>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
