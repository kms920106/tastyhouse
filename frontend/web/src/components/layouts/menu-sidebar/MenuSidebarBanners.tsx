'use client'

import { useSidebarBanners } from '@/domains/banner/banner.hook'
import Image from 'next/image'
import Link from 'next/link'
import 'swiper/css'
import { Autoplay } from 'swiper/modules'
import { Swiper, SwiperSlide } from 'swiper/react'

export default function MenuSidebarBanners() {
  const { banners } = useSidebarBanners()

  return (
    <div className="relative w-full shrink-0 pb-10">
      <Swiper
        modules={[Autoplay]}
        autoplay={{ delay: 3000, disableOnInteraction: false }}
        spaceBetween={10}
        slidesPerView={1.2}
        centeredSlidesBounds={true}
        centeredSlides={true}
        initialSlide={0}
        className="w-full py-2"
      >
        {banners.map((banner, index) => (
          <SwiperSlide key={banner.id}>
            <div className="relative w-full aspect-[3/1] rounded-[5px] overflow-hidden">
              {banner.linkUrl ? (
                <Link
                  href={banner.linkUrl}
                  rel="noopener noreferrer"
                  className="relative block w-full h-full"
                >
                  <Image
                    src={banner.imageUrl}
                    alt={banner.title}
                    fill
                    className="object-cover"
                    priority={index === 0}
                  />
                </Link>
              ) : (
                <Image
                  src={banner.imageUrl}
                  alt={banner.title}
                  fill
                  className="object-cover"
                  priority={index === 0}
                />
              )}
            </div>
          </SwiperSlide>
        ))}
      </Swiper>
    </div>
  )
}
