'use client'

import ProductCategoryGroup from '@/components/products/ProductCategoryGroup'
import ProductItem from '@/components/products/ProductItem'
import { useShopPopularProducts } from '@/domains/shop/shop.hook'
import type { ShopPopularProduct } from '@/domains/shop/shop.model'
import { PAGE_PATHS } from '@/lib/paths'
import Link from 'next/link'

interface Props {
  shopId: number
}

/**
 * "가장 인기 있는 메뉴" 그룹.
 *
 * 사장님 추천 메뉴가 먼저 채워지고 남은 자리를 최근 30일 판매량으로 채운 최대 5건을 서버가
 * 정렬해서 내려주므로, 프론트는 재정렬하거나 개수를 자르지 않는다.
 *
 * 로딩·에러·빈 목록을 모두 `null` 로 처리한다 — 이 그룹은 아래 전체 메뉴 목록의 요약 진입점일
 * 뿐이라, 실패했을 때 에러 박스를 띄우면 정작 볼 수 있는 메뉴판 위에 잡음만 얹힌다.
 * 판매 이력이 없는 신규 가게에서 항목이 0건인 것도 정상 상태다(`frontend.md` C-4).
 */
export default function ShopPopularMenuGroup({ shopId }: Props) {
  const { data, isLoading, error } = useShopPopularProducts(shopId)

  if (isLoading || error) return null

  const popularProducts: ShopPopularProduct[] = data?.data ?? []

  if (popularProducts.length === 0) return null

  return (
    <ProductCategoryGroup
      categoryName="가장 인기 있는 메뉴"
      className="border-b border-line box-border"
    >
      {popularProducts.map((product) => (
        <Link
          key={product.id}
          href={PAGE_PATHS.PLACE_PRODUCT_DETAIL(shopId, product.id)}
          className="block"
        >
          <ProductItem
            imageUrl={product.imageUrl}
            spiciness={product.spiciness}
            name={product.name}
            originalPrice={product.originalPrice}
            discountPrice={product.discountPrice}
            discountRate={product.discountRate}
            rating={product.rating}
            reviewCount={product.reviewCount}
            representative={product.representative}
          />
        </Link>
      ))}
    </ProductCategoryGroup>
  )
}
