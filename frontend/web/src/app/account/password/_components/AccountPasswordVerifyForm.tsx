'use client'

import AppFormField from '@/components/ui/AppFormField'
import AppInput from '@/components/ui/AppInput'
import AppSubmitButton from '@/components/ui/AppSubmitButton'
import { toast } from '@/components/ui/AppToaster'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import { extractZodFieldErrors } from '@/lib/form'
import { verifyMemberPassword } from '@/actions/member'
import { useState } from 'react'
import { z } from 'zod'

const verifySchema = z.object({
  password: z.string().min(1, '현재 비밀번호를 입력해 주세요.'),
})

type FormData = z.infer<typeof verifySchema>
type FormErrors = Partial<Record<keyof FormData, string>>

const INITIAL_FORM_DATA: FormData = {
  password: '',
}

interface Props {
  onVerified: (verifyToken: string) => void
}

export default function AccountPasswordVerifyForm({ onVerified }: Props) {
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
      const response = await verifyMemberPassword({ password: formData.password })

      if (response?.error) {
        toast('비밀번호가 일치하지 않습니다.')
        return
      }

      const verifyToken = response?.data?.verifyToken
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
    <div className="px-[15px] py-[30px]">
      <AppFormField label="현재 비밀번호" error={errors.password}>
        {({ className }) => (
          <AppInput
            type="password"
            value={formData.password}
            onChange={(e) => handleChange('password', e.target.value)}
            placeholder="현재 비밀번호를 입력해 주세요."
            className={className}
            onKeyDown={(e) => e.key === 'Enter' && !isSubmitting && handleConfirm()}
          />
        )}
      </AppFormField>
      <div className="mt-[30px]">
        <AppSubmitButton onClick={handleConfirm} isSubmitting={isSubmitting} loadingText="확인 중">
          확인
        </AppSubmitButton>
      </div>
    </div>
  )
}
