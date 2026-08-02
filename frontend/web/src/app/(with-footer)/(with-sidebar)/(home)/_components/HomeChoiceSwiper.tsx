'use client'

import 'swiper/css'
import 'swiper/css/navigation'
import 'swiper/css/pagination'

import DiscountProductItem from '@/components/products/DiscountProductItem'
import Divider from '@/components/ui/Divider'
import { PAGE_PATHS } from '@/lib/paths'
import Image from 'next/image'
import Link from 'next/link'
import { Fragment, useEffect, useState } from 'react'
import { Swiper, SwiperSlide } from 'swiper/react'
import { HomeChoiceSwiperSkeleton } from './HomeChoiceSwiperSkeleton'

interface Product {
  id: number
  name: string
  shopName: string
  imageUrl: string
  originalPrice: number
  discountPrice: number
  discountRate: number
}

interface Place {
  id: number
  imageUrl: string
  name: string
  title: string
  content: string
  products: Product[]
}

interface Props {
  places: Place[]
}

export default function HomeChoiceSwiper({ places }: Props) {
  const [isMounted, setIsMounted] = useState(false)

  // Swiper는 초기화 시 DOM을 직접 조작하고 window 객체에 접근하기 때문에
  // SSR 결과와 클라이언트 렌더링 결과가 달라져 React hydration mismatch가 발생한다.
  // 이는 Swiper 라이브러리의 구조적 문제로 2026년 현재도 미해결 상태이며,
  // next/dynamic ssr:false 또는 isMounted 패턴이 공식 권장 우회책이다.
  // Swiper GitHub Discussion #8068, Issue #6443 참고.
  useEffect(() => {
    setIsMounted(true)
  }, [])

  if (!isMounted) {
    return <HomeChoiceSwiperSkeleton />
  }

  if (places.length === 0) {
    return null
  }

  return (
    <>
      <Swiper spaceBetween={20} slidesPerView={1.15} className="pb-12">
        {places.map((place) => (
          <SwiperSlide key={place.id}>
            <Link href={PAGE_PATHS.PLACE_DETAIL(place.id)}>
              <div className="relative w-full aspect-[2/3] overflow-hidden">
                <Image
                  src={place.imageUrl}
                  alt={place.name}
                  fill
                  className="object-cover"
                  sizes="100vw"
                />
                <div className="absolute inset-0 bg-gradient-to-t from-black/60 via-black/20 to-transparent" />
                <div className="absolute bottom-0 left-0 right-0 px-5 py-[30px] text-white">
                  <h3 className="mb-3 text-[17px] leading-[17px] font-nanum-myeongjo-bold font-bold">
                    {place.title}
                  </h3>
                  <p className="text-xs opacity-90 leading-relaxed">{place.content}</p>
                </div>
              </div>
            </Link>
            <div className="py-[15px] border-t border-b border-line">
              {place.products.map((product, i, arr) => (
                <Fragment key={product.id}>
                  <Link href={PAGE_PATHS.PRODUCT_DETAIL(product.id)} className="block">
                    <DiscountProductItem {...product} />
                  </Link>
                  {i < arr.length - 1 && <Divider />}
                </Fragment>
              ))}
            </div>
          </SwiperSlide>
        ))}
      </Swiper>
    </>
  )
}
