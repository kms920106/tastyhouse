'use client'

import Header, { HeaderCenter, HeaderLeft, HeaderTitle } from '@/components/layouts/Header'
import { BackButton } from '@/components/layouts/header-parts'

interface Props {
  step: 'phone' | 'signup'
}

export default function AuthSignupSocialHeader({ step }: Props) {
  return (
    <Header variant="white" height={55}>
      <HeaderLeft>
        <BackButton />
      </HeaderLeft>
      <HeaderCenter>
        <HeaderTitle>{step === 'signup' ? '회원가입' : '본인 인증'}</HeaderTitle>
      </HeaderCenter>
    </Header>
  )
}
