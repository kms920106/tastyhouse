import ShopImageGallery from '@/components/shops/ShopImageGallery'
import { productRepository } from '@/domains/product/product.repository'

interface Props {
  productId: number
}

export default async function ProductDetailBannerContent({ productId }: Props) {
  const { error, data } = await productRepository.getProductImages(productId)

  if (error || !data) {
    return null
  }

  const { imageUrls } = data

  return <ShopImageGallery imageUrls={imageUrls} />
}
