'use client'

import AppFormField from '@/components/ui/AppFormField'
import AppInputText from '@/components/ui/AppInputText'
import AppOutlineButton from '@/components/ui/AppOutlineButton'
import AppSelect from '@/components/ui/AppSelect'
import PhoneVerificationField from '@/components/ui/PhoneVerificationField'
import type { MemberGender } from '@/domains/member'
import type { UsePhoneVerificationReturn } from '@/hooks/usePhoneVerification'
import { cn } from '@/lib/utils'

const BIRTH_YEARS = Array.from({ length: 100 }, (_, i) => 2026 - i)
const BIRTH_MONTHS = Array.from({ length: 12 }, (_, i) => i + 1)
const BIRTH_DAYS = Array.from({ length: 31 }, (_, i) => i + 1)

interface ProfileErrors {
  fullName?: string
  nickname?: string
  phoneNumber?: string
  birthDate?: string
  gender?: string
}

interface Props {
  fullName: string
  nickname: string
  isNicknameChecked: boolean
  isCheckingNickname: boolean
  phoneVerification: UsePhoneVerificationReturn
  birthYear: string
  birthMonth: string
  birthDay: string
  gender: MemberGender | null
  referrerNickname: string
  isReferrerVerified: boolean
  isCheckingReferrer: boolean
  errors: ProfileErrors
  onFullNameChange: (value: string) => void
  onNicknameChange: (value: string) => void
  onCheckNickname: () => void
  onBirthYearChange: (value: string) => void
  onBirthMonthChange: (value: string) => void
  onBirthDayChange: (value: string) => void
  onGenderChange: (value: MemberGender) => void
  onReferrerNicknameChange: (value: string) => void
  onCheckReferrer: () => void
  onClearError: (field: keyof ProfileErrors) => void
  onPhoneError: (message: string) => void
}

export default function AuthSignupProfileFields({
  fullName,
  nickname,
  isNicknameChecked,
  isCheckingNickname,
  phoneVerification,
  birthYear,
  birthMonth,
  birthDay,
  gender,
  referrerNickname,
  isReferrerVerified,
  isCheckingReferrer,
  errors,
  onFullNameChange,
  onNicknameChange,
  onCheckNickname,
  onBirthYearChange,
  onBirthMonthChange,
  onBirthDayChange,
  onGenderChange,
  onReferrerNicknameChange,
  onCheckReferrer,
  onClearError,
  onPhoneError,
}: Props) {
  return (
    <>
      <input type="hidden" name="phoneVerifyToken" value={phoneVerification.phoneVerifyToken} />

      {/* 이름 */}
      <AppFormField label="이름" required error={errors.fullName}>
        {({ className }) => (
          <AppInputText
            id="fullName"
            name="fullName"
            placeholder="이름을 입력해 주세요."
            value={fullName}
            onChange={(e) => {
              onFullNameChange(e.target.value)
              if (errors.fullName) onClearError('fullName')
            }}
            className={className}
          />
        )}
      </AppFormField>

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
                  onNicknameChange(e.target.value)
                  if (errors.nickname) onClearError('nickname')
                }}
                className={cn('flex-1', className)}
              />
              <AppOutlineButton
                type="button"
                onClick={onCheckNickname}
                disabled={!nickname.trim() || isCheckingNickname || isNicknameChecked}
                className="shrink-0"
              >
                {isCheckingNickname ? '확인 중' : '중복확인'}
              </AppOutlineButton>
            </div>
            {isNicknameChecked && (
              <p className="text-xs leading-[12px] text-[#999999]">사용 가능한 닉네임입니다.</p>
            )}
          </div>
        )}
      </AppFormField>

      {/* 휴대폰 번호 */}
      <PhoneVerificationField
        verification={phoneVerification}
        error={errors.phoneNumber}
        phoneInputName="phoneNumber"
        onClearError={() => onClearError('phoneNumber')}
        onInvalidPhone={onPhoneError}
      />

      {/* 생년월일 */}
      <AppFormField label="생년월일" required error={errors.birthDate}>
        {() => (
          <div className="flex gap-2">
            <AppSelect
              name="birthYear"
              value={birthYear}
              onChange={(e) => {
                onBirthYearChange(e.target.value)
                if (errors.birthDate) onClearError('birthDate')
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
                onBirthMonthChange(e.target.value)
                if (errors.birthDate) onClearError('birthDate')
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
                onBirthDayChange(e.target.value)
                if (errors.birthDate) onClearError('birthDate')
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
                    onGenderChange(value)
                    if (errors.gender) onClearError('gender')
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
              onChange={(e) => onReferrerNicknameChange(e.target.value)}
              className="flex-1"
            />
            <input
              type="hidden"
              name="verifiedReferrerNickname"
              value={isReferrerVerified ? referrerNickname : ''}
            />
            <AppOutlineButton
              type="button"
              onClick={onCheckReferrer}
              disabled={!referrerNickname.trim() || isCheckingReferrer}
              className="shrink-0"
            >
              {isCheckingReferrer ? '확인 중' : '추천인 확인'}
            </AppOutlineButton>
          </div>
        )}
      </AppFormField>
    </>
  )
}
