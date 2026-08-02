import ProductCategoryGroup from '@/components/products/ProductCategoryGroup'
import ProductItem from '@/components/products/ProductItem'
import BorderedSection from '@/components/ui/BorderedSection'
import ErrorStateSection from '@/components/ui/ErrorStateSection'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import type { OrderMethodType } from '@/domains/order'
import { shopRepository } from '@/domains/shop/shop.repository'
import { ProductCategory } from '@/domains/product'
import { PAGE_PATHS } from '@/lib/paths'
import Link from 'next/link'

interface Props {
  shopId: number
  orderMethod: OrderMethodType
}

export default async function ShopOrderMenuList({ shopId, orderMethod }: Props) {
  const { error, data } = await shopRepository.getShopProducts(shopId)

  if (error || !data) {
    return <ErrorStateSection message={COMMON_ERROR_MESSAGES.FETCH_ERROR('메뉴')} />
  }

  const menuCategories: ProductCategory[] = data

  return menuCategories.map((menuCategory) => (
    <BorderedSection key={menuCategory.categoryName}>
      <div className="px-[15px]">
        <ProductCategoryGroup categoryName={menuCategory.categoryName}>
          {menuCategory.products.map((product) => (
            <Link
              key={product.id}
              href={PAGE_PATHS.ORDER_MENU_DETAIL(shopId, product.id, orderMethod)}
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
              />
            </Link>
          ))}
        </ProductCategoryGroup>
      </div>
    </BorderedSection>
  ))
}
