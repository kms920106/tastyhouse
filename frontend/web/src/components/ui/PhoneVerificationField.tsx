'use client'

import AppFormField from '@/components/ui/AppFormField'
import AppInputPhone from '@/components/ui/AppInputPhone'
import AppOutlineButton from '@/components/ui/AppOutlineButton'
import { PHONE_ERROR_MESSAGES, PHONE_REGEX } from '@/constants/validation'
import type { usePhoneVerification } from '@/hooks/usePhoneVerification'
import { cn } from '@/lib/utils'

interface Props {
  verification: ReturnType<typeof usePhoneVerification>
  error?: string
  phoneInputName?: string
  originalPhone?: string
  onPhoneChange?: (phone: string) => void
  onClearError?: () => void
  onInvalidPhone?: (message: string) => void
}

export default function PhoneVerificationField({
  verification,
  error,
  phoneInputName,
  originalPhone,
  onPhoneChange,
  onClearError,
  onInvalidPhone,
}: Props) {
  const {
    phone,
    setPhone,
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
    <AppFormField label="휴대폰 번호" required error={error}>
      {({ className }) => (
        <div className="flex flex-col gap-2">
          <div className="flex gap-2">
            <AppInputPhone
              name={phoneInputName}
              value={phone}
              onChange={(e) => {
                if (e.target.value.length <= 11) {
                  setPhone(e.target.value)
                  onPhoneChange?.(e.target.value)
                }
                if (error) onClearError?.()
              }}
              readOnly={isVerified}
              placeholder="숫자만 입력해 주세요."
              maxLength={11}
              className={cn('flex-1 pr-4', className)}
            />
            <AppOutlineButton
              type="button"
              onClick={() => {
                const rawPhone = phone.replace(/-/g, '')
                if (!PHONE_REGEX.test(rawPhone)) {
                  onInvalidPhone?.(PHONE_ERROR_MESSAGES.INVALID)
                  return
                }
                if (originalPhone && rawPhone === originalPhone.replace(/-/g, '')) {
                  onInvalidPhone?.('현재 등록된 번호와 동일합니다. 다른 번호를 입력해 주세요.')
                  return
                }
                handleSendCode()
              }}
              disabled={!phone.trim() || isVerified || isSending}
              className="shrink-0"
            >
              {isSending ? '발송 중' : isCodeVisible ? '재발송' : '인증번호 받기'}
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
                placeholder="숫자만 입력해 주세요."
                maxLength={6}
                className="flex-1 pr-4"
              />
              <AppOutlineButton
                type="button"
                className="shrink-0"
                onClick={handleConfirmCode}
                disabled={isVerified || isConfirming}
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
