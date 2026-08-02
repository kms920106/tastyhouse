'use client'

import { verifyMemberPassword } from '@/actions/member'
import AppFormField from '@/components/ui/AppFormField'
import AppInput from '@/components/ui/AppInput'
import AppSubmitButton from '@/components/ui/AppSubmitButton'
import { toast } from '@/components/ui/AppToaster'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import { extractZodFieldErrors } from '@/lib/form'
import { useState } from 'react'
import { z } from 'zod'

const verifySchema = z.object({
  password: z.string().min(1, '비밀번호를 입력해 주세요.'),
})

type FormData = z.infer<typeof verifySchema>

type FormErrors = Partial<Record<keyof FormData, string>>

const INITIAL_FORM_DATA: FormData = {
  password: '',
}

interface Props {
  onVerified: (verifyToken: string) => void
}

export default function AccountInfoVerifyForm({ onVerified }: Props) {
  const [formData, setFormData] = useState<FormData>(INITIAL_FORM_DATA)
  const [errors, setErrors] = useState<FormErrors>({})
  const [isSubmitting, setIsSubmitting] = useState(false)

  const handleChange = (field: keyof FormData, value: string) => {
    setFormData((prev) => ({ ...prev, [field]: value }))
    if (errors[field]) {
      setErrors((prev) => ({ ...prev, [field]: undefined }))
    }
  }

  const validateForm = (): boolean => {
    const result = verifySchema.safeParse(formData)

    if (result.success) {
      setErrors({})
      return true
    }

    setErrors(extractZodFieldErrors(result.error) as FormErrors)
    return false
  }

  const handleConfirm = async () => {
    if (!validateForm()) return

    setIsSubmitting(true)
    try {
      const { error, status, data } = await verifyMemberPassword({ password: formData.password })

      if (error && status === 400) {
        toast('비밀번호가 일치하지 않습니다.')
        return
      }

      if (error) {
        toast(COMMON_ERROR_MESSAGES.API_FETCH_ERROR)
        return
      }

      const verifyToken = data?.verifyToken
      if (!verifyToken) {
        toast(COMMON_ERROR_MESSAGES.MUTATION_ERROR)
        return
      }

      onVerified(verifyToken)
    } catch {
      toast(COMMON_ERROR_MESSAGES.MUTATION_ERROR)
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <>
      <div className="px-[15px] py-[30px]">
        <p className="text-sm leading-relaxed text-[#666666]">
          회원님의 정보를 안전하게 보호하기위해
          <br />
          비밀번호를 한번 더 확인해 주세요.
        </p>
        <div className="mt-[20px]">
          <AppFormField label="비밀번호" error={errors.password}>
            {({ className }) => (
              <AppInput
                type="password"
                value={formData.password}
                onChange={(e) => handleChange('password', e.target.value)}
                placeholder="비밀번호를 입력하세요."
                className={className}
                onKeyDown={(e) => e.key === 'Enter' && !isSubmitting && handleConfirm()}
              />
            )}
          </AppFormField>
          <div className="mt-[30px]">
            <AppSubmitButton
              onClick={handleConfirm}
              isSubmitting={isSubmitting}
              loadingText="확인 중"
            >
              확인
            </AppSubmitButton>
          </div>
        </div>
      </div>
    </>
  )
}
