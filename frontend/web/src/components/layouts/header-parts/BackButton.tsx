'use client'

import Image from 'next/image'
import { useRouter } from 'next/navigation'
import HeaderIconButton from './HeaderIconButton'

interface Props {
  onClick?: () => void
}

export default function BackButton({ onClick }: Props) {
  const router = useRouter()

  const handleClick = () => {
    if (onClick) {
      onClick()
    } else {
      router.back()
    }
  }

  return (
    <HeaderIconButton onClick={handleClick}>
      <Image src="/images/layout/nav-left-black.png" alt="뒤로가기" width={9} height={16} />
    </HeaderIconButton>
  )
}
