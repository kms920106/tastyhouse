'use client'

import AppFormField from '@/components/ui/AppFormField'
import AppInput from '@/components/ui/AppInput'
import AppInputPhone from '@/components/ui/AppInputPhone'
import AppInputText from '@/components/ui/AppInputText'
import AppSubmitButton from '@/components/ui/AppSubmitButton'
import { toast } from '@/components/ui/AppToaster'
import CircleCheckbox from '@/components/ui/CircleCheckbox'
import { PHONE_ERROR_MESSAGES, PHONE_REGEX } from '@/constants/validation'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import { getTodayString } from '@/lib/date'
import { extractZodFieldErrors } from '@/lib/form'
import { cn } from '@/lib/utils'
import { createPartnershipRequest } from '@/actions/partnership'
import { useState } from 'react'
import type { Address } from 'react-daum-postcode'
import { z } from 'zod'
import PostcodeModal from './PostcodeModal'

const CONSULTATION_HOURS = ['09', '10', '11', '14', '15', '16', '17'] as const

const advertisingSchema = z.object({
  businessName: z.string().min(1, '상호명을 입력해 주세요.'),
  address: z.string().min(1, '주소를 검색해 주세요.'),
  addressDetail: z.string().min(1, '상세주소를 입력해 주세요.'),
  contactName: z.string().min(1, '성함을 입력해 주세요.'),
  contactPhone: z
    .string()
    .min(1, PHONE_ERROR_MESSAGES.REQUIRED)
    .regex(PHONE_REGEX, PHONE_ERROR_MESSAGES.INVALID),
  consultationDate: z.string().min(1, '상담 날짜를 선택해 주세요.'),
  consultationHour: z.string().min(1, '상담 시간을 선택해 주세요.'),
})

interface FormData extends z.infer<typeof advertisingSchema> {
  agreeToTerms: boolean
}

type FormErrors = Partial<Record<keyof z.infer<typeof advertisingSchema>, string>>

function buildConsultationDateTime(date: string, hour: string): string {
  return `${date}T${hour}:00:00`
}

const INITIAL_FORM_DATA: FormData = {
  businessName: '',
  address: '',
  addressDetail: '',
  contactName: '',
  contactPhone: '',
  consultationDate: '',
  consultationHour: '',
  agreeToTerms: false,
}

