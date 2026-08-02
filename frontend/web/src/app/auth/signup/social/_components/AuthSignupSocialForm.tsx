'use client'

import {
  confirmEmailVerificationCodeForEmailField,
  sendEmailVerificationCode,
} from '@/actions/email-verification'
import { checkNicknameAvailability } from '@/actions/member'
import {
  fetchAgeVerificationContent,
  fetchElectronicFinancialTransactionsContent,
  fetchPrivacyPolicyContent,
  fetchTermsOfServiceContent,
} from '@/actions/policies'
import AppTermsDialog from '@/components/ui/AppTermsDialog'
import AppFormField from '@/components/ui/AppFormField'
import AppInputText from '@/components/ui/AppInputText'
import AppOutlineButton from '@/components/ui/AppOutlineButton'
import AppSelect from '@/components/ui/AppSelect'
import AppSubmitButton from '@/components/ui/AppSubmitButton'
import { toast } from '@/components/ui/AppToaster'
import BorderedSection from '@/components/ui/BorderedSection'
import CircleCheckbox from '@/components/ui/CircleCheckbox'
import EmailVerificationField from '@/components/ui/EmailVerificationField'
import FormCheckbox from '@/components/ui/FormCheckbox'
import SectionStack from '@/components/ui/SectionStack'
import type { SocialProfile } from '@/domains/auth'
import type { MemberGender } from '@/domains/member'
import { useEmailVerification } from '@/hooks/useEmailVerification'
import { extractZodFieldErrors } from '@/lib/form'
import { PAGE_PATHS } from '@/lib/paths'
import { cn } from '@/lib/utils'
import Icon from '@/components/ui/Icon'
import { useRouter } from 'next/navigation'
import { useEffect, useState, useTransition } from 'react'
import { z } from 'zod'

const BIRTH_YEARS = Array.from({ length: 100 }, (_, i) => 2026 - i)
const BIRTH_MONTHS = Array.from({ length: 12 }, (_, i) => i + 1)
const BIRTH_DAYS = Array.from({ length: 31 }, (_, i) => i + 1)

const TERMS_LIST = [
  { key: 'agreedTerms', label: '이용약관 동의 (필수)', required: true, href: '/terms' },
  { key: 'agreedPrivacy', label: '개인정보처리방침 동의 (필수)', required: true, href: '/privacy' },
  {
    key: 'agreedFinance',
    label: '전자금융거래 이용약관 동의 (필수)',
    required: true,
    href: '/terms/finance',
  },
  {
    key: 'agreedAge',
    label: '만 14세 이상 이용 동의 (필수)',
    required: true,
    href: '/terms/age-verification',
  },
  { key: 'agreedMarketing', label: '마케팅 정보 수신 동의 (선택)', required: false, href: null },
  {
    key: 'agreedPushNotification',
    label: '푸시 알림 수신 동의 (선택)',
    required: false,
    href: null,
  },
] as const

type TermsKey = (typeof TERMS_LIST)[number]['key']

const signupSchema = z.object({
  nickname: z
    .string()
    .min(2, '닉네임은 2자 이상 입력해 주세요.')
    .max(20, '닉네임은 20자 이하로 입력해 주세요.'),
  fullName: z.string().min(1, '이름을 입력해 주세요.'),
  birthYear: z.string(),
  birthMonth: z.string(),
  birthDay: z.string(),
  gender: z.enum(['MALE', 'FEMALE'], { message: '성별을 선택해 주세요.' }),
})

type FormErrors = Partial<
  Record<keyof z.infer<typeof signupSchema> | 'birthDate' | 'email', string>
>

export interface SocialSignupFormData {
  username: string
  nickname: string
  fullName: string
  gender: MemberGender
  birthDate: number
  phoneNumber: string
  pushNotificationEnabled?: boolean
  marketingInfoEnabled?: boolean
  eventInfoEnabled?: boolean
  referrerNickname?: string
}

interface Props {
  socialProfile: SocialProfile | null
  phone: string
  onSignUp: (formData: SocialSignupFormData) => Promise<{ success: false; error: string } | null>
}

