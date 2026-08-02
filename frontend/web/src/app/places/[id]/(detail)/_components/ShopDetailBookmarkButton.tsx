import { cn } from '@/lib/utils'
import { FaBookmark, FaRegBookmark } from 'react-icons/fa'

interface Props {
  onClick?: () => void
  disabled?: boolean
  isBookmarked: boolean
}

export default function ShopDetailBookmarkButton({ onClick, disabled, isBookmarked }: Props) {
  return (
    <button
      onClick={onClick}
      className={cn(
        'flex items-center justify-center w-[35px] h-[35px] shrink-0 border rounded-full cursor-pointer box-border',
        isBookmarked ? 'border-main' : 'border-line',
      )}
      disabled={disabled}
    >
      {isBookmarked ? (
        <FaBookmark size={16} className="text-main" />
      ) : (
        <FaRegBookmark size={16} className="text-line" />
      )}
    </button>
  )
}
