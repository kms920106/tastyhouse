'use client'

import 'swiper/css'
import 'swiper/css/pagination'
import 'yet-another-react-lightbox/styles.css'

import { useState } from 'react'

import 'yet-another-react-lightbox/plugins/counter.css'
import ImageLightbox from '../ui/ImageLightbox'
import ImageSwiper from '../ui/ImageSwiper'

interface Props {
  imageUrls: string[]
}

export default function ReviewImageGallery({ imageUrls }: Props) {
  const [lightboxOpen, setLightboxOpen] = useState(false)
  const [lightboxIndex, setLightboxIndex] = useState(0)

  if (imageUrls.length === 0) return null

  const openLightbox = (index: number) => {
    setLightboxIndex(index)
    setLightboxOpen(true)
  }

  return (
    <>
      <ImageSwiper imageUrls={imageUrls} onImageClick={openLightbox} />
      <ImageLightbox
        imageUrls={imageUrls}
        isOpen={lightboxOpen}
        currentIndex={lightboxIndex}
        onClose={() => setLightboxOpen(false)}
      />
    </>
  )
}
