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
import DeliveryRatingSection, {
  DELIVERY_COMMENT_MAX_LENGTH,
} from '@/components/reviews/DeliveryRatingSection'
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
  // 0은 "배달 평가 해제" 상태(DeliveryRatingSection의 토글 해제)를 의미하므로 허용한다.
  deliveryRating: z.union([z.literal(0), z.number().min(1).max(5)]).optional(),
  deliveryComment: z.string().max(DELIVERY_COMMENT_MAX_LENGTH).optional(),
})

type FormData = z.infer<typeof reviewEditSchema>

type FormErrors = Partial<Record<'ratings' | 'content', string>>

/** 배달 평가 섹션을 렌더하는 주문유형 */
const DELIVERY_ORDER_METHOD = 'DELIVERY'

interface Props {
  reviewId: number
  tasteRating: number
  amountRating: number
  priceRating: number
  content: string
  tagNames: string[]
  ownerOnly: boolean
  orderMethod: string | null
  deliveryRating: number | null
  deliveryComment: string | null
}

export default function ReviewEditForm({
  reviewId,
  tasteRating,
  amountRating,
  priceRating,
  content,
  tagNames,
  ownerOnly,
  orderMethod,
  deliveryRating,
  deliveryComment,
}: Props) {
  const router = useRouter()

  /**
   * 배달 평가 섹션을 그릴지 여부.
   *
   * `orderMethod`가 null이면(서버가 작성자에게만 채워주는 값) 섹션을 그리지 않는다.
   */
  const isDelivery = orderMethod === DELIVERY_ORDER_METHOD

  /**
   * 배달 평가 필드를 요청에 실을지 여부.
   *
   * 섹션을 그리지 않더라도 **기존 배달 평가가 있으면 반드시 함께 보낸다** — PUT이 전체 교체라
   * 빠뜨리면 본문만 수정해도 조용히 지워진다. `orderMethod`가 어떤 이유로든 null로 내려온
   * 경우까지 방어하기 위해 렌더 조건이 아니라 데이터 유무로 판정한다.
   */
  const hasExistingDeliveryRating = deliveryRating !== null
  const shouldSendDelivery = isDelivery || hasExistingDeliveryRating

  const [formData, setFormData] = useState<FormData>({
    ratings: { taste: tasteRating, amount: amountRating, price: priceRating },
    content,
    tags: tagNames,
    // ⚠️ PUT은 전체 교체다. 기존 배달 평가를 초기값으로 채워두지 않으면
    //    본문 오타만 고쳐도 배달 평가가 조용히 지워진다.
    deliveryRating: deliveryRating ?? undefined,
    deliveryComment: deliveryComment ?? undefined,
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

  const handleDeliveryRatingChange = (nextRating: number) => {
    setFormData((prev) => ({ ...prev, deliveryRating: nextRating }))
  }

  const handleDeliveryCommentChange = (nextComment: string) => {
    setFormData((prev) => ({ ...prev, deliveryComment: nextComment }))
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
        // PUT은 전체 교체다 — 배달 주문이면 현재 폼 값을 항상 함께 보낸다.
        // 사용자가 건드리지 않았으면 초기값(=기존 값)이 그대로 나가고, 비웠으면 null로 나가 실제로 지워진다.
        //
        // 별점 없이 코멘트만 보내지 않는다. 서버 검증(validateDeliveryRating)은 "둘 다 null이면 통과"라
        // 코멘트만 실린 요청을 막지 못하고, 별점 없는 배달 평가라는 모순된 상태가 그대로 저장된다.
        ...(shouldSendDelivery
          ? formData.deliveryRating
            ? {
                deliveryRating: formData.deliveryRating,
                deliveryComment: formData.deliveryComment?.trim() || null,
              }
            : { deliveryRating: null, deliveryComment: null }
          : {}),
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
      {isDelivery && (
        <BorderedSection>
          <DeliveryRatingSection
            rating={formData.deliveryRating ?? 0}
            comment={formData.deliveryComment ?? ''}
            onRatingChange={handleDeliveryRatingChange}
            onCommentChange={handleDeliveryCommentChange}
          />
        </BorderedSection>
      )}
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
