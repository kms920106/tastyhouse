import ImageContainer from '@/components/ui/ImageContainer'
import type { OrderedProductOption } from '@/domains/order'
import { formatNumber } from '@/lib/number'

interface Props {
  productName: string
  /** 가격명("곱빼기" 등). 가격이 1개인 메뉴는 없으므로 표시하지 않는다 */
  priceName?: string | null
  productImageUrl: string
  totalPrice: number
  quantity?: number
  options?: OrderedProductOption[]
  action?: React.ReactNode
}

export default function OrderProductItem({
  productName,
  priceName,
  productImageUrl,
  totalPrice,
  quantity,
  options,
  action,
}: Props) {
  return (
    <div className="flex items-center gap-[15px] py-[15px]">
      <ImageContainer src={productImageUrl} alt={productName} size={50} />
      <div className="flex flex-col gap-2.5 flex-1">
        <h3 className="text-sm leading-[14px]">{productName}</h3>
        {/* 가격명은 옵션보다 위에 둔다 — 가격을 정하는 선택이므로 옵션 추가금과 구분돼야 한다 */}
        {priceName && <p className="text-xs text-[#999999]">{priceName}</p>}
        {options && options.length > 0 && (
          <div className="space-y-1">
            {options.map((opt, index) => (
              <p key={index} className="text-xs text-[#999999]">
                {opt.optionName}
                {opt.additionalPrice > 0 && ` (${formatNumber(opt.additionalPrice)}원)`}
              </p>
            ))}
          </div>
        )}
        <p className="text-sm leading-[14px]">
          {formatNumber(totalPrice)}원{quantity !== undefined && ` | ${quantity}개`}
        </p>
      </div>
      {action}
    </div>
  )
}
