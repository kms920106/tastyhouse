'use client'

import { useSidebar } from '@/components/ui/shadcn/sidebar'
import { RxHamburgerMenu } from 'react-icons/rx'
import HeaderIconButton from './HeaderIconButton'

interface Props {
  onClick?: () => void
}

export default function MenuButton({ onClick }: Props) {
  const { setOpenMobile } = useSidebar()

  const handleClick = () => {
    onClick?.()
    setOpenMobile(true)
  }

  return (
    <HeaderIconButton onClick={handleClick}>
      <RxHamburgerMenu size={22} className="white" />
    </HeaderIconButton>
  )
}
