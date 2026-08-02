'use client'

import { signupFormAction } from '@/actions/auth'
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
import AuthSignupCredentialFields from '@/app/auth/signup/(page)/_components/AuthSignupCredentialFields'
import AuthSignupProfileFields from '@/app/auth/signup/(page)/_components/AuthSignupProfileFields'
import AuthSignupTermSection, {
  TERMS_LIST,
  type TermsKey,
} from '@/app/auth/signup/(page)/_components/AuthSignupTermSection'
import AppSubmitButton from '@/components/ui/AppSubmitButton'
import { toast } from '@/components/ui/AppToaster'
import BorderedSection from '@/components/ui/BorderedSection'
import SectionStack from '@/components/ui/SectionStack'
import {
  EMAIL_ERROR_MESSAGES,
  EMAIL_REGEX,
  PHONE_ERROR_MESSAGES,
  PHONE_REGEX,
} from '@/constants/validation'
import type { MemberGender } from '@/domains/member'
import { useEmailVerification } from '@/hooks/useEmailVerification'
import { usePhoneVerification } from '@/hooks/usePhoneVerification'
import { extractZodFieldErrors } from '@/lib/form'
import { useActionState, useEffect, useState, useTransition } from 'react'
import { z } from 'zod'

const signupSchema = z.object({
  email: z.string().superRefine((val, ctx) => {
    if (val.length === 0) {
      ctx.addIssue({ code: 'custom', message: EMAIL_ERROR_MESSAGES.REQUIRED })
      return
    }
    if (!EMAIL_REGEX.test(val)) {
      ctx.addIssue({ code: 'custom', message: EMAIL_ERROR_MESSAGES.INVALID })
    }
  }),
  password: z.string().superRefine((val, ctx) => {
    if (val.length === 0) {
      ctx.addIssue({ code: 'custom', message: '비밀번호를 입력해 주세요.' })
      return
    }
    if (
      val.length < 8 ||
      !/[A-Za-z]/.test(val) ||
      !/[0-9]/.test(val) ||
      !/[!@#$%^&*()_+\-=[\]{};':"\\|,.<>/?]/.test(val)
    ) {
      ctx.addIssue({
        code: 'custom',
        message:
          '올바른 비밀번호 형식이 아닙니다. (8자 이상, 영문자·숫자·특수문자를 각각 1개 이상 포함)',
      })
    }
  }),
  passwordConfirm: z.string().superRefine((val, ctx) => {
    if (val.length === 0) {
      ctx.addIssue({ code: 'custom', message: '비밀번호를 입력해 주세요.' })
      return
    }
    if (
      val.length < 8 ||
      !/[A-Za-z]/.test(val) ||
      !/[0-9]/.test(val) ||
      !/[!@#$%^&*()_+\-=[\]{};':"\\|,.<>/?]/.test(val)
    ) {
      ctx.addIssue({
        code: 'custom',
        message:
          '올바른 비밀번호 형식이 아닙니다. (8자 이상, 영문자·숫자·특수문자를 각각 1개 이상 포함)',
      })
    }
  }),
  fullName: z.string().min(1, '이름을 입력해 주세요.'),
  nickname: z.string().min(1, '닉네임을 입력해 주세요.'),
  phoneNumber: z.string().superRefine((val, ctx) => {
    if (val.length === 0) {
      ctx.addIssue({ code: 'custom', message: PHONE_ERROR_MESSAGES.REQUIRED })
      return
    }
    if (!PHONE_REGEX.test(val)) {
      ctx.addIssue({ code: 'custom', message: PHONE_ERROR_MESSAGES.INVALID })
    }
  }),
  birthYear: z.string(),
  birthMonth: z.string(),
  birthDay: z.string(),
  gender: z.enum(['MALE', 'FEMALE'], { message: '성별을 선택해 주세요.' }),
})

type FormErrors = Partial<Record<keyof z.infer<typeof signupSchema> | 'birthDate', string>>

type TermsDialog = {
  open: boolean
  title: string
  htmlContent: string
}

type Props = {
  onOpenTermsDialog: (dialog: TermsDialog) => void
}

export default function AuthSignupForm({ onOpenTermsDialog }: Props) {
  const emailVerification = useEmailVerification({
    sendFn: sendEmailVerificationCode,
    confirmFn: confirmEmailVerificationCodeForEmailField,
  })

  const [password, setPassword] = useState('')
  const [passwordConfirm, setPasswordConfirm] = useState('')
  const [fullName, setFullName] = useState('')

  const [nickname, setNickname] = useState('')
  const [isNicknameChecked, setIsNicknameChecked] = useState(false)
  const [isCheckingNickname, startCheckingNickname] = useTransition()

  const phoneVerification = usePhoneVerification()

  const [birthYear, setBirthYear] = useState('')
  const [birthMonth, setBirthMonth] = useState('')
  const [birthDay, setBirthDay] = useState('')
  const [gender, setGender] = useState<MemberGender | null>(null)

  const [referrerNickname, setReferrerNickname] = useState('')
  const [isReferrerVerified, setIsReferrerVerified] = useState(false)
  const [isCheckingReferrer, startCheckingReferrer] = useTransition()

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
  const [state, formAction] = useActionState(signupFormAction, null)
  const [isSubmitting, startSubmitTransition] = useTransition()

  useEffect(() => {
    if (state && !state.success) {
      toast(state.error)
    }
  }, [state])

  useEffect(() => {
    const allChecked = TERMS_LIST.every(({ key }) => agreedTerms[key])
    setAgreedAll(allChecked)
  }, [agreedTerms])

  const handleClearError = (field: keyof FormErrors) => {
    setErrors((prev) => ({ ...prev, [field]: undefined }))
  }

  const handleAgreedAll = (checked: boolean) => {
    setAgreedAll(checked)
    const updated = {} as Record<TermsKey, boolean>
    TERMS_LIST.forEach(({ key }) => {
      updated[key] = checked
    })
    setAgreedTerms(updated)
  }

  const handleTermChange = (key: TermsKey, checked: boolean) => {
    setAgreedTerms((prev) => ({ ...prev, [key]: checked }))
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
      onOpenTermsDialog({ open: true, title: label, htmlContent })
    } catch {
      toast('약관 내용을 불러오는데 실패했습니다.')
    }
  }

  const validateForm = (): boolean => {
    const result = signupSchema.safeParse({
      email: emailVerification.email,
      password,
      passwordConfirm,
      fullName,
      nickname,
      phoneNumber: phoneVerification.phone.replace(/-/g, ''),
      birthYear,
      birthMonth,
      birthDay,
      gender: gender ?? '',
    })

    const fieldErrors: FormErrors = result.success
      ? {}
      : (extractZodFieldErrors(result.error) as FormErrors)

    if (
      !fieldErrors.passwordConfirm &&
      password.length > 0 &&
      passwordConfirm.length > 0 &&
      password !== passwordConfirm
    ) {
      fieldErrors.passwordConfirm = '비밀번호가 일치하지 않습니다.'
    }

    const hasYear = birthYear.length > 0
    const hasMonth = birthMonth.length > 0
    const hasDay = birthDay.length > 0

    if (!hasYear || !hasMonth || !hasDay) {
      fieldErrors.birthDate = '생년월일을 선택해 주세요.'
    }

    const hasErrors = Object.values(fieldErrors).some((v) => v !== undefined)

    if (hasErrors) {
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
    if (!phoneVerification.isVerified) {
      toast('휴대폰 인증을 완료해 주세요.')
      return
    }
    if (TERMS_LIST.filter((t) => t.required).some(({ key }) => !agreedTerms[key])) {
      toast('필수 약관에 동의해 주세요.')
      return
    }
    startSubmitTransition(() => {
      formAction(new FormData(e.currentTarget))
    })
  }

  return (
    <form onSubmit={handleSubmit}>
      <SectionStack>
        <BorderedSection>
          <div className="px-[15px] py-[30px] flex flex-col gap-5">
            <AuthSignupCredentialFields
              emailVerification={emailVerification}
              password={password}
              passwordConfirm={passwordConfirm}
              errors={{
                email: errors.email,
                password: errors.password,
                passwordConfirm: errors.passwordConfirm,
              }}
              onPasswordChange={setPassword}
              onPasswordConfirmChange={setPasswordConfirm}
              onClearEmailError={() => handleClearError('email')}
              onClearPasswordError={() => handleClearError('password')}
              onClearPasswordConfirmError={() => handleClearError('passwordConfirm')}
            />
            <AuthSignupProfileFields
              fullName={fullName}
              nickname={nickname}
              isNicknameChecked={isNicknameChecked}
              isCheckingNickname={isCheckingNickname}
              phoneVerification={phoneVerification}
              birthYear={birthYear}
              birthMonth={birthMonth}
              birthDay={birthDay}
              gender={gender}
              referrerNickname={referrerNickname}
              isReferrerVerified={isReferrerVerified}
              isCheckingReferrer={isCheckingReferrer}
              errors={{
                fullName: errors.fullName,
                nickname: errors.nickname,
                phoneNumber: errors.phoneNumber,
                birthDate: errors.birthDate,
                gender: errors.gender,
              }}
              onFullNameChange={setFullName}
              onNicknameChange={(value) => {
                setNickname(value)
                setIsNicknameChecked(false)
              }}
              onCheckNickname={handleCheckNickname}
              onBirthYearChange={setBirthYear}
              onBirthMonthChange={setBirthMonth}
              onBirthDayChange={setBirthDay}
              onGenderChange={setGender}
              onReferrerNicknameChange={(value) => {
                setReferrerNickname(value)
                setIsReferrerVerified(false)
              }}
              onCheckReferrer={handleCheckReferrer}
              onClearError={handleClearError}
              onPhoneError={(message) => setErrors((prev) => ({ ...prev, phoneNumber: message }))}
            />
          </div>
        </BorderedSection>

        <BorderedSection>
          <AuthSignupTermSection
            agreedAll={agreedAll}
            agreedTerms={agreedTerms}
            onAgreedAllChange={handleAgreedAll}
            onTermChange={handleTermChange}
            onOpenTermsDialog={handleOpenTermsDialog}
          />
        </BorderedSection>
      </SectionStack>

      <div className="px-[15px] py-5">
        <AppSubmitButton isSubmitting={isSubmitting} loadingText="가입 중">
          가입하기
        </AppSubmitButton>
      </div>
    </form>
  )
}
