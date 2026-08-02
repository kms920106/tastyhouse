'use client'

import { createBugReport } from '@/actions/bug-report'
import PhotoUploader from '@/components/ui/PhotoUploader'
import AppFormField from '@/components/ui/AppFormField'
import AppInputText from '@/components/ui/AppInputText'
import AppSubmitButton from '@/components/ui/AppSubmitButton'
import AppTextarea from '@/components/ui/AppTextarea'
import { toast } from '@/components/ui/AppToaster'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import { extractZodFieldErrors } from '@/lib/form'
import { cn } from '@/lib/utils'
import { useCallback, useState, useTransition } from 'react'
import { z } from 'zod'

const bugReportSchema = z.object({
  device: z.string().min(1, '단말기를 선택해 주세요.'),
  title: z.string().min(1, '제목을 입력해 주세요.'),
  content: z.string().min(1, '내용을 입력해 주세요.'),
})

type FormData = z.infer<typeof bugReportSchema>

type FormErrors = Partial<Record<keyof FormData, string>>

const INITIAL_FORM_DATA: FormData = {
  device: '',
  title: '',
  content: '',
}

const MAX_IMAGE_COUNT = 5

export default function BugReportsForm() {
  const [formData, setFormData] = useState<FormData>(INITIAL_FORM_DATA)
  const [errors, setErrors] = useState<FormErrors>({})
  const [isUploading, setIsUploading] = useState(false)
  const [uploadedFileIds, setUploadedFileIds] = useState<number[]>([])
  const [photoUploaderKey, setPhotoUploaderKey] = useState(0)
  const [isSubmitting, startSubmitting] = useTransition()

  const handleChange = (field: keyof FormData, value: string) => {
    setFormData((prev) => ({ ...prev, [field]: value }))
    if (errors[field]) {
      setErrors((prev) => ({ ...prev, [field]: undefined }))
    }
  }

  const handleUploadedFileIdsChange = useCallback((fileIds: number[]) => {
    setUploadedFileIds(fileIds)
  }, [])

  const handleUploadingChange = useCallback((uploading: boolean) => {
    setIsUploading(uploading)
  }, [])

  const validateForm = (): boolean => {
    const trimmedData = {
      ...formData,
      title: formData.title.trim(),
      content: formData.content.trim(),
    }
    const result = bugReportSchema.safeParse(trimmedData)

    if (result.success) {
      setErrors({})
      return true
    }

    setErrors(extractZodFieldErrors(result.error) as FormErrors)
    return false
  }

  const handleSubmit = () => {
    if (!validateForm()) return

    startSubmitting(async () => {
      try {
        const response = await createBugReport({
          device: formData.device,
          title: formData.title,
          content: formData.content,
          uploadedFileIds,
        })

        if (!response?.error) {
          toast('버그 제보가 접수되었습니다.')
          setFormData(INITIAL_FORM_DATA)
          setErrors({})
          setPhotoUploaderKey((prev) => prev + 1)
        } else {
          toast(COMMON_ERROR_MESSAGES.MUTATION_ERROR)
        }
      } catch (error) {
        console.error('버그 제보 실패:', error)
        toast(COMMON_ERROR_MESSAGES.MUTATION_ERROR)
      }
    })
  }

  return (
    <>
      <div className="flex flex-col gap-5 px-[15px] py-5">
        <AppFormField label="단말기" required error={errors.device}>
          {({ className }) => (
            <div className="relative">
              <select
                value={formData.device}
                onChange={(e) => handleChange('device', e.target.value)}
                className={cn(
                  'w-full h-[50px] pl-[16px] pr-[40px] text-sm leading-[14px] border box-border appearance-none bg-white focus:outline-none',
                  !formData.device && 'text-[#999999]',
                  errors.device
                    ? 'border-[#bc4040] focus:border-[#bc4040]'
                    : 'border-line focus:border-[#666666]',
                  className,
                )}
              >
                <option value="">선택</option>
                <option value="android">Android</option>
                <option value="ios">iOS</option>
                <option value="web">Web</option>
              </select>
              <svg
                className="absolute right-4 top-1/2 -translate-y-1/2 pointer-events-none text-[#999999]"
                width="20"
                height="20"
                viewBox="0 0 20 20"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
              >
                <path d="M5 7.5l5 5 5-5" />
              </svg>
            </div>
          )}
        </AppFormField>
        <AppFormField label="제목" required error={errors.title}>
          {({ className }) => (
            <AppInputText
              value={formData.title}
              onChange={(e) => handleChange('title', e.target.value)}
              placeholder="제목을 입력해 주세요."
              className={cn(
                errors.title && 'border-[#bc4040] focus-visible:border-[#bc4040]',
                className,
              )}
            />
          )}
        </AppFormField>
        <AppFormField label="내용" required error={errors.content}>
          {({ className }) => (
            <AppTextarea
              value={formData.content}
              onChange={(e) => handleChange('content', e.target.value)}
              placeholder="내용을 입력해 주세요."
              rows={16}
              error={!!errors.content}
              className={className}
            />
          )}
        </AppFormField>
        <AppFormField label="사진">
          {() => (
            <PhotoUploader
              key={photoUploaderKey}
              maxCount={MAX_IMAGE_COUNT}
              onUploadedFileIdsChange={handleUploadedFileIdsChange}
              onUploadingChange={handleUploadingChange}
            />
          )}
        </AppFormField>
        <p className="text-sm leading-[14px] text-[#999999]">
          아직 제보되지 않은 버그인 경우 1,000 포인트를 지급해드립니다.
        </p>
      </div>
      <div className="p-[15px]">
        <AppSubmitButton
          onClick={handleSubmit}
          disabled={isUploading}
          isSubmitting={isSubmitting}
          loadingText="제출 중"
        >
          확인
        </AppSubmitButton>
      </div>
    </>
  )
}
