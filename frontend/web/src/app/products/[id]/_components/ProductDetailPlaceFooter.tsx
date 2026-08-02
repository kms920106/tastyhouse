import AppPrimaryButton from '@/components/ui/AppPrimaryButton'
import StickyFooter from '@/components/ui/StickyFooter'
import { productRepository } from '@/domains/product/product.repository'
import { PAGE_PATHS } from '@/lib/paths'
import Link from 'next/link'

interface Props {
  productId: number
}

export default async function ProductDetailPlaceFooter({ productId }: Props) {
  const { error, data } = await productRepository.getProductById(productId)

  if (error || !data) {
    return null
  }

  return (
    <StickyFooter>
      <div className="px-[15px] py-2.5 bg-[#f9f9f9]">
        <Link href={PAGE_PATHS.PLACE_DETAIL(data.shopId)}>
          <AppPrimaryButton>매장 보러가기</AppPrimaryButton>
        </Link>
      </div>
    </StickyFooter>
  )
}
