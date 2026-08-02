'use client'

import { useState } from 'react'
import AuthSignupSocialContent from './AuthSignupSocialContent'
import AuthSignupSocialHeader from './AuthSignupSocialHeader'

interface Props {
  providerParam: string | undefined
  tempTokenParam: string | undefined
}

export default function AuthSignupSocialPage({ providerParam, tempTokenParam }: Props) {
  const [step, setStep] = useState<'phone' | 'signup'>('phone')

  return (
    <>
      <AuthSignupSocialHeader step={step} />
      <AuthSignupSocialContent
        providerParam={providerParam}
        tempTokenParam={tempTokenParam}
        onStepChange={setStep}
      />
    </>
  )
}
