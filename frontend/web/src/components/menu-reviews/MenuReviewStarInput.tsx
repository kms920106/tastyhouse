'use client'

import { MENU_REVIEW_RATING_MAX } from '@/domains/menu-review'
import { FaStar } from 'react-icons/fa'

const STARS = Array.from({ length: MENU_REVIEW_RATING_MAX }, (_, index) => index + 1)

interface Props {
  value: number
  onChange: (value: number) => void
  disabled?: boolean
}

export default function MenuReviewStarInput({ value, onChange, disabled }: Props) {
  return (
    <div className="flex gap-1">
      {STARS.map((star) => (
        <button
          key={star}
          type="button"
          disabled={disabled}
          aria-label={`${star}점`}
          onClick={() => onChange(star)}
          className="cursor-pointer transition-transform hover:scale-110 disabled:cursor-not-allowed disabled:hover:scale-100"
        >
          <FaStar size={24} className={star <= value ? 'text-main' : 'text-line'} />
        </button>
      ))}
    </div>
  )
}
