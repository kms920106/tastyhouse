import { shopRepository } from '@/domains/shop/shop.repository'
import type { Metadata } from 'next'
import ShopDetailPage from './_components/ShopDetailPage'
import type { ShopDetailTab } from './_components/ShopDetailTabs'

interface Props {
  params: Promise<{
    id: string
  }>
  searchParams: Promise<{
    tab?: string
  }>
}

export async function generateMetadata({ params }: Props): Promise<Metadata> {
  const { id } = await params
  const shopId = Number(id)

  const [detailRes, bannersRes] = await Promise.all([
    shopRepository.getShopDetail(shopId),
    shopRepository.getShopBanners(shopId),
  ])

  const detail = detailRes.data
  const banners = bannersRes.data

  if (!detail) return {}

  const thumbnailUrl = banners?.[0]?.imageUrl
  const description = `${detail.name} | ${detail.roadAddress ?? detail.lotAddress ?? ''}`

  return {
    title: detail.name,
    description,
    openGraph: {
      title: detail.name,
      description,
      ...(thumbnailUrl && { images: [thumbnailUrl] }),
    },
  }
}

const PLACE_TAB_VALUES: ShopDetailTab[] = ['info', 'menu', 'photo', 'review']

function parsePlaceTab(value: string | undefined): ShopDetailTab {
  return PLACE_TAB_VALUES.includes(value as ShopDetailTab) ? (value as ShopDetailTab) : 'info'
}

export default async function Page({ params, searchParams }: Props) {
  const [{ id }, { tab }] = await Promise.all([params, searchParams])
  const shopId = Number(id)
  const initialTab = parsePlaceTab(tab)

  return <ShopDetailPage shopId={shopId} tab={initialTab} />
}
