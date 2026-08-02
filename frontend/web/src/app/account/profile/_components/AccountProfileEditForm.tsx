'use client'

import AppFormField from '@/components/ui/AppFormField'
import AppInputText from '@/components/ui/AppInputText'
import AppSubmitButton from '@/components/ui/AppSubmitButton'
import { toast } from '@/components/ui/AppToaster'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import { useMyProfile, useUpdateMemberProfile } from '@/domains/member/member.hook'
import { extractZodFieldErrors } from '@/lib/form'
import { uploadFileClient } from '@/lib/uploadFile'
import { useRouter } from 'next/navigation'
import { useEffect, useState } from 'react'
import { z } from 'zod'
import ProfileImageEditor from './ProfileImageEditor'

const profileSchema = z.object({
  nickname: z
    .string()
    .min(1, '닉네임을 입력해 주세요.')
    .max(20, '닉네임은 최대 20자까지 가능합니다.')
    .refine((val) => !/\s/.test(val), '닉네임에 공백을 포함할 수 없습니다.'),
  statusMessage: z.string().max(30, '상태메세지는 최대 30자까지 가능합니다.'),
})

type ProfileErrors = {
  nickname?: string
  statusMessage?: string
}

export default function AccountProfileEditForm() {
  const router = useRouter()

  const { memberProfile, isLoading } = useMyProfile()
  const { mutateAsync: updateProfile } = useUpdateMemberProfile(memberProfile?.id)

  const [nickname, setNickname] = useState('')
  const [statusMessage, setStatusMessage] = useState('')
  const [profileImageUrl, setProfileImageUrl] = useState<string | null>(null)
  const [profileImageFileId, setProfileImageFileId] = useState<number | null>(null)
  const [errors, setErrors] = useState<ProfileErrors>({})
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [isUploading, setIsUploading] = useState(false)

  // 프로필 정보로 초기값 설정
  useEffect(() => {
    if (memberProfile) {
      setNickname(memberProfile.nickname || '')
      setStatusMessage(memberProfile.statusMessage || '')
      setProfileImageUrl(memberProfile.profileImageUrl || null)
    }
  }, [memberProfile])

  const handleImageChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (!file) return

    // 이미지 파일 타입 검증
    const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp']
    if (!allowedTypes.includes(file.type)) {
      toast('jpg, png, gif, webp 형식의 이미지만 업로드 가능합니다.')
      return
    }

    // 파일 크기 검증 (10MB)
    const maxSize = 10 * 1024 * 1024
    if (file.size > maxSize) {
      toast('파일 크기는 최대 10MB까지 가능합니다.')
      return
    }

    // 미리보기용 DataURL 생성
    const reader = new FileReader()
    reader.onloadend = () => {
      setProfileImageUrl(reader.result as string)
    }
    reader.readAsDataURL(file)

    // 파일 업로드
    setIsUploading(true)
    try {
      const response = await uploadFileClient(file)

      if (response?.data) {
        setProfileImageFileId(response.data)
      } else {
        toast(COMMON_ERROR_MESSAGES.MUTATION_ERROR)
        setProfileImageUrl(null)
      }
    } catch (error) {
      console.error('이미지 업로드 실패:', error)
      toast(COMMON_ERROR_MESSAGES.MUTATION_ERROR)
      setProfileImageUrl(null)
    } finally {
      setIsUploading(false)
    }
  }

  const validate = () => {
    const result = profileSchema.safeParse({ nickname, statusMessage })
    if (!result.success) {
      setErrors(extractZodFieldErrors(result.error) as ProfileErrors)
      return false
    }
    setErrors({})
    return true
  }

  const handleSubmit = async () => {
    if (!validate()) return

    setIsSubmitting(true)
    try {
      const response = await updateProfile({
        nickname,
        statusMessage,
        profileImageFileId: profileImageFileId || undefined,
      })

      if (!response?.error) {
        toast('프로필이 변경됐습니다.')
        router.back()
      } else {
        toast(COMMON_ERROR_MESSAGES.MUTATION_ERROR)
      }
    } catch (error) {
      console.error('프로필 수정 실패:', error)
      toast(COMMON_ERROR_MESSAGES.MUTATION_ERROR)
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div>
      <ProfileImageEditor profileImageUrl={profileImageUrl} onImageChange={handleImageChange} />
      <div className="flex flex-col gap-5 px-[15px]">
        <AppFormField label="닉네임" required error={errors.nickname}>
          {({ className }) => (
            <AppInputText
              value={nickname}
              onChange={(e) => setNickname(e.target.value.replace(/\s/g, ''))}
              placeholder="닉네임은 공백을 포함하지 않는 최대 20자까지 가능합니다."
              maxLength={20}
              className={className}
            />
          )}
        </AppFormField>
        <AppFormField label="상태메세지" error={errors.statusMessage}>
          {({ className }) => (
            <AppInputText
              value={statusMessage}
              onChange={(e) => setStatusMessage(e.target.value)}
              placeholder="상태메세지는 공백을 포함한 최대 30자까지 가능합니다."
              maxLength={30}
              className={className}
            />
          )}
        </AppFormField>
      </div>
      <div className="px-[15px] mt-[30px]">
        <AppSubmitButton
          onClick={handleSubmit}
          disabled={isLoading}
          isSubmitting={isUploading || isSubmitting}
          loadingText={isUploading ? '이미지 업로드 중' : '변경 중'}
        >
          완료
        </AppSubmitButton>
      </div>
    </div>
  )
}
