'use client'

import { createOrderReview } from '@/actions/review'
import OrderProductItem from '@/components/orders/OrderProductItem'
import AppSubmitButton from '@/components/ui/AppSubmitButton'
import { toast } from '@/components/ui/AppToaster'
import BorderedSection from '@/components/ui/BorderedSection'
import FormCheckbox from '@/components/ui/FormCheckbox'
import SectionStack from '@/components/ui/SectionStack'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import { extractZodFieldErrors } from '@/lib/form'
import { useRouter } from 'next/navigation'
import { useCallback, useState, useTransition } from 'react'
import { z } from 'zod'
import DeliveryRatingSection, {
  DELIVERY_COMMENT_MAX_LENGTH,
} from '@/components/reviews/DeliveryRatingSection'
import ReviewContentSection from './ReviewContentSection'
import ReviewRatingSection from './ReviewRatingSection'

/** 배달 평가 섹션을 렌더하는 주문유형 */
const DELIVERY_ORDER_METHOD = 'DELIVERY'

const reviewSchema = z.object({
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
  ownerOnly: z.boolean(),
  // 배달 평가는 선택 항목이다. 메뉴 평가(menuRatings)는 별도 API 소관이라 여기에 넣지 않는다.
  deliveryRating: z.number().min(1).max(5).optional(),
  deliveryComment: z.string().max(DELIVERY_COMMENT_MAX_LENGTH).optional(),
})

type FormData = z.infer<typeof reviewSchema>

type FormErrors = Partial<Record<'ratings' | 'content', string>>

const INITIAL_FORM_DATA: FormData = {
  ratings: { taste: 0, amount: 0, price: 0 },
  content: '',
  tags: [],
  ownerOnly: false,
}

interface Props {
  orderProductId: number
  productId: number
  productName: string
  productImageUrl: string
  productPrice: number
  orderMethod: string
}

export default function OrderReviewCreateForm({
  orderProductId,
  productId,
  productName,
  productImageUrl,
  productPrice,
  orderMethod,
}: Props) {
  const isDelivery = orderMethod === DELIVERY_ORDER_METHOD
  const router = useRouter()

  const [formData, setFormData] = useState<FormData>(INITIAL_FORM_DATA)
  const [errors, setErrors] = useState<FormErrors>({})
  const [isUploading, setIsUploading] = useState(false)
  const [uploadedFileIds, setUploadedFileIds] = useState<number[]>([])
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

  const handleOwnerOnlyChange = (checked: boolean) => {
    setFormData((prev) => ({ ...prev, ownerOnly: checked }))
  }

  const handleDeliveryRatingChange = (deliveryRating: number) => {
    setFormData((prev) => ({ ...prev, deliveryRating }))
  }

  const handleDeliveryCommentChange = (deliveryComment: string) => {
    setFormData((prev) => ({ ...prev, deliveryComment }))
  }

  const handleUploadedFileIdsChange = useCallback((fileIds: number[]) => {
    setUploadedFileIds(fileIds)
  }, [])

  const handleUploadingChange = useCallback((uploading: boolean) => {
    setIsUploading(uploading)
  }, [])

  const validateForm = (): boolean => {
    const trimmedData = {
      ...formData,
      content: formData.content.trim(),
    }
    const result = reviewSchema.safeParse(trimmedData)

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
      const { error } = await createOrderReview({
        orderProductId,
        productId,
        tasteRating: formData.ratings.taste,
        amountRating: formData.ratings.amount,
        priceRating: formData.ratings.price,
        content: formData.content,
        uploadedFileIds,
        tags: formData.tags,
        ownerOnly: formData.ownerOnly,
        // 배달 주문이 아니면 값을 보내지 않는다 — 서버가 REVIEW_DELIVERY_RATING_NOT_ALLOWED로 거부한다.
        ...(isDelivery && formData.deliveryRating
          ? {
              deliveryRating: formData.deliveryRating,
              ...(formData.deliveryComment?.trim()
                ? { deliveryComment: formData.deliveryComment.trim() }
                : {}),
            }
          : {}),
      })

      if (error) {
        toast(COMMON_ERROR_MESSAGES.MUTATION_ERROR)
        return
      }

      toast('리뷰가 등록되었습니다.')
      router.back()
    })
  }

  return (
    <SectionStack>
      <BorderedSection>
        <div className="px-4">
          <OrderProductItem
            productName={productName}
            productImageUrl={productImageUrl}
            totalPrice={productPrice}
          />
        </div>
      </BorderedSection>
      <BorderedSection>
        <ReviewRatingSection
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
        <ReviewContentSection
          content={formData.content}
          contentError={errors.content}
          tags={formData.tags}
          onContentChange={handleContentChange}
          onTagsChange={handleTagsChange}
          onUploadedFileIdsChange={handleUploadedFileIdsChange}
          onUploadingChange={handleUploadingChange}
        />
      </BorderedSection>
      <BorderedSection>
        <div className="flex flex-col gap-2 px-[15px] py-5">
          {/* FormCheckbox가 자체적으로 label을 렌더하므로 바깥을 label로 감싸지 않는다(중첩 label 무효) */}
          <div className="flex items-center gap-2.5">
            <FormCheckbox
              name="ownerOnly"
              checked={formData.ownerOnly}
              onChange={handleOwnerOnlyChange}
            />
            <button
              type="button"
              className="text-sm leading-[14px] cursor-pointer"
              onClick={() => handleOwnerOnlyChange(!formData.ownerOnly)}
            >
              사장님만보기
            </button>
          </div>
          <p className="text-xs leading-relaxed text-[#666666]">
            체크하면 다른 고객에게 보이지 않고 사장님과 나만 볼 수 있어요.
          </p>
        </div>
      </BorderedSection>
      <BorderedSection>
        <div className="flex flex-col gap-5 px-[15px] py-5">
          <p className="text-base leading-[16px]">리뷰 작성시 포인트 적립 및 주의사항</p>
          <ul className="list-disc list-inside space-y-1">
            <li className="text-xs leading-relaxed text-[#666666]">
              일반(평점 및 내용)리뷰 작성시 <span className="text-main">100p 적립</span>
            </li>
            <li className="text-xs leading-relaxed text-[#666666]">
              포토리뷰 작성시 <span className="text-main">200p 적립</span>
            </li>
            <li className="text-xs leading-relaxed text-[#666666]">
              주문한 상품별로 리뷰 작성이 가능하며, 동일 상품 여러개 구매시 최소 1회의 한해 포인트
              적립
            </li>
            <li className="text-xs leading-relaxed text-[#666666]">
              리뷰 삭제 후 재작성시 포인트 미지급
            </li>
          </ul>
        </div>
      </BorderedSection>
      <BorderedSection>
        <div className="p-[15px]">
          <AppSubmitButton
            onClick={handleSubmit}
            disabled={isUploading}
            isSubmitting={isSubmitting}
            loadingText="등록 중..."
          >
            등록하기
          </AppSubmitButton>
        </div>
      </BorderedSection>
    </SectionStack>
  )
}
