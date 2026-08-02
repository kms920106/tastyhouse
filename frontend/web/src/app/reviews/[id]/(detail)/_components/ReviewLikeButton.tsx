import { PiHeartFill, PiHeartThin } from 'react-icons/pi'

interface Props {
  onClick?: () => void
  disabled?: boolean
  isLiked: boolean
}

export default function ReviewLikeButton({ onClick, disabled, isLiked }: Props) {
  return (
    <button
      onClick={onClick}
      className="flex items-center gap-1.5 cursor-pointer"
      disabled={disabled}
    >
      {isLiked ? <PiHeartFill size={17} className="text-main" /> : <PiHeartThin size={17} />}
      <span className="text-xs leading-[12px]">좋아요</span>
    </button>
  )
}
