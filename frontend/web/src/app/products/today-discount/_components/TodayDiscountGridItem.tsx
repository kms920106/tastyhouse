import { formatDecimal, formatNumber } from '@/lib/number'
import { PAGE_PATHS } from '@/lib/paths'
import Image from 'next/image'
import Link from 'next/link'

interface Props {
  id: number
  imageUrl: string
  name: string
  shopName: string
  originalPrice: number
  discountPrice: number
  discountRate: number
}

export default function TodayDiscountGridItem({
  id,
  imageUrl,
  name,
  shopName,
  originalPrice,
  discountPrice,
  discountRate,
}: Props) {
  return (
    <Link href={PAGE_PATHS.PRODUCT_DETAIL(id)} className="block">
      <div className="relative w-full aspect-square overflow-hidden bg-[#f5f5f5]">
        {imageUrl && (
          <Image src={imageUrl} alt={name} fill className="object-cover" sizes="50vw" />
        )}
      </div>
      <div className="pt-2.5">
        <p className="text-xs leading-[12px] text-[#aaaaaa] truncate mb-1.5">{shopName}</p>
        <h3 className="text-sm leading-[14px] text-[#333333] truncate mb-2">{name}</h3>
        <div className="flex items-baseline gap-1.5 flex-wrap">
          <span className="text-sm leading-[14px] font-medium text-[#333333]">
            {formatNumber(discountPrice)}원
          </span>
          {discountRate > 0 && (
            <>
              <span className="text-xs leading-[12px] text-[#aaaaaa] line-through">
                {formatNumber(originalPrice)}원
              </span>
              <span className="text-xs leading-[12px] text-main font-medium">
                {formatDecimal(discountRate, 0)}%
              </span>
            </>
          )}
        </div>
      </div>
    </Link>
  )
}
