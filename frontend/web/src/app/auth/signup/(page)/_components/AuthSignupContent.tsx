'use client'

import AppTermsDialog from '@/components/ui/AppTermsDialog'
import { useState } from 'react'
import AuthSignupForm from './AuthSignupForm'

export default function AuthSignupContent() {
  const [termsDialog, setTermsDialog] = useState<{
    open: boolean
    title: string
    htmlContent: string
  }>({ open: false, title: '', htmlContent: '' })

  return (
    <>
      <AuthSignupForm onOpenTermsDialog={setTermsDialog} />
      <AppTermsDialog
        open={termsDialog.open}
        onOpenChange={(open) => setTermsDialog((prev) => ({ ...prev, open }))}
        title={termsDialog.title}
      >
        <div dangerouslySetInnerHTML={{ __html: termsDialog.htmlContent }} />
      </AppTermsDialog>
    </>
  )
}
