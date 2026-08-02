'use client'

import { loginFormAction } from '@/actions/auth'
import AppInputEmail from '@/components/ui/AppInputEmail'
import AppInputPassword from '@/components/ui/AppInputPassword'
import AppPrimaryButton from '@/components/ui/AppPrimaryButton'
import { toast } from '@/components/ui/AppToaster'
import CircleCheckbox from '@/components/ui/CircleCheckbox'
import { useActionState, useEffect, useState, useTransition } from 'react'
import { z } from 'zod'

const loginSchema = z.object({
  username: z.string().min(1, '아이디를 입력해 주세요.'),
  password: z.string().min(1, '비밀번호를 입력해 주세요.'),
})

type FormErrors = Partial<Record<keyof z.infer<typeof loginSchema>, string>>

export default function LoginCredentialForm() {
  const [keepLoggedIn, setKeepLoggedIn] = useState(false)
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [errors, setErrors] = useState<FormErrors>({})

  const [state, formAction] = useActionState(loginFormAction, null)
  const [isSubmitting, startSubmitTransition] = useTransition()

  useEffect(() => {
    if (state && !state.success) {
      toast(state.error)
    }
  }, [state])

  const validateForm = (): boolean => {
    const result = loginSchema.safeParse({ username, password })
    if (!result.success) {
      const fieldErrors: FormErrors = {}
      for (const issue of result.error.issues) {
        const field = issue.path[0] as keyof FormErrors
        if (!fieldErrors[field]) fieldErrors[field] = issue.message
      }
      setErrors(fieldErrors)
      return false
    }
    setErrors({})
    return true
  }

  const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault()
    if (!validateForm()) return
    startSubmitTransition(() => {
      formAction(new FormData(e.currentTarget))
    })
  }

  return (
    <form onSubmit={handleSubmit}>
      <div className="space-y-4">
        <div className="flex flex-col gap-2">
          <AppInputEmail
            id="username"
            name="username"
            placeholder="아이디(이메일주소)를 입력해 주세요."
            value={username}
            onChange={(e) => {
              setUsername(e.target.value)
              if (errors.username) setErrors((prev) => ({ ...prev, username: undefined }))
            }}
            className={
              errors.username ? 'border-[#bc4040] focus-visible:border-[#bc4040]' : undefined
            }
          />
          {errors.username && (
            <p className="text-xs leading-[12px] text-[#bc4040]">{errors.username}</p>
          )}
        </div>

        <div className="flex flex-col gap-2">
          <AppInputPassword
            id="password"
            name="password"
            placeholder="비밀번호를 입력해 주세요."
            value={password}
            onChange={(e) => {
              setPassword(e.target.value)
              if (errors.password) setErrors((prev) => ({ ...prev, password: undefined }))
            }}
            className={
              errors.password ? 'border-[#bc4040] focus-visible:border-[#bc4040]' : undefined
            }
          />
          {errors.password && (
            <p className="text-xs leading-[12px] text-[#bc4040]">{errors.password}</p>
          )}
        </div>

        <label className="flex items-center gap-2 cursor-pointer">
          <CircleCheckbox checked={keepLoggedIn} onChange={setKeepLoggedIn} />
          <span className="text-sm leading-[14px] text-[#666666]">로그인 상태 유지</span>
        </label>
        <input type="hidden" name="rememberMe" value={String(keepLoggedIn)} />
      </div>

      <div className="mt-[30px]">
        <AppPrimaryButton type="submit" disabled={isSubmitting}>
          {isSubmitting ? '로그인 중...' : '로그인'}
        </AppPrimaryButton>
      </div>
    </form>
  )
}
