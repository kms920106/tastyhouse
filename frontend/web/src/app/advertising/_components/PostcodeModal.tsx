'use client'

import DaumPostcodeEmbed, { Address } from 'react-daum-postcode'
import { useEffect } from 'react'

interface Props {
  onComplete: (address: Address) => void
  onClose: () => void
}

export default function PostcodeModal({ onComplete, onClose }: Props) {
  useEffect(() => {
    document.body.style.overflow = 'hidden'
    return () => {
      document.body.style.overflow = ''
    }
  }, [])

  const handleComplete = (data: Address) => {
    onComplete(data)
    onClose()
  }

  return (
    <div
      className="fixed inset-0 z-50 flex items-end bg-black/50"
      onClick={onClose}
    >
      <div
        className="w-full bg-white"
        onClick={(e) => e.stopPropagation()}
      >
        {/* 헤더 */}
        <div className="flex items-center justify-between px-[15px] py-3 border-b border-line">
          <span className="text-[14px] font-bold">우편번호 검색</span>
          <button
            type="button"
            onClick={onClose}
            className="p-1 text-[#999999] active:text-foreground"
            aria-label="닫기"
          >
            <svg width="20" height="20" viewBox="0 0 20 20" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round">
              <path d="M4 4l12 12M16 4L4 16" />
            </svg>
          </button>
        </div>

        {/* 다음 우편번호 컴포넌트 */}
        <DaumPostcodeEmbed
          onComplete={handleComplete}
          style={{ width: '100%', height: 400 }}
          autoClose={false}
        />
      </div>
    </div>
  )
}
