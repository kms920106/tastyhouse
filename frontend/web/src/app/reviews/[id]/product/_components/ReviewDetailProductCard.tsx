import BorderedSection from '@/components/ui/BorderedSection'
import { formatNumber } from '@/lib/number'
import Image from 'next/image'

interface Props {
  productImageUrl: string | null
  productName: string
  productPrice: number
}

export default function ReviewDetailProductCard({ productImageUrl, productName, productPrice }: Props) {
  return (
    <BorderedSection>
      <div className="px-[15px] py-5 flex items-center gap-4">
        <div className="relative w-[50px] h-[50px] flex-shrink-0 overflow-hidden">
          <Image
            src={productImageUrl ?? ''}
            alt={productName}
            fill
            className="object-cover"
            sizes="50px"
          />
        </div>
        <div className="flex-1 flex flex-col min-w-0">
          <h3 className="text-sm leading-[14px] truncate">{productName}</h3>
          <span className="mt-2.5 text-sm leading-[14px]">
            {formatNumber(productPrice)}원
          </span>
        </div>
      </div>
    </BorderedSection>
  )
}
