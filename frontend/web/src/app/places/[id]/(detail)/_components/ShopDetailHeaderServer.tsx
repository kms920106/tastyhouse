import { HeaderTitle } from '@/components/layouts/Header'
import { shopRepository } from '@/domains/shop/shop.repository'

interface Props {
  shopId: number
}

export default async function ShopDetailHeaderServer({ shopId }: Props) {
  const { error, data } = await shopRepository.getShopDetail(shopId)

  if (error) {
    return <HeaderTitle>-</HeaderTitle>
  }

  if (!data) {
    return <HeaderTitle>-</HeaderTitle>
  }

  const { name } = data

  return <HeaderTitle>{name}</HeaderTitle>
}
