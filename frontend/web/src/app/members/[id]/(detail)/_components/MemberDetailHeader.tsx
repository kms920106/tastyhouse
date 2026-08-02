'use client'

import Header, { HeaderLeft, HeaderRight } from '@/components/layouts/Header'
import { HeaderIconButton } from '@/components/layouts/header-parts'
import { useRouter } from 'next/navigation'
import { IoChevronBack } from 'react-icons/io5'
import FollowButton from './FollowButton'

interface Props {
  memberId: number
  isLoggedIn: boolean
}

export default function MemberDetailHeader({ memberId, isLoggedIn }: Props) {
  const router = useRouter()

  return (
    <div className="flex-1 bg-main">
      <Header height={55} showBorder={false}>
        <HeaderLeft>
          <HeaderIconButton onClick={() => router.back()}>
            <IoChevronBack size={24} className="text-white" />
          </HeaderIconButton>
        </HeaderLeft>
        <HeaderRight>
          <FollowButton memberId={memberId} isLoggedIn={isLoggedIn} />
        </HeaderRight>
      </Header>
    </div>
  )
}
