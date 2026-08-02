'use client'

import { useState } from 'react'
import AccountPasswordChangeForm from './AccountPasswordChangeForm'
import AccountPasswordHeader from './AccountPasswordChangeHeader'
import AccountPasswordVerifyForm from './AccountPasswordVerifyForm'

export default function AccountPasswordPage() {
  const [verifyToken, setVerifyToken] = useState<string | null>(null)

  return (
    <>
      <AccountPasswordHeader />
      {verifyToken ? (
        <AccountPasswordChangeForm verifyToken={verifyToken} />
      ) : (
        <AccountPasswordVerifyForm onVerified={setVerifyToken} />
      )}
    </>
  )
}
