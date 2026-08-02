'use client'

import { createOrderReview } from '@/actions/review'
import OrderProductItem from '@/components/orders/OrderProductItem'
import AppSubmitButton from '@/components/ui/AppSubmitButton'
import { toast } from '@/components/ui/AppToaster'
import BorderedSection from '@/components/ui/BorderedSection'
import SectionStack from '@/components/ui/SectionStack'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import { extractZodFieldErrors } from '@/lib/form'
import { useRouter } from 'next/navigation'
import { useCallback, useState, useTransition } from 'react'
import { z } from 'zod'
import ReviewContentSection from './ReviewContentSection'
import ReviewRatingSection from './ReviewRatingSection'

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
})

type FormData = z.infer<typeof reviewSchema>

type FormErrors = Partial<Record<'ratings' | 'content', string>>

const INITIAL_FORM_DATA: FormData = {
  ratings: { taste: 0, amount: 0, price: 0 },
  content: '',
  tags: [],
}

interface Props {
  orderProductId: number
  productId: number
  productName: string
  productImageUrl: string
  productPrice: number
}

export default function OrderReviewCreateForm({
  orderProductId,
  productId,
  productName,
  productImageUrl,
  productPrice,
}: Props) {
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
