'use client'

import { confirmPasswordReset } from '@/actions/auth'
import AppFormField from '@/components/ui/AppFormField'
import AppInputPassword from '@/components/ui/AppInputPassword'
import AppSubmitButton from '@/components/ui/AppSubmitButton'
import { toast } from '@/components/ui/AppToaster'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import { extractZodFieldErrors } from '@/lib/form'
import { PAGE_PATHS } from '@/lib/paths'
import { useRouter } from 'next/navigation'
import { useState } from 'react'
import { z } from 'zod'

const resetSchema = z
  .object({
    newPassword: z.string().superRefine((val, ctx) => {
      if (val.length === 0) {
        ctx.addIssue({ code: 'custom', message: '새 비밀번호를 입력해 주세요.' })
        return
      }
      if (!/^(?=.*[a-zA-Z])(?=.*\d)(?=.*[!@#$%^&*()_+\-=[\]{};':"\\|,.<>/?]).{8,20}$/.test(val)) {
        ctx.addIssue({
          code: 'custom',
          message: '비밀번호는 영문, 숫자, 특수문자를 포함하여 8~20자로 입력해주세요.',
        })
      }
    }),
    newPasswordConfirm: z.string().min(1, '새 비밀번호 확인을 입력해 주세요.'),
  })
  .refine((data) => data.newPassword === data.newPasswordConfirm, {
    message: '비밀번호가 일치하지 않습니다.',
    path: ['newPasswordConfirm'],
  })

type FormData = { newPassword: string; newPasswordConfirm: string }
type FormErrors = Partial<Record<keyof FormData, string>>

interface Props {
  passwordResetToken: string
}

export default function ForgotPasswordResetStep({ passwordResetToken }: Props) {
  const router = useRouter()

  const [formData, setFormData] = useState<FormData>({ newPassword: '', newPasswordConfirm: '' })
  const [errors, setErrors] = useState<FormErrors>({})
  const [isSubmitting, setIsSubmitting] = useState(false)

  const handleChange = (field: keyof FormData, value: string) => {
    setFormData((prev) => ({ ...prev, [field]: value }))
    if (errors[field]) setErrors((prev) => ({ ...prev, [field]: undefined }))
  }

  const validateForm = (): boolean => {
    const result = resetSchema.safeParse(formData)
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
      const { error } = await confirmPasswordReset(
        passwordResetToken,
        formData.newPassword,
        formData.newPasswordConfirm,
      )

      if (error) {
        toast(error || COMMON_ERROR_MESSAGES.MUTATION_ERROR)
        return
      }

      toast('비밀번호가 변경되었습니다.')
      router.replace(PAGE_PATHS.AUTH_LOGIN)
    } catch {
      toast(COMMON_ERROR_MESSAGES.MUTATION_ERROR)
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div className="px-[15px] py-[30px] flex flex-col gap-5">
      <AppFormField label="새 비밀번호" required error={errors.newPassword}>
        {({ className }) => (
          <AppInputPassword
            value={formData.newPassword}
            onChange={(e) => handleChange('newPassword', e.target.value)}
            placeholder="새 비밀번호를 입력해 주세요."
            className={className}
          />
        )}
      </AppFormField>
      <AppFormField label="새 비밀번호 확인" required error={errors.newPasswordConfirm}>
        {({ className }) => (
          <AppInputPassword
            value={formData.newPasswordConfirm}
            onChange={(e) => handleChange('newPasswordConfirm', e.target.value)}
            placeholder="새 비밀번호를 확인해 주세요."
            className={className}
            onKeyDown={(e) => e.key === 'Enter' && !isSubmitting && handleConfirm()}
          />
        )}
      </AppFormField>
      <div className="mt-[10px]">
        <AppSubmitButton onClick={handleConfirm} isSubmitting={isSubmitting} loadingText="변경 중">
          확인
        </AppSubmitButton>
      </div>
    </div>
  )
}
