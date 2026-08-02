import { shopRepository } from '@/domains/shop/shop.repository'
import ShopDetailShareButtonClient from './ShopDetailShareButtonClient'
import ShopDetailShareButtonError from './ShopDetailShareButtonError'

interface Props {
  shopId: number
}

export default async function ShopDetailShareButtonServer({ shopId }: Props) {
  const { error, data } = await shopRepository.getShopDetail(shopId)

  if (error) {
    return <ShopDetailShareButtonError />
  }

  if (!data) {
    return <ShopDetailShareButtonError />
  }

  const { name } = data

  return <ShopDetailShareButtonClient shopId={shopId} shopName={name} />
}
