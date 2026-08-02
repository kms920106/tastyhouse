import { shopRepository } from '@/domains/shop/shop.repository'
import type { Metadata } from 'next'
import ShopMapPage from './_components/ShopMapPage'

interface Props {
  params: Promise<{
    id: string
  }>
}

export async function generateMetadata({ params }: Props): Promise<Metadata> {
  const { id } = await params
  const { data } = await shopRepository.getShopDetail(Number(id))

  if (!data) return {}

  return {
    title: `${data.name} 위치`,
    description: `${data.name} | ${data.roadAddress ?? data.lotAddress ?? ''}`,
  }
}

export default async function Page({ params }: Props) {
  const { id } = await params
  return <ShopMapPage shopId={Number(id)} />
}
