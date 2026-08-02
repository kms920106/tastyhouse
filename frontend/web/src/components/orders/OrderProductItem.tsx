import ImageContainer from '@/components/ui/ImageContainer'
import type { OrderedProductOption } from '@/domains/order'
import { formatNumber } from '@/lib/number'

interface Props {
  productName: string
  productImageUrl: string
  totalPrice: number
  quantity?: number
  options?: OrderedProductOption[]
  action?: React.ReactNode
}

export default function OrderProductItem({
  productName,
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
