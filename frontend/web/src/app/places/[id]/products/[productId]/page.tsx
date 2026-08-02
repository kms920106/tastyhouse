import ShopProductDetailPage from './_components/ShopProductDetailPage'

interface Props {
  params: Promise<{ id: string; productId: string }>
}

export default async function Page({ params }: Props) {
  const { id, productId } = await params
  const shopId = Number(id)
  const productIdNum = Number(productId)

  return <ShopProductDetailPage shopId={shopId} productId={productIdNum} />
}
