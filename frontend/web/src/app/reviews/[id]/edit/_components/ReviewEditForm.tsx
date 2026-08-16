'use client'

import { updateReview } from '@/actions/review'
import AppSubmitButton from '@/components/ui/AppSubmitButton'
import { toast } from '@/components/ui/AppToaster'
import BorderedSection from '@/components/ui/BorderedSection'
import SectionStack from '@/components/ui/SectionStack'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import { extractZodFieldErrors } from '@/lib/form'
import { useRouter } from 'next/navigation'
import { useState, useTransition } from 'react'
import { z } from 'zod'
import ReviewEditContentSection from './ReviewEditContentSection'
import ReviewEditOwnerOnlySection from './ReviewEditOwnerOnlySection'
import ReviewEditRatingSection from './ReviewEditRatingSection'

const reviewEditSchema = z.object({
  ratings: z
    .object({
      taste: z.number(),
      amount: z.number(),
      price: z.number(),
    })
    .refine(({ taste, amount, price }) => taste > 0 && amount > 0 && price > 0, {
      message: '평점을 선택해 주세요.',
    }),
  content: z.string().min(1, '내용을 입력해 주세요.'),
  tags: z.array(z.string()),
})

type FormData = z.infer<typeof reviewEditSchema>

type FormErrors = Partial<Record<'ratings' | 'content', string>>

interface Props {
  reviewId: number
  tasteRating: number
  amountRating: number
  priceRating: number
  content: string
  tagNames: string[]
  ownerOnly: boolean
}

export default function ReviewEditForm({
  reviewId,
  tasteRating,
  amountRating,
  priceRating,
  content,
  tagNames,
  ownerOnly,
}: Props) {
  const router = useRouter()

  const [formData, setFormData] = useState<FormData>({
    ratings: { taste: tasteRating, amount: amountRating, price: priceRating },
    content,
    tags: tagNames,
  })
  const [errors, setErrors] = useState<FormErrors>({})
  const [isSubmitting, startSubmitting] = useTransition()

  const handleRatingChange = (key: 'taste' | 'amount' | 'price', value: number) => {
    setFormData((prev) => ({ ...prev, ratings: { ...prev.ratings, [key]: value } }))
    if (errors.ratings) {
      setErrors((prev) => ({ ...prev, ratings: undefined }))
    }
  }

  const handleContentChange = (value: string) => {
    setFormData((prev) => ({ ...prev, content: value }))
    if (errors.content) {
      setErrors((prev) => ({ ...prev, content: undefined }))
    }
  }

  const handleTagsChange = (tags: string[]) => {
    setFormData((prev) => ({ ...prev, tags }))
  }

  const validateForm = (): boolean => {
    const trimmedData = {
      ...formData,
      content: formData.content.trim(),
    }
    const result = reviewEditSchema.safeParse(trimmedData)

    if (result.success) {
      setErrors({})
      return true
    }

    const fieldErrors = extractZodFieldErrors(result.error) as FormErrors

    // ratings refine 에러는 path가 없으므로 별도 처리
    const ratingsIssue = result.error.issues.find(
      (issue) => issue.path.length === 0 || issue.path[0] === 'ratings',
    )
    if (ratingsIssue && !fieldErrors.ratings) {
      fieldErrors.ratings = ratingsIssue.message
    }

    setErrors(fieldErrors)
    return false
  }

  const handleSubmit = () => {
    if (!validateForm()) {
      toast('작성하지 않은 항목이 있습니다. 확인해 주세요.')
      return
    }

    startSubmitting(async () => {
      // ownerOnly는 전환 불허 정책상 요청 본문에 포함하지 않는다(backend.md §3-2).
      const { error } = await updateReview(reviewId, {
        tasteRating: formData.ratings.taste,
        amountRating: formData.ratings.amount,
        priceRating: formData.ratings.price,
        content: formData.content,
        uploadedFileIds: [],
        tags: formData.tags,
      })

      if (error) {
        toast(COMMON_ERROR_MESSAGES.MUTATION_ERROR)
        return
      }

      toast('리뷰가 수정되었습니다.')
      router.back()
    })
  }

  return (
    <SectionStack>
      <BorderedSection>
        <ReviewEditRatingSection
          ratings={formData.ratings}
          error={errors.ratings}
          onRatingChange={handleRatingChange}
        />
      </BorderedSection>
      <BorderedSection>
        <ReviewEditContentSection
          content={formData.content}
          contentError={errors.content}
          tags={formData.tags}
          onContentChange={handleContentChange}
          onTagsChange={handleTagsChange}
        />
      </BorderedSection>
      <BorderedSection>
        <ReviewEditOwnerOnlySection ownerOnly={ownerOnly} />
      </BorderedSection>
      <BorderedSection>
        <div className="p-[15px]">
          <AppSubmitButton
            onClick={handleSubmit}
            isSubmitting={isSubmitting}
            loadingText="수정 중..."
          >
            수정하기
          </AppSubmitButton>
        </div>
      </BorderedSection>
    </SectionStack>
  )
}
