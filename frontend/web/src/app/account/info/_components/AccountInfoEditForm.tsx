'use client'

import { getMemberPersonalInfo, updateMemberPersonalInfo } from '@/actions/member'
import AppFormField from '@/components/ui/AppFormField'
import AppInput from '@/components/ui/AppInput'
import AppOutlineButton from '@/components/ui/AppOutlineButton'
import AppSelect from '@/components/ui/AppSelect'
import AppSubmitButton from '@/components/ui/AppSubmitButton'
import { toast } from '@/components/ui/AppToaster'
import BorderedSection from '@/components/ui/BorderedSection'
import PhoneVerificationField from '@/components/ui/PhoneVerificationField'
import SectionStack from '@/components/ui/SectionStack'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import { PHONE_ERROR_MESSAGES, PHONE_REGEX } from '@/constants/validation'
import type { MemberGender } from '@/domains/member'
import { usePhoneVerification } from '@/hooks/usePhoneVerification'
import { extractZodFieldErrors } from '@/lib/form'
import { PAGE_PATHS } from '@/lib/paths'
import { cn } from '@/lib/utils'
import Image from 'next/image'
import Link from 'next/link'
import { useRouter } from 'next/navigation'
import { useEffect, useState, useTransition } from 'react'
import { z } from 'zod'
import ToggleSwitch from './ToggleSwitch'

const BIRTH_YEARS = Array.from({ length: 100 }, (_, i) => 2026 - i)
const BIRTH_MONTHS = Array.from({ length: 12 }, (_, i) => i + 1)
const BIRTH_DAYS = Array.from({ length: 31 }, (_, i) => i + 1)

const accountInfoSchema = z.object({
  name: z.string().min(1, '이름을 입력해 주세요.'),
  phone: z
    .string()
    .min(1, PHONE_ERROR_MESSAGES.REQUIRED)
    .regex(PHONE_REGEX, PHONE_ERROR_MESSAGES.INVALID),
  birthYear: z.string().min(1, '생년월일을 선택해 주세요.'),
  birthMonth: z.string().min(1, '생년월일을 선택해 주세요.'),
  birthDay: z.string().min(1, '생년월일을 선택해 주세요.'),
  gender: z.enum(['MALE', 'FEMALE'], { message: '성별을 선택해 주세요.' }),
})

type FormErrors = Partial<Record<keyof z.infer<typeof accountInfoSchema>, string>>

function parseBirthDate(birthDate: number | null) {
  if (!birthDate) return { year: '', month: '', day: '' }
  const str = String(birthDate)
  return {
    year: str.slice(0, 4),
    month: String(parseInt(str.slice(4, 6), 10)),
    day: String(parseInt(str.slice(6, 8), 10)),
  }
}

interface Props {
  verifyToken: string
}

