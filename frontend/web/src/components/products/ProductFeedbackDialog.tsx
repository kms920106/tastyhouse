'use client'

import { createProductFeedback } from '@/actions/product'
import AppFormField from '@/components/ui/AppFormField'
import AppTextarea from '@/components/ui/AppTextarea'
import { toast } from '@/components/ui/AppToaster'
import { Modal, ModalContentWrapper, ModalHeader, ModalTitle } from '@/components/ui/Modal'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import {
  PRODUCT_FEEDBACK_CONTENT_MAX_LENGTH,
  PRODUCT_FEEDBACK_COPY,
  PRODUCT_FEEDBACK_MESSAGE,
  PRODUCT_FEEDBACK_TYPE_OPTIONS,
} from '@/domains/product/product.constants'
import type { ProductFeedbackType } from '@/domains/product/product.dto'
import { extractZodFieldErrors } from '@/lib/form'
import { cn } from '@/lib/utils'
import { useState, useTransition } from 'react'
import { z } from 'zod'

/**
 * 의견 폼 스키마.
 *
 * `content` 는 `ETC` 일 때만 필수라 필드 단독 규칙으로 표현할 수 없어 `superRefine` 을 쓴다.
 * 길이 제한은 서버와 같은 500자다 — 어긋나면 화면이 통과시킨 값을 서버가 거절한다.
 */
const productFeedbackSchema = z
  .object({
    feedbackType: z.enum(['PRICE', 'IMAGE', 'COMPOSITION', 'SOLD_OUT', 'ETC'], {
      message: PRODUCT_FEEDBACK_MESSAGE.TYPE_REQUIRED,
    }),
    content: z
      .string()
      .trim()
      .max(PRODUCT_FEEDBACK_CONTENT_MAX_LENGTH, PRODUCT_FEEDBACK_MESSAGE.CONTENT_TOO_LONG)
      .optional(),
  })
  .superRefine((value, ctx) => {
    if (value.feedbackType === 'ETC' && !value.content) {
      ctx.addIssue({
        code: 'custom',
        path: ['content'],
        message: PRODUCT_FEEDBACK_MESSAGE.CONTENT_REQUIRED,
      })
    }
  })

type FormErrors = { feedbackType?: string; content?: string }

interface Props {
  productId: number
  open: boolean
  onOpenChange: (open: boolean) => void
}

/**
 * 메뉴 정보 의견 보내기 다이얼로그.
 *
 * 유형을 먼저 고르게 하는 이유는 자유 서술만으로는 점주가 무엇을 고쳐야 할지 분류할 수 없고
 * 집계도 되지 않기 때문이다. 서술은 `기타` 에서만 받는다.
 */
export default function ProductFeedbackDialog({ productId, open, onOpenChange }: Props) {
  const [feedbackType, setFeedbackType] = useState<ProductFeedbackType | ''>('')
  const [content, setContent] = useState('')
  const [errors, setErrors] = useState<FormErrors>({})
  const [isSubmitting, startSubmitting] = useTransition()

  const isEtc = feedbackType === 'ETC'

  const reset = () => {
    setFeedbackType('')
    setContent('')
    setErrors({})
  }

  const handleOpenChange = (next: boolean) => {
    if (!next) reset()
    onOpenChange(next)
  }

  const handleSubmit = () => {
    const result = productFeedbackSchema.safeParse({
      feedbackType: feedbackType === '' ? undefined : feedbackType,
      // `기타` 가 아니면 서술을 보내지 않는다 — 유형만으로 집계되는 값이다.
      content: isEtc ? content.trim() : undefined,
    })

    if (!result.success) {
      setErrors(extractZodFieldErrors(result.error) as FormErrors)
      return
    }
    setErrors({})

    startSubmitting(async () => {
      try {
        const response = await createProductFeedback(productId, result.data)

        // 7일 내 중복 제보(`PRODUCT_FEEDBACK_ALREADY_SUBMITTED`)는 오류 화면이 아니라 안내다.
        // 서버가 한국어 문구를 내려주므로 그대로 노출하고, 없을 때만 기본 문구로 갈음한다.
        if (response?.error) {
          toast(response.error ?? PRODUCT_FEEDBACK_MESSAGE.ALREADY_SUBMITTED)
          return
        }

        toast(PRODUCT_FEEDBACK_MESSAGE.SUBMIT_SUCCESS)
        handleOpenChange(false)
      } catch (error) {
        console.error('메뉴 의견 전송 실패:', error)
        toast(COMMON_ERROR_MESSAGES.MUTATION_ERROR)
      }
    })
  }

  return (
    <Modal open={open} onOpenChange={handleOpenChange} contentClassName="max-w-[85%]">
      <ModalHeader className="px-[15px] pt-5 pb-2.5">
        <ModalTitle className="text-base leading-[16px]">{PRODUCT_FEEDBACK_COPY.TITLE}</ModalTitle>
      </ModalHeader>

      <ModalContentWrapper className="flex flex-col gap-5 px-[15px] pb-5">
        <div className="flex flex-col gap-2.5">
          {PRODUCT_FEEDBACK_TYPE_OPTIONS.map((option) => (
            <label key={option.value} className="flex items-center gap-2.5">
              <input
                type="radio"
                name="product-feedback-type"
                value={option.value}
                checked={feedbackType === option.value}
                onChange={() => {
                  setFeedbackType(option.value)
                  setErrors({})
                }}
                className="size-4 accent-black"
              />
              <span className="text-sm leading-[14px]">{option.label}</span>
            </label>
          ))}
          {errors.feedbackType && (
            <p className="text-xs leading-[12px] text-[#bc4040]">{errors.feedbackType}</p>
          )}
        </div>

        {/* 서술은 `기타` 에서만 받는다 — 나머지 유형은 라벨 자체가 내용이다 */}
        {isEtc && (
          <AppFormField label={PRODUCT_FEEDBACK_COPY.CONTENT_LABEL} required error={errors.content}>
            {({ className }) => (
              <AppTextarea
                value={content}
                onChange={(event) => setContent(event.target.value)}
                maxLength={PRODUCT_FEEDBACK_CONTENT_MAX_LENGTH}
                rows={4}
                placeholder={PRODUCT_FEEDBACK_COPY.CONTENT_PLACEHOLDER}
                className={className}
              />
            )}
          </AppFormField>
        )}

        <p className="text-xs leading-relaxed text-[#666666]">
          {PRODUCT_FEEDBACK_MESSAGE.PRIVACY_NOTICE}
        </p>

        <div className="flex gap-2.5">
          <button
            type="button"
            onClick={() => handleOpenChange(false)}
            className="h-[50px] flex-1 border border-line text-sm leading-[14px]"
          >
            {PRODUCT_FEEDBACK_COPY.CANCEL}
          </button>
          <button
            type="button"
            onClick={handleSubmit}
            disabled={isSubmitting}
            className={cn(
              'h-[50px] flex-1 bg-black text-sm leading-[14px] text-white',
              isSubmitting && 'opacity-50',
            )}
          >
            {PRODUCT_FEEDBACK_COPY.SUBMIT}
          </button>
        </div>
      </ModalContentWrapper>
    </Modal>
  )
}
