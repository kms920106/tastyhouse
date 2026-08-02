import { FaStar } from 'react-icons/fa'

interface RatingCategory {
  key: 'taste' | 'amount' | 'price'
  label: string
}

const RATING_CATEGORIES: RatingCategory[] = [
  { key: 'taste', label: '맛은 어떤가요?' },
  { key: 'amount', label: '양은 어떤가요?' },
  { key: 'price', label: '가격은 어떤가요?' },
]

interface Props {
  ratings: { taste: number; amount: number; price: number }
  error?: string
  onRatingChange: (key: 'taste' | 'amount' | 'price', value: number) => void
}

export default function ReviewRatingSection({ ratings, error, onRatingChange }: Props) {
  return (
    <div className="px-[15px] divide-y divide-line">
      {RATING_CATEGORIES.map(({ key, label }) => (
        <div key={key} className="flex flex-col items-center gap-5 py-[30px]">
          <p className="text-base leading-[16px]">{label}</p>
          <div className="flex gap-2">
            {[1, 2, 3, 4, 5].map((star) => (
              <button
                key={star}
                type="button"
                onClick={() => onRatingChange(key, star)}
                className="cursor-pointer transition-transform hover:scale-110"
              >
                <FaStar
                  size={40}
                  className={star <= ratings[key] ? 'text-main' : 'text-line'}
                />
              </button>
            ))}
          </div>
          {error && ratings[key] === 0 && (
            <p className="text-xs leading-[12px] text-[#bc4040]">{error}</p>
          )}
        </div>
      ))}
    </div>
  )
}
