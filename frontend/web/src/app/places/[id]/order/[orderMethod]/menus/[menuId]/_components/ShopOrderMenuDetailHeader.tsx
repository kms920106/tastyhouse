import Header, { HeaderLeft, HeaderRight } from '@/components/layouts/Header'
import { BackButton } from '@/components/layouts/header-parts'
import ProductShareButton from '@/components/products/ProductShareButton'
import type { OrderMethodType } from '@/domains/order'
import { productRepository } from '@/domains/product/product.repository'
import ShopOrderMenuDetailCartButtonClient from './ShopOrderMenuDetailCartButtonClient'

interface Props {
  shopId: number
  productId: number
  orderMethod: OrderMethodType
}

export default async function ShopOrderMenuDetailHeader({
  shopId,
  productId,
  orderMethod,
}: Props) {
  const { error, data } = await productRepository.getProductById(productId)

  if (error || !data) {
    return null
  }

  const { name } = data

  return (
    <Header variant="white" height={55}>
      <HeaderLeft>
        <BackButton />
      </HeaderLeft>
      <HeaderRight>
        <ProductShareButton productId={productId} productName={name} />
        <ShopOrderMenuDetailCartButtonClient shopId={shopId} orderMethod={orderMethod} />
      </HeaderRight>
    </Header>
  )
}
