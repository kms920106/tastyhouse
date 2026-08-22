import type { ProductPrice } from '@/domains/product'
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
  /**
   * 가격 행 목록.
   *
   * 목록 응답이 아직 내려주지 않는 화면도 있어 옵셔널이다. **2개 이상일 때만** 가격명별 목록으로
   * 바꿔 보여주고, 1개거나 없으면 기존 정가·할인가 표시를 그대로 쓴다 — 대부분의 메뉴가
   * 가격 1개라 기존 화면이 그대로 돌아야 한다.
   */
  prices?: ProductPrice[] | null
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
  prices,
}: Props) {
  // 가격명은 행이 2개 이상일 때만 의미가 있다(1개면 가격명이 null 이고 표시 대상이 아니다).
  const hasMultiplePrices = prices != null && prices.length > 1
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
            {hasMultiplePrices ? (
              /*
                가격이 여러 개면 가격명과 함께 나열한다. `price` 는 서버가 주문유형에 따라 이미
                해석한 값이라 화면이 다시 고르지 않는다 — 고르면 서버 계산과 어긋나 주문이 거절된다.
              */
              <div className="flex flex-col gap-[4px]">
                {prices.map((row) => (
                  <div key={row.priceId} className="flex items-center gap-[6px]">
                    {row.priceName != null && (
                      <span className="text-xs leading-[12px] text-[#999999]">{row.priceName}</span>
                    )}
                    <p className="text-sm leading-[14px]">{formatNumber(row.price)}원</p>
                  </div>
                ))}
              </div>
            ) : discountRate == null || discountPrice == null ? (
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