export default function AccountInfoEditForm({ verifyToken }: Props) {
  const router = useRouter()

  const [email, setEmail] = useState('')
  const [name, setName] = useState('')
  const [originalPhone, setOriginalPhone] = useState('')
  const [birthYear, setBirthYear] = useState('')
  const [birthMonth, setBirthMonth] = useState('')
  const [birthDay, setBirthDay] = useState('')
  const [gender, setGender] = useState<MemberGender>('FEMALE')
  const [pushNotification, setPushNotification] = useState(false)
  const [marketingNotification, setMarketingNotification] = useState(false)
  const [eventNotification, setEventNotification] = useState(false)
  const [errors, setErrors] = useState<FormErrors>({})
  const [isSubmitting, startSubmitting] = useTransition()

  const phoneVerification = usePhoneVerification({ originalPhone })

  useEffect(() => {
    getMemberPersonalInfo().then(({ data }) => {
      const info = data
      if (!info) return
      const { year, month, day } = parseBirthDate(info.birthDate)
      setEmail(info.email)
      setName(info.fullName)
      phoneVerification.setPhone(info.phoneNumber)
      setOriginalPhone(info.phoneNumber)
      setBirthYear(year)
      setBirthMonth(month)
      setBirthDay(day)
      if (info.gender) setGender(info.gender)
      setPushNotification(info.pushNotificationEnabled)
      setMarketingNotification(info.marketingInfoEnabled)
      setEventNotification(info.eventInfoEnabled)
    })
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  const buildBirthDate = (): number | undefined => {
    if (!birthYear || !birthMonth || !birthDay) return undefined
    const mm = String(birthMonth).padStart(2, '0')
    const dd = String(birthDay).padStart(2, '0')
    return Number(`${birthYear}${mm}${dd}`)
  }

  const validateForm = (): boolean => {
    const result = accountInfoSchema.safeParse({
      name: name.trim(),
      phone: phoneVerification.phone.replace(/-/g, ''),
      birthYear,
      birthMonth,
      birthDay,
      gender,
    })

    if (result.success) {
      setErrors({})
      return true
    }

    setErrors(extractZodFieldErrors(result.error) as FormErrors)
    return false
  }

  const handleSubmit = () => {
    if (!verifyToken) {
      toast(COMMON_ERROR_MESSAGES.MUTATION_ERROR)
      return
    }

    if (!validateForm()) return

    startSubmitting(async () => {
      try {
        const response = await updateMemberPersonalInfo(
          {
            fullName: name,
            phoneNumber: phoneVerification.isVerified
              ? phoneVerification.phone.replace(/-/g, '')
              : undefined,
            birthDate: buildBirthDate(),
            gender,
            pushNotificationEnabled: pushNotification,
            marketingInfoEnabled: marketingNotification,
            eventInfoEnabled: eventNotification,
          },
          verifyToken,
          phoneVerification.isVerified ? phoneVerification.phoneVerifyToken : undefined,
        )

        if (response?.error) {
          toast(response.error)
          return
        }

        toast('개인정보가 수정되었습니다.')

        router.back()
      } catch {
        toast(COMMON_ERROR_MESSAGES.MUTATION_ERROR)
      }
    })
  }

  return (
    <>
      <SectionStack>
        <BorderedSection>
          <div className="px-[15px] py-[30px] flex flex-col gap-5">
            {/* 아이디 */}
            <AppFormField label="아이디" required>
              {() => (
                <AppInput
                  type="email"
                  value={email}
                  readOnly
                  className="pr-4 bg-[#f8f8f8] text-[#aaaaaa]"
                />
              )}
            </AppFormField>

            {/* 이름 */}
            <AppFormField label="이름" required error={errors.name}>
              {({ className }) => (
                <AppInput
                  type="text"
                  value={name}
                  onChange={(e) => {
                    setName(e.target.value)
                    if (errors.name) setErrors((prev) => ({ ...prev, name: undefined }))
                  }}
                  placeholder="이름을 입력해 주세요."
                  className={cn('pr-4', className)}
                />
              )}
            </AppFormField>

            {/* 휴대폰 번호 */}
            <PhoneVerificationField
              verification={phoneVerification}
              error={errors.phone}
              originalPhone={originalPhone}
              onClearError={() => setErrors((prev) => ({ ...prev, phone: undefined }))}
              onInvalidPhone={(message) => setErrors((prev) => ({ ...prev, phone: message }))}
            />

            {/* 생년월일 */}
            <AppFormField label="생년월일" required error={errors.birthYear}>
              {() => (
                <div className="flex gap-2">
                  <AppSelect
                    value={birthYear}
                    onChange={(e) => {
                      setBirthYear(e.target.value)
                      if (errors.birthYear) setErrors((prev) => ({ ...prev, birthYear: undefined }))
                    }}
                    className="flex-1"
                  >
                    <option value="">년도</option>
                    {BIRTH_YEARS.map((year) => (
                      <option key={year} value={year}>
                        {year}
                      </option>
                    ))}
                  </AppSelect>
                  <AppSelect
                    value={birthMonth}
                    onChange={(e) => {
                      setBirthMonth(e.target.value)
                      if (errors.birthYear) setErrors((prev) => ({ ...prev, birthYear: undefined }))
                    }}
                    className="flex-1"
                  >
                    <option value="">월</option>
                    {BIRTH_MONTHS.map((month) => (
                      <option key={month} value={month}>
                        {month}
                      </option>
                    ))}
                  </AppSelect>
                  <AppSelect
                    value={birthDay}
                    onChange={(e) => {
                      setBirthDay(e.target.value)
                      if (errors.birthYear) setErrors((prev) => ({ ...prev, birthYear: undefined }))
                    }}
                    className="flex-1"
                  >
                    <option value="">일</option>
                    {BIRTH_DAYS.map((day) => (
                      <option key={day} value={day}>
                        {day}
                      </option>
                    ))}
                  </AppSelect>
                </div>
              )}
            </AppFormField>

            {/* 성별 */}
            <AppFormField label="성별" required error={errors.gender}>
              {() => (
                <div className="flex">
                  <AppOutlineButton
                    className={cn(
                      'flex-1 transition-colors',
                      gender === 'MALE' ? 'border-main text-main' : 'border-line',
                    )}
                    onClick={() => setGender('MALE')}
                  >
                    남성
                  </AppOutlineButton>
                  <AppOutlineButton
                    className={cn(
                      'flex-1 transition-colors',
                      gender === 'FEMALE' ? 'border-main text-main' : 'border-line',
                    )}
                    onClick={() => setGender('FEMALE')}
                  >
                    여성
                  </AppOutlineButton>
                </div>
              )}
            </AppFormField>
          </div>
        </BorderedSection>
        <BorderedSection>
          <div className="px-[15px] py-[20px]">
            <div className="flex flex-col gap-[15px]">
              <div className="flex items-center justify-between">
                <span className="text-sm leading-[14px] text-[#666666]">푸시 알림 동의</span>
                <ToggleSwitch checked={pushNotification} onChange={setPushNotification} />
              </div>
              <div className="flex items-center justify-between">
                <span className="text-sm leading-[14px] text-[#666666]">마케팅 정보 수신</span>
                <ToggleSwitch checked={marketingNotification} onChange={setMarketingNotification} />
              </div>
              <div className="flex items-center justify-between">
                <span className="text-sm leading-[14px] text-[#666666]">이벤트 정보 수신</span>
                <ToggleSwitch checked={eventNotification} onChange={setEventNotification} />
              </div>
            </div>
            <div className="flex items-center justify-end mt-10">
              <Link
                href={PAGE_PATHS.ACCOUNT_WITHDRAW}
                className="flex items-center gap-1 text-[13px] leading-[13px] text-[#999999]"
              >
                회원탈퇴
                <Image src="/images/layout/nav-right.png" alt="" width={6} height={11} />
              </Link>
            </div>
            <div className="mt-5">
              <AppSubmitButton
                onClick={handleSubmit}
                isSubmitting={isSubmitting}
                loadingText="수정 중"
              >
                수정하기
              </AppSubmitButton>
            </div>
          </div>
        </BorderedSection>
      </SectionStack>
    </>
  )
}
