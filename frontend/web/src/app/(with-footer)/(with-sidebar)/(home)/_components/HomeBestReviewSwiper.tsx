'use client'

import 'swiper/css'
import 'swiper/css/navigation'
import 'swiper/css/pagination'

import Rating from '@/components/ui/Rating'
import { PAGE_PATHS } from '@/lib/paths'
import { cn } from '@/lib/utils'
import Image from 'next/image'
import Link from 'next/link'
import { useEffect, useRef, useState } from 'react'
import { SlArrowLeft, SlArrowRight } from 'react-icons/sl'
import type { Swiper as SwiperType } from 'swiper'
import { Navigation } from 'swiper/modules'
import { Swiper, SwiperSlide } from 'swiper/react'
import { HomeBestReviewSwiperSkeleton } from './HomeBestReviewSwiperSkeleton'

interface BestReviewItem {
  id: number
  content: string
  imageUrl: string
  stationName: string
  shopName: string
  productName: string
  totalRating: number
}

interface Props {
  reviews: BestReviewItem[]
}

export default function HomeBestReviewSwiper({ reviews }: Props) {
  const swiperRef = useRef<SwiperType | null>(null)

  const [isMounted, setIsMounted] = useState(false)
  const [currentSlide, setCurrentSlide] = useState(2)
  const [totalSlides, setTotalSlides] = useState(reviews.length)

  // Swiper는 초기화 시 DOM을 직접 조작하고 window 객체에 접근하기 때문에
  // SSR 결과와 클라이언트 렌더링 결과가 달라져 React hydration mismatch가 발생한다.
  // 이는 Swiper 라이브러리의 구조적 문제로 2026년 현재도 미해결 상태이며,
  // next/dynamic ssr:false 또는 isMounted 패턴이 공식 권장 우회책이다.
  // Swiper GitHub Discussion #8068, Issue #6443 참고.
  useEffect(() => {
    setIsMounted(true)
  }, [])

  if (!isMounted) {
    return <HomeBestReviewSwiperSkeleton />
  }

  if (reviews.length === 0) {
    return null
  }

  return (
    <>
      <Swiper
        modules={[Navigation]}
        spaceBetween={15}
        slidesPerView={1.5}
        centeredSlides={true}
        initialSlide={1}
        onSwiper={(swiper) => {
          swiperRef.current = swiper
          setTotalSlides(swiper.slides.length)
        }}
        onSlideChange={(swiper) => {
          setCurrentSlide(swiper.realIndex + 1)
        }}
        className="!pb-[53px]"
      >
        {reviews.map((review, index) => (
          <SwiperSlide key={review.id} className="transition-all duration-300">
            {({ isActive }) => (
              <Link
                href={PAGE_PATHS.REVIEW_DETAIL(review.id)}
                className={cn(
                  'block h-full overflow-hidden transition-all duration-300',
                  isActive ? 'scale-100 opacity-100' : 'scale-90 opacity-60',
                )}
              >
                <div className="relative mb-[15px] w-full bg-gray-100 pt-[75%]">
                  <Image
                    src={review.imageUrl}
                    alt={review.content}
                    fill
                    sizes="(max-width: 640px) 100vw, (max-width: 768px) 50vw, (max-width: 1024px) 40vw, 33vw"
                    className="object-cover"
                    loading={index < 3 ? 'eager' : 'lazy'}
                  />
                </div>
                <div className="mb-1.5 flex items-center justify-between">
                  <span className="text-xs leading-[12px] text-[#999999] truncate">
                    {review.stationName}
                  </span>
                  <Rating value={review.totalRating} />
                </div>
                <h3 className="mb-[19px]  leading-[16px] truncate">
                  [{review.shopName}] {review.productName}
                </h3>
                <p className="text-xs leading-relaxed text-[#666666] line-clamp-4">
                  {review.content}
                </p>
              </Link>
            )}
          </SwiperSlide>
        ))}
      </Swiper>
      <div className="relative flex items-center justify-center pt-4">
        <SlArrowLeft
          className="absolute left-1/2 -translate-x-[85px] cursor-pointer"
          size={15}
          onClick={() => swiperRef.current?.slidePrev()}
        />
        <div className="absolute left-1/2 -translate-x-1/2 text-sm">
          {currentSlide} <span className="text-[#aaaaaa]"> / {totalSlides}</span>
        </div>
        <SlArrowRight
          className="absolute left-1/2 translate-x-[70px] cursor-pointer"
          size={15}
          onClick={() => swiperRef.current?.slideNext()}
        />
      </div>
    </>
  )
}
