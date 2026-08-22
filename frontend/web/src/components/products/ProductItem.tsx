import Icon from '@/components/ui/Icon'
import ImageContainer from '@/components/ui/ImageContainer'
import Rating from '@/components/ui/Rating'
import { formatDecimal, formatNumber } from '@/lib/number'

interface Props {
  /** 인기 메뉴 응답처럼 이미지가 없는 메뉴도 있어 null 을 허용한다 (ImageContainer 와 같은 폭) */
  imageUrl: string | null
  spiciness: number | null
  name: string
  originalPrice: number
  /** 할인이 없으면 null. `discountRate` 가 null 일 때는 렌더에 쓰이지 않는다 */
  discountPrice: number | null
  discountRate: number | null
  rating: number | null
  reviewCount: number | null
  /** 사장님 추천 메뉴 여부. 서버가 null 로 내려줄 수 있어 옵셔널로 둔다 */
  representative?: boolean | null
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
  representative,
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
            {/*
              사장님 추천 뱃지. 메뉴명 옆에 두고 `shrink-0` 로 고정한다 — 메뉴명은 truncate 로
              줄어들 수 있으므로, 뱃지까지 같이 줄어들면 "사장님 추..." 로 잘린다.
            */}
            <div className="flex items-center gap-[6px] mb-[9px] min-w-0">
              <h4 className="text-sm leading-[14px] truncate">{name}</h4>
              {representative && (
                <span className="shrink-0 px-[6px] py-[3px] bg-main text-[10px] leading-[10px] text-white rounded-[3px]">
                  사장님 추천
                </span>
              )}
            </div>
            {/*
              할인율이 없거나 할인가가 비어 있으면 정가만 보여준다 — 인기 메뉴 응답은
              할인 미설정 메뉴의 discountPrice 를 null 로 내려주므로 둘 다 확인해야 한다.
            */}
            {discountRate == null || discountPrice == null ? (
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
