'use client'

import AppFormField from '@/components/ui/AppFormField'
import AppInput from '@/components/ui/AppInput'
import AppSubmitButton from '@/components/ui/AppSubmitButton'
import { toast } from '@/components/ui/AppToaster'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import { extractZodFieldErrors } from '@/lib/form'
import { updateMemberPassword } from '@/actions/member'
import { useRouter } from 'next/navigation'
import { useState } from 'react'
import { z } from 'zod'

const changeSchema = z
  .object({
    newPassword: z
      .string()
      .min(1, '새 비밀번호를 입력해 주세요.')
      .min(8, '비밀번호는 8자 이상 20자 이하로 입력해 주세요.')
      .max(20, '비밀번호는 8자 이상 20자 이하로 입력해 주세요.'),
    newPasswordConfirm: z.string().min(1, '새 비밀번호 확인을 입력해 주세요.'),
  })
  .refine((data) => data.newPassword === data.newPasswordConfirm, {
    message: '비밀번호가 일치하지 않습니다.',
    path: ['newPasswordConfirm'],
  })

type FormData = { newPassword: string; newPasswordConfirm: string }
type FormErrors = Partial<Record<keyof FormData, string>>

const INITIAL_FORM_DATA: FormData = {
  newPassword: '',
  newPasswordConfirm: '',
}

interface Props {
  verifyToken: string
}

export default function AccountPasswordChangeForm({ verifyToken }: Props) {
  const router = useRouter()

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
    const result = changeSchema.safeParse(formData)

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
      const { error, status } = await updateMemberPassword(
        { newPassword: formData.newPassword, newPasswordConfirm: formData.newPasswordConfirm },
        verifyToken,
      )

      if (error) {
        if (status === 400) {
          toast('현재 비밀번호와 동일한 비밀번호로 변경할 수 없습니다.')
        } else {
          toast(COMMON_ERROR_MESSAGES.MUTATION_ERROR)
        }
        return
      }

      toast('비밀번호가 변경되었습니다.')

      router.back()
    } catch {
      toast(COMMON_ERROR_MESSAGES.MUTATION_ERROR)
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div className="px-[15px] py-[30px] flex flex-col gap-5">
      <AppFormField label="새 비밀번호" error={errors.newPassword}>
        {({ className }) => (
          <AppInput
            type="password"
            value={formData.newPassword}
            onChange={(e) => handleChange('newPassword', e.target.value)}
            placeholder="새 비밀번호를 입력해 주세요."
            className={className}
          />
        )}
      </AppFormField>
      <AppFormField label="새 비밀번호 확인" error={errors.newPasswordConfirm}>
        {({ className }) => (
          <AppInput
            type="password"
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
