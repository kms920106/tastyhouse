'use client'

import 'swiper/css'
import 'swiper/css/pagination'
import styles from '@/components/reviews/ReviewImageGallery.module.css'

import { cn } from '@/lib/utils'
import Image from 'next/image'
import { Pagination } from 'swiper/modules'
import { Swiper, SwiperSlide } from 'swiper/react'

interface Props {
  imageUrls: string[]
  onImageClick: (index: number) => void
}

export default function ImageSwiper({ imageUrls, onImageClick }: Props) {
  if (imageUrls.length === 0) return null

  return (
    <Swiper
      modules={[Pagination]}
      spaceBetween={0}
      slidesPerView={1}
      pagination={{
        type: 'fraction',
        formatFractionCurrent: (number) => number,
        formatFractionTotal: (number) => number,
      }}
      className={cn('aspect-[345/190]', styles.reviewSwiper, imageUrls.length === 1 && styles.reviewSwiperSingleImage)}
    >
      {imageUrls.map((imageUrl: string, index: number) => (
        <SwiperSlide key={index}>
          <div
            className="relative w-full h-full cursor-pointer"
            onClick={() => onImageClick(index)}
          >
            <Image
              src={imageUrl}
              alt={`이미지 ${index + 1}`}
              fill
              sizes="calc(100vw - 30px)"
              className="object-cover"
            />
          </div>
        </SwiperSlide>
      ))}
    </Swiper>
  )
}
