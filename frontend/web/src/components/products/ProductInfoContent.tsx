import FetchErrorState from '@/components/ui/FetchErrorState'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import { productRepository } from '@/domains/product/product.repository'
import { formatDecimal, formatNumber } from '@/lib/number'

interface Props {
  productId: number
}

export default async function ProductInfoContent({ productId }: Props) {
  const { error, data } = await productRepository.getProductById(productId)

  if (error || !data) {
    return <FetchErrorState message={COMMON_ERROR_MESSAGES.FETCH_ERROR('상품 정보')} />
  }

  const { name, description, originalPrice, discountPrice, discountRate, weightText } = data

  return (
    <div className="px-[15px] py-[21px]">
      <h1 className="text-lg leading-[18px] font-bold">{name}</h1>
      {/* 중량은 설명보다 앞에 둔다 — 치킨 중량표시 규제가 고객이 바로 확인할 수 있게 하라고 안내한다.
          별도 필드라 설명 문구와 무관하게 이 배치를 화면이 보장할 수 있다. */}
      {weightText && <p className="mt-[13px] text-sm leading-[14px] text-[#666666]">{weightText}</p>}
      <p className="mt-[13px] text-sm leading-relaxed">{description}</p>
      <div className="mt-[17px]">
        {discountRate == null ? (
          <p className="mt-[13px] text-base leading-[16px]">{formatNumber(originalPrice)}원</p>
        ) : (
          <div className="flex items-end leading-[21px]">
            <p className="text-base leading-[16px]">{formatNumber(discountPrice ?? 0)}원</p>
            <p className="ml-[7px] text-xs leading-[12px] text-[#aaaaaa] line-through">
              {formatNumber(originalPrice)}원
            </p>
            <p className="ml-[11px] text-sm leading-[14px] text-main">
              {formatDecimal(discountRate, 0)}%
            </p>
          </div>
        )}
      </div>
    </div>
  )
}
