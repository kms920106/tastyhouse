'use client'

import { toast } from '@/components/ui/AppToaster'
import { cn } from '@/lib/utils'

interface Props {
  form: {
    shopName?: string
    menuName?: string
    content: string
    tags: string[]
  }
  disabled?: boolean
}

export default function ReviewCreateSubmitButton({ form, disabled = false }: Props) {
  const handleSubmit = async () => {
    if (!form.shopName) {
      toast('상호명은 필수 입력 사항입니다.')
      return
    }
    if (!form.content) {
      toast('내용은 필수 입력 사항입니다.')
      return
    }

    // TODO: 리뷰 생성 API 연동
    toast('리뷰 등록 기능은 준비 중입니다.')
  }

  return (
    <button
      className={cn(
        'w-full py-3 text-base leading-[16px]',
        disabled
          ? 'bg-gray-300 text-gray-500 cursor-not-allowed'
          : 'bg-main text-white cursor-pointer',
      )}
      onClick={handleSubmit}
      disabled={disabled}
    >
      등록하기
    </button>
  )
}
