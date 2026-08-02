import ProductDetailPage from './_components/ProductDetailPage'

interface Props {
  params: Promise<{ id: string }>
}

export default async function Page({ params }: Props) {
  const { id } = await params
  const productId = Number(id)

  return <ProductDetailPage productId={productId} />
}
