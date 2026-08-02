import { Spinner } from '@/components/ui/shadcn/spinner'
import { RxPaperPlane } from 'react-icons/rx'

interface Props {
  onClick?: () => void
  isSubmitting: boolean
}

export default function ReviewCommentSubmitButton({ onClick, isSubmitting }: Props) {
  return (
    <button
      type="button"
      className="flex justify-end items-center w-[22px] h-[44px] disabled:opacity-50 cursor-pointer"
      onClick={onClick}
      disabled={isSubmitting}
    >
      {isSubmitting ? <Spinner /> : <RxPaperPlane size={22} color="#cccccc" />}
    </button>
  )
}