export default function AdvertisingForm() {
  const [formData, setFormData] = useState<FormData>(INITIAL_FORM_DATA)
  const [errors, setErrors] = useState<FormErrors>({})
  const [isPostcodeOpen, setIsPostcodeOpen] = useState(false)
  const [isSubmitting, setIsSubmitting] = useState(false)

  const handleChange = (field: keyof FormData, value: string | boolean) => {
    setFormData((prev) => ({ ...prev, [field]: value }))
    if (errors[field as keyof FormErrors]) {
      setErrors((prev) => ({ ...prev, [field]: undefined }))
    }
  }

  const handlePhoneChange = (value: string) => {
    if (value.length <= 11) handleChange('contactPhone', value)
  }

  const handlePostcodeComplete = (data: Address) => {
    setFormData((prev) => ({
      ...prev,
      address: data.roadAddress || data.jibunAddress,
      addressDetail: '',
    }))
    setErrors((prev) => ({ ...prev, address: undefined }))
  }

  const validateForm = (): boolean => {
    const trimmedData = {
      businessName: formData.businessName.trim(),
      address: formData.address.trim(),
      addressDetail: formData.addressDetail.trim(),
      contactName: formData.contactName.trim(),
      contactPhone: formData.contactPhone.trim(),
      consultationDate: formData.consultationDate,
      consultationHour: formData.consultationHour,
    }
    const result = advertisingSchema.safeParse(trimmedData)

    if (result.success) {
      setErrors({})
      return true
    }

    setErrors(extractZodFieldErrors(result.error) as FormErrors)
    return false
  }

  const handleSubmit = async () => {
    if (!validateForm()) return

    if (!formData.agreeToTerms) {
      toast('개인정보 수집 및 이용에 동의해 주세요.')
      return
    }

    setIsSubmitting(true)
    try {
      const { data, error } = await createPartnershipRequest({
        businessName: formData.businessName,
        address: formData.address,
        addressDetail: formData.addressDetail,
        contactName: formData.contactName,
        contactPhone: formData.contactPhone,
        consultationRequestedAt: buildConsultationDateTime(
          formData.consultationDate,
          formData.consultationHour,
        ),
      })

      if (error || !data) {
        toast(error || COMMON_ERROR_MESSAGES.MUTATION_ERROR)
        return
      }

      toast('광고 및 제휴 신청이 완료되었습니다.')
      setFormData(INITIAL_FORM_DATA)
      setErrors({})
    } finally {
      setIsSubmitting(false)
    }
  }

  const today = getTodayString()

  return (
    <>
      {isPostcodeOpen && (
        <PostcodeModal
          onComplete={handlePostcodeComplete}
          onClose={() => setIsPostcodeOpen(false)}
        />
      )}
      <div className="px-[15px] pt-[30px] pb-[15px] flex flex-col gap-5">
        <AppFormField label="상호명" required error={errors.businessName}>
          {({ className }) => (
            <AppInputText
              value={formData.businessName}
              onChange={(e) => handleChange('businessName', e.target.value)}
              placeholder="상호명을 입력해 주세요."
              className={className}
            />
          )}
        </AppFormField>
        <AppFormField label="위치정보" required error={errors.address ?? errors.addressDetail}>
          {({ className }) => (
            <div className="flex flex-col gap-2">
              <div className="flex gap-2">
                <AppInput
                  type="text"
                  value={formData.address}
                  readOnly
                  placeholder="도로명 주소를 검색해 주세요."
                  className={cn('flex-1 pr-4 bg-white', className)}
                />
                <button
                  type="button"
                  onClick={() => setIsPostcodeOpen(true)}
                  className="shrink-0 h-[50px] px-4 border border-line box-border text-[13px] whitespace-nowrap bg-[#f8f8f8] active:bg-line"
                >
                  우편번호 검색
                </button>
              </div>
              <AppInputText
                value={formData.addressDetail}
                onChange={(e) => handleChange('addressDetail', e.target.value)}
                placeholder="상세주소를 입력해 주세요."
                className={
                  errors.addressDetail
                    ? 'border-[#bc4040] focus-visible:border-[#bc4040]'
                    : undefined
                }
              />
            </div>
          )}
        </AppFormField>
        <AppFormField label="성명" required error={errors.contactName}>
          {({ className }) => (
            <AppInputText
              value={formData.contactName}
              onChange={(e) => handleChange('contactName', e.target.value)}
              placeholder="성함을 입력해 주세요."
              className={className}
            />
          )}
        </AppFormField>
        <AppFormField label="휴대폰 번호" required error={errors.contactPhone}>
          {({ className }) => (
            <AppInputPhone
              value={formData.contactPhone}
              onChange={(e) => handlePhoneChange(e.target.value)}
              placeholder="숫자만 입력해 주세요."
              maxLength={11}
              className={cn('pr-4', className)}
            />
          )}
        </AppFormField>
        <AppFormField
          label="상담신청시간"
          required
          error={errors.consultationDate ?? errors.consultationHour}
        >
          {({ className }) => (
            <div className="flex gap-2">
              <div className="relative flex-1">
                <input
                  type="date"
                  value={formData.consultationDate}
                  min={today}
                  onChange={(e) => handleChange('consultationDate', e.target.value)}
                  className={cn(
                    'w-full h-[50px] pl-[16px] pr-[16px] text-sm leading-[14px] border border-line box-border bg-white focus:outline-none focus:border-[#666666]',
                    !formData.consultationDate && 'text-[#999999]',
                    errors.consultationDate && 'border-[#bc4040] focus:border-[#bc4040]',
                    className,
                  )}
                />
              </div>
              <div className="relative flex-1">
                <select
                  value={formData.consultationHour}
                  onChange={(e) => handleChange('consultationHour', e.target.value)}
                  className={cn(
                    'w-full h-[50px] pl-[16px] pr-[40px] text-sm leading-[14px] border border-line box-border appearance-none bg-white focus:outline-none focus:border-[#666666]',
                    !formData.consultationHour && 'text-[#999999]',
                    errors.consultationHour && 'border-[#bc4040] focus:border-[#bc4040]',
                    className,
                  )}
                >
                  <option value="">시간 선택</option>
                  {CONSULTATION_HOURS.map((hour) => (
                    <option key={hour} value={hour}>
                      {hour}:00 - {String(Number(hour) + 1).padStart(2, '0')}:00
                    </option>
                  ))}
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
            </div>
          )}
        </AppFormField>
        <label className="flex items-center gap-2.5 mt-[5px] cursor-pointer">
          <CircleCheckbox
            checked={formData.agreeToTerms}
            onChange={(value) => handleChange('agreeToTerms', value)}
          />
          <span className="text-sm leading-[14px], text-[#666666]">
            개인정보 수집 및 이용에 동의합니다. (필수)
          </span>
        </label>
      </div>
      <div className="p-[15px]">
        <AppSubmitButton onClick={handleSubmit} isSubmitting={isSubmitting} loadingText="신청 중">
          확인
        </AppSubmitButton>
      </div>
    </>
  )
}
