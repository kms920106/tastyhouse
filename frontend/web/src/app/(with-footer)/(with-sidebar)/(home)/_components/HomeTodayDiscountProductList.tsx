import DiscountProductItem from '@/components/products/DiscountProductItem'
import Divider from '@/components/ui/Divider'
import FetchErrorState from '@/components/ui/FetchErrorState'
import ViewMoreButton from '@/components/ui/ViewMoreButton'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import { productRepository } from '@/domains/product/product.repository'
import { PAGE_PATHS } from '@/lib/paths'
import Link from 'next/link'
import { Fragment } from 'react'

export default async function HomeTodayDiscountProductList() {
  const { error, status, data } = await productRepository.getTodayDiscountProducts({
    page: 0,
    size: 4,
  })

  if ((error && status === 404) || !data) {
    return <FetchErrorState message={COMMON_ERROR_MESSAGES.FETCH_ERROR('메뉴')} />
  }

  if (error) {
    return <FetchErrorState message={COMMON_ERROR_MESSAGES.API_FETCH_ERROR} />
  }

  return (
    <>
      <div className="py-[15px] border-t border-b border-line">
        {data.map((product, i, arr) => (
          <Fragment key={product.id}>
            <Link href={PAGE_PATHS.PRODUCT_DETAIL(product.id)} className="block">
              <DiscountProductItem {...product} />
            </Link>
            {i < arr.length - 1 && <Divider />}
          </Fragment>
        ))}
      </div>
      <div className="flex justify-center mt-10">
        <ViewMoreButton href={PAGE_PATHS.PRODUCTS_TODAY_DISCOUNT} />
      </div>
    </>
  )
}
