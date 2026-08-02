'use client'

import KakaoMap from '@/app/map/_components/KakaoMap'
import QuickReviewLink from '@/components/ui/QuickReviewLink'
import SideBar from '@/components/ui/SideBar'
import SideBarButton from '@/components/ui/SideBarButton'
import { useState } from 'react'

export default function Page() {
  const [isOpen, setIsOpen] = useState(false)

  const togglePanel = () => setIsOpen((prev) => !prev)
  const closePanel = () => setIsOpen(false)

  return (
    <div className="relative min-h-screen">
      {!isOpen && <SideBarButton onClick={togglePanel} />}
      <SideBar isOpen={isOpen} closePanel={closePanel} />
      {isOpen && (
        <button
          aria-label="backdrop"
          onClick={closePanel}
          className="absolute inset-0 bg-black/30 z-30"
        />
      )}
      <QuickReviewLink />
      <KakaoMap />
    </div>
  )
}
