'use client'

import { useState } from 'react'
import AccountInfoEditForm from './AccountInfoEditForm'
import AccountInfoHeader from './AccountInfoHeader'
import AccountInfoVerifyForm from './AccountInfoVerifyForm'

export default function AccountInfoPage() {
  const [verifyToken, setVerifyToken] = useState<string | null>(null)

  return (
    <>
      <AccountInfoHeader />
      {verifyToken ? (
        <AccountInfoEditForm verifyToken={verifyToken} />
      ) : (
        <AccountInfoVerifyForm onVerified={setVerifyToken} />
      )}
    </>
  )
}