export default function AuthSignupSocialForm({ socialProfile, phone, onSignUp }: Props) {
  const router = useRouter()

  const emailVerification = useEmailVerification({
    sendFn: sendEmailVerificationCode,
    confirmFn: confirmEmailVerificationCodeForEmailField,
  })

  const [nickname, setNickname] = useState('')
  const [isNicknameChecked, setIsNicknameChecked] = useState(false)
  const [isCheckingNickname, startCheckingNickname] = useTransition()

  const [fullName, setFullName] = useState('')

  const [birthYear, setBirthYear] = useState('')
  const [birthMonth, setBirthMonth] = useState('')
  const [birthDay, setBirthDay] = useState('')
  const [gender, setGender] = useState<MemberGender | null>(null)

  const [referrerNickname, setReferrerNickname] = useState('')
  const [isReferrerVerified, setIsReferrerVerified] = useState(false)
  const [isCheckingReferrer, startCheckingReferrer] = useTransition()

  const [termsDialog, setTermsDialog] = useState<{
    open: boolean
    title: string
    htmlContent: string
  }>({ open: false, title: '', htmlContent: '' })

  const [agreedAll, setAgreedAll] = useState(false)
  const [agreedTerms, setAgreedTerms] = useState<Record<TermsKey, boolean>>({
    agreedTerms: false,
    agreedPrivacy: false,
    agreedFinance: false,
    agreedAge: false,
    agreedMarketing: false,
    agreedPushNotification: false,
  })

  const [errors, setErrors] = useState<FormErrors>({})
  const [isSubmitting, startSubmitTransition] = useTransition()

  useEffect(() => {
    if (socialProfile?.email) emailVerification.setEmail(socialProfile.email)
    if (socialProfile?.nickname) setNickname(socialProfile.nickname)
    if (socialProfile?.name) setFullName(socialProfile.name)
    if (socialProfile?.birthYear) setBirthYear(socialProfile.birthYear)
    if (socialProfile?.birthMonth) setBirthMonth(socialProfile.birthMonth)
    if (socialProfile?.birthDay) setBirthDay(socialProfile.birthDay)
    if (socialProfile?.gender === 'MALE' || socialProfile?.gender === 'FEMALE')
      setGender(socialProfile.gender)
  }, []) // eslint-disable-line react-hooks/exhaustive-deps

  useEffect(() => {
    const allChecked = TERMS_LIST.every(({ key }) => agreedTerms[key])
    setAgreedAll(allChecked)
  }, [agreedTerms])

  const handleAgreedAll = (checked: boolean) => {
    setAgreedAll(checked)
    const updated = {} as Record<TermsKey, boolean>
    TERMS_LIST.forEach(({ key }) => {
      updated[key] = checked
    })
    setAgreedTerms(updated)
  }

  const handleOpenTermsDialog = async (termKey: TermsKey, label: string) => {
    try {
      const fetchMap: Partial<Record<TermsKey, () => Promise<string>>> = {
        agreedTerms: fetchTermsOfServiceContent,
        agreedPrivacy: fetchPrivacyPolicyContent,
        agreedFinance: fetchElectronicFinancialTransactionsContent,
        agreedAge: fetchAgeVerificationContent,
      }
      const fetcher = fetchMap[termKey]
      if (!fetcher) return
      const htmlContent = await fetcher()
      setTermsDialog({ open: true, title: label, htmlContent })
    } catch {
      toast('약관 내용을 불러오는데 실패했습니다.')
    }
  }

  const handleCheckNickname = () => {
    if (!nickname.trim()) {
      setErrors((prev) => ({ ...prev, nickname: '닉네임을 입력해 주세요.' }))
      return
    }
    startCheckingNickname(async () => {
      try {
        const response = await checkNicknameAvailability(nickname)
        if (response?.error) {
          toast(response.error)
          return
        }
        if (response?.data?.available === false) {
          setErrors((prev) => ({ ...prev, nickname: '이미 사용 중인 닉네임입니다.' }))
          setIsNicknameChecked(false)
          return
        }
        setIsNicknameChecked(true)
        setErrors((prev) => ({ ...prev, nickname: undefined }))
      } catch {
        toast('닉네임 확인에 실패했습니다. 다시 시도해 주세요.')
      }
    })
  }

  const handleCheckReferrer = () => {
    if (!referrerNickname.trim()) return
    startCheckingReferrer(async () => {
      try {
        const response = await checkNicknameAvailability(referrerNickname)
        if (response?.error || response?.data?.available !== false) {
          toast('존재하지 않는 닉네임입니다.')
          setIsReferrerVerified(false)
          return
        }
        setIsReferrerVerified(true)
        toast('추천인이 확인되었습니다.')
      } catch {
        toast('추천인 확인에 실패했습니다. 다시 시도해 주세요.')
      }
    })
  }

  const validateForm = (): boolean => {
    const result = signupSchema.safeParse({
      nickname,
      fullName,
      birthYear,
      birthMonth,
      birthDay,
      gender: gender ?? '',
    })

    const fieldErrors: FormErrors = result.success
      ? {}
      : (extractZodFieldErrors(result.error) as FormErrors)

    if (!birthYear || !birthMonth || !birthDay) {
      fieldErrors.birthDate = '생년월일을 선택해 주세요.'
    }

    if (Object.values(fieldErrors).some((v) => v !== undefined)) {
      setErrors(fieldErrors)
      return false
    }

    setErrors({})
    return true
  }

  const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault()
    if (!validateForm()) return
    if (!emailVerification.isVerified) {
      toast('이메일 인증을 완료해 주세요.')
      return
    }
    if (!isNicknameChecked) {
      toast('닉네임 중복확인을 해 주세요.')
      return
    }
    if (TERMS_LIST.filter((t) => t.required).some(({ key }) => !agreedTerms[key])) {
      toast('필수 약관에 동의해 주세요.')
      return
    }

    const birthDate = Number(
      `${birthYear}${birthMonth.padStart(2, '0')}${birthDay.padStart(2, '0')}`,
    )

    startSubmitTransition(async () => {
      const result = await onSignUp({
        username: emailVerification.email,
        nickname,
        fullName,
        gender: gender!,
        birthDate,
        phoneNumber: phone || socialProfile?.phoneNumber || '',
        pushNotificationEnabled: agreedTerms.agreedPushNotification,
        marketingInfoEnabled: agreedTerms.agreedMarketing,
        eventInfoEnabled: agreedTerms.agreedMarketing,
        referrerNickname: isReferrerVerified ? referrerNickname : undefined,
      })

      if (result?.success === false) {
        toast(result.error)
        return
      }

      router.replace(PAGE_PATHS.AUTH_SIGNUP_COMPLETE)
    })
  }

  return (
    <form onSubmit={handleSubmit}>
      <SectionStack>
        <BorderedSection>
          <div className="px-[15px] py-[30px] flex flex-col gap-5">
            {/* 이메일 */}
            <EmailVerificationField
              verification={emailVerification}
              label="이메일"
              error={errors.email}
              onClearError={() => setErrors((prev) => ({ ...prev, email: undefined }))}
            />

            {/* 닉네임 */}
            <AppFormField label="닉네임" required error={errors.nickname}>
              {({ className }) => (
                <div className="flex flex-col gap-2">
                  <div className="flex gap-2">
                    <AppInputText
                      id="nickname"
                      name="nickname"
                      placeholder="닉네임을 입력해 주세요."
                      value={nickname}
                      onChange={(e) => {
                        setNickname(e.target.value)
                        setIsNicknameChecked(false)
                        if (errors.nickname) setErrors((prev) => ({ ...prev, nickname: undefined }))
                      }}
                      className={cn('flex-1', className)}
                    />
                    <AppOutlineButton
                      type="button"
                      onClick={handleCheckNickname}
                      disabled={!nickname.trim() || isCheckingNickname || isNicknameChecked}
                      className="shrink-0"
                    >
                      {isCheckingNickname ? '확인 중' : '중복확인'}
                    </AppOutlineButton>
                  </div>
                  {isNicknameChecked && (
                    <p className="text-xs leading-[12px] text-[#999999]">
                      사용 가능한 닉네임입니다.
                    </p>
                  )}
                </div>
              )}
            </AppFormField>

            {/* 이름 */}
            <AppFormField label="이름" required error={errors.fullName}>
              {({ className }) => (
                <AppInputText
                  id="fullName"
                  name="fullName"
                  placeholder="이름을 입력해 주세요."
                  value={fullName}
                  onChange={(e) => {
                    setFullName(e.target.value)
                    if (errors.fullName) setErrors((prev) => ({ ...prev, fullName: undefined }))
                  }}
                  className={className}
                />
              )}
            </AppFormField>

            {/* 생년월일 */}
            <AppFormField label="생년월일" required error={errors.birthDate}>
              {() => (
                <div className="flex gap-2">
                  <AppSelect
                    name="birthYear"
                    value={birthYear}
                    onChange={(e) => {
                      setBirthYear(e.target.value)
                      if (errors.birthDate) setErrors((prev) => ({ ...prev, birthDate: undefined }))
                    }}
                    className={cn('flex-1', errors.birthDate && !birthYear && 'border-[#bc4040]')}
                  >
                    <option value="">년도</option>
                    {BIRTH_YEARS.map((year) => (
                      <option key={year} value={year}>
                        {year}
                      </option>
                    ))}
                  </AppSelect>
                  <AppSelect
                    name="birthMonth"
                    value={birthMonth}
                    onChange={(e) => {
                      setBirthMonth(e.target.value)
                      if (errors.birthDate) setErrors((prev) => ({ ...prev, birthDate: undefined }))
                    }}
                    className={cn('flex-1', errors.birthDate && !birthMonth && 'border-[#bc4040]')}
                  >
                    <option value="">월</option>
                    {BIRTH_MONTHS.map((month) => (
                      <option key={month} value={month}>
                        {month}
                      </option>
                    ))}
                  </AppSelect>
                  <AppSelect
                    name="birthDay"
                    value={birthDay}
                    onChange={(e) => {
                      setBirthDay(e.target.value)
                      if (errors.birthDate) setErrors((prev) => ({ ...prev, birthDate: undefined }))
                    }}
                    className={cn('flex-1', errors.birthDate && !birthDay && 'border-[#bc4040]')}
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
                  {(['MALE', 'FEMALE'] as const).map((value, index) => (
                    <label
                      key={value}
                      className={cn(
                        'flex-1 flex items-center justify-center h-[45px] text-sm leading-[14px] text-[#aaaaaa] border cursor-pointer transition-colors',
                        index > 0 && '-ml-px',
                        gender === value
                          ? 'border-main text-main z-10'
                          : errors.gender
                            ? 'border-[#bc4040]'
                            : 'border-line',
                      )}
                    >
                      <input
                        type="radio"
                        name="gender"
                        value={value}
                        checked={gender === value}
                        onChange={() => {
                          setGender(value)
                          if (errors.gender) setErrors((prev) => ({ ...prev, gender: undefined }))
                        }}
                        className="sr-only"
                      />
                      {value === 'MALE' ? '남성' : '여성'}
                    </label>
                  ))}
                </div>
              )}
            </AppFormField>

            {/* 추천인 닉네임 */}
            <AppFormField label="추천인 닉네임">
              {() => (
                <div className="flex gap-2">
                  <AppInputText
                    id="referrerNickname"
                    name="referrerNickname"
                    placeholder="추천인 닉네임을 입력해 주세요."
                    value={referrerNickname}
                    onChange={(e) => {
                      setReferrerNickname(e.target.value)
                      setIsReferrerVerified(false)
                    }}
                    className="flex-1"
                  />
                  <AppOutlineButton
                    type="button"
                    onClick={handleCheckReferrer}
                    disabled={!referrerNickname.trim() || isCheckingReferrer}
                    className="shrink-0"
                  >
                    {isCheckingReferrer ? '확인 중' : '추천인 확인'}
                  </AppOutlineButton>
                </div>
              )}
            </AppFormField>
          </div>
        </BorderedSection>

        {/* 약관 동의 */}
        <BorderedSection>
          <div className="flex flex-col px-[15px] pt-[18px]">
            <label className="flex items-center gap-2.5 pb-2.5 border-b border-line cursor-pointer">
              <CircleCheckbox checked={agreedAll} onChange={handleAgreedAll} />
              <span className="text-sm leading-[14px]">약관에 모두 동의합니다.</span>
            </label>
            <div className="flex flex-col gap-[13px] px-[5px] py-5">
              {TERMS_LIST.map(({ key, label, href }) => (
                <div key={key} className="flex items-center justify-between">
                  <label className="flex items-center gap-[15px] cursor-pointer">
                    <FormCheckbox
                      name={key}
                      checked={agreedTerms[key]}
                      onChange={(checked) =>
                        setAgreedTerms((prev) => ({ ...prev, [key]: checked }))
                      }
                    />
                    <span className="text-[13px] leading-[13px] font-light">{label}</span>
                  </label>
                  {href && (
                    <button
                      type="button"
                      onClick={() => handleOpenTermsDialog(key, label)}
                      className="shrink-0 cursor-pointer"
                    >
                      <Icon name="nav-right" alt="약관 보기" />
                    </button>
                  )}
                </div>
              ))}
            </div>

            <div className="my-5">
              <AppSubmitButton isSubmitting={isSubmitting} loadingText="가입 중">
                가입하기
              </AppSubmitButton>
            </div>
          </div>
        </BorderedSection>
      </SectionStack>
      <AppTermsDialog
        open={termsDialog.open}
        onOpenChange={(open) => setTermsDialog((prev) => ({ ...prev, open }))}
        title={termsDialog.title}
      >
        <div dangerouslySetInnerHTML={{ __html: termsDialog.htmlContent }} />
      </AppTermsDialog>
    </form>
  )
}
