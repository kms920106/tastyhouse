'use client'

import AppFormField from '@/components/ui/AppFormField'
import AppInputEmail from '@/components/ui/AppInputEmail'
import AppInputPhone from '@/components/ui/AppInputPhone'
import AppOutlineButton from '@/components/ui/AppOutlineButton'
import type { UseEmailVerificationReturn } from '@/hooks/useEmailVerification'
import { cn } from '@/lib/utils'

interface Props {
  verification: UseEmailVerificationReturn
  label?: string
  error?: string
  onClearError?: () => void
}

export default function EmailVerificationField({
  verification,
  label = '아이디',
  error,
  onClearError,
}: Props) {
  const {
    email,
    setEmail,
    verifyCode,
    setVerifyCode,
    isCodeVisible,
    isVerified,
    isSending,
    isConfirming,
    handleSendCode,
    handleConfirmCode,
  } = verification

  return (
    <AppFormField label={label} required error={error}>
      {({ className }) => (
        <div className="flex flex-col gap-2">
          <div className="flex gap-2">
            <AppInputEmail
              name="email"
              placeholder="이메일을 입력해 주세요."
              value={email}
              onChange={(e) => {
                setEmail(e.target.value)
                if (error) onClearError?.()
              }}
              readOnly={isVerified}
              className={cn('flex-1', className)}
            />
            <AppOutlineButton
              type="button"
              onClick={handleSendCode}
              disabled={!email.trim() || isVerified || isSending}
              className="shrink-0"
            >
              {isSending ? '발송 중' : isCodeVisible ? '재발송' : '인증메일 받기'}
            </AppOutlineButton>
          </div>

          {isCodeVisible && (
            <div className="flex gap-2">
              <AppInputPhone
                value={verifyCode}
                onChange={(e) => {
                  if (e.target.value.length <= 6) setVerifyCode(e.target.value)
                }}
                readOnly={isVerified}
                placeholder="123456"
                maxLength={6}
                className="flex-1 pr-4"
              />
              <AppOutlineButton
                type="button"
                className="shrink-0"
                onClick={handleConfirmCode}
                disabled={!verifyCode.trim() || isVerified || isConfirming}
              >
                {isConfirming ? '확인 중' : '확인'}
              </AppOutlineButton>
            </div>
          )}

          {isVerified && (
            <p className="text-xs leading-[12px] text-[#999999]">인증이 완료되었습니다.</p>
          )}
        </div>
      )}
    </AppFormField>
  )
}
