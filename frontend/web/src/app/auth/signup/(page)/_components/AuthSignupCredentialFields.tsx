'use client'

import AppFormField from '@/components/ui/AppFormField'
import AppInputPassword from '@/components/ui/AppInputPassword'
import EmailVerificationField from '@/components/ui/EmailVerificationField'
import type { UseEmailVerificationReturn } from '@/hooks/useEmailVerification'

interface CredentialErrors {
  email?: string
  password?: string
  passwordConfirm?: string
}

interface Props {
  emailVerification: UseEmailVerificationReturn
  password: string
  passwordConfirm: string
  errors: CredentialErrors
  onPasswordChange: (value: string) => void
  onPasswordConfirmChange: (value: string) => void
  onClearEmailError: () => void
  onClearPasswordError: () => void
  onClearPasswordConfirmError: () => void
}

export default function AuthSignupCredentialFields({
  emailVerification,
  password,
  passwordConfirm,
  errors,
  onPasswordChange,
  onPasswordConfirmChange,
  onClearEmailError,
  onClearPasswordError,
  onClearPasswordConfirmError,
}: Props) {
  return (
    <>
      <input type="hidden" name="emailVerifyToken" value={emailVerification.token} />

      <EmailVerificationField
        verification={emailVerification}
        label="아이디"
        error={errors.email}
        onClearError={onClearEmailError}
      />

      <AppFormField label="비밀번호" required>
        {() => (
          <div className="flex flex-col gap-2.5">
            <AppInputPassword
              id="password"
              name="password"
              placeholder="비밀번호를 입력해 주세요."
              value={password}
              onChange={(e) => {
                onPasswordChange(e.target.value)
                if (errors.password) onClearPasswordError()
              }}
              className={
                errors.password ? 'border-[#bc4040] focus-visible:border-[#bc4040]' : undefined
              }
            />
            {errors.password && (
              <p className="text-xs leading-[12px] text-[#bc4040]">{errors.password}</p>
            )}
            <AppInputPassword
              id="passwordConfirm"
              name="passwordConfirm"
              placeholder="비밀번호를 확인해 주세요."
              value={passwordConfirm}
              onChange={(e) => {
                onPasswordConfirmChange(e.target.value)
                if (errors.passwordConfirm) onClearPasswordConfirmError()
              }}
              className={
                errors.passwordConfirm
                  ? 'border-[#bc4040] focus-visible:border-[#bc4040]'
                  : undefined
              }
            />
            {errors.passwordConfirm && (
              <p className="text-xs leading-[12px] text-[#bc4040]">{errors.passwordConfirm}</p>
            )}
          </div>
        )}
      </AppFormField>
    </>
  )
}
