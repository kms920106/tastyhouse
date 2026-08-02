import { useState } from 'react'

export default function useImageLightbox() {
  const [isOpen, setIsOpen] = useState(false)
  const [currentIndex, setCurrentIndex] = useState(0)

  const openLightbox = (index: number) => {
    setCurrentIndex(index)
    setIsOpen(true)
  }

  const closeLightbox = () => {
    setIsOpen(false)
  }

  return {
    isOpen,
    currentIndex,
    openLightbox,
    closeLightbox,
  }
}
