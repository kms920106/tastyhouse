'use client'

import { consentReviewBlindDeleteAction, rejectReviewBlindDeleteAction } from '@/actions/review'
import AppOutlineButton from '@/components/ui/AppOutlineButton'
import AppButton from '@/components/ui/AppButton'
import AppConfirmDialog from '@/components/ui/AppConfirmDialog'
import { toast } from '@/components/ui/AppToaster'
import { REVIEW_BLIND_CONSENT_COPY, REVIEW_BLIND_ERROR_CODE } from '@/domains/review'
import { PAGE_PATHS } from '@/lib/paths'
import { useRouter } from 'next/navigation'
import { useState, useTransition } from 'react'

interface Props {
  reviewId: number
}

/**
 * 게시중단 리뷰의 삭제 동의/거부 버튼.
 *
 * web은 `react-hook-form`을 쓰지 않으므로 state + Server Action 직접 호출로 처리한다.
 * 동의는 되돌릴 수 없어 `AppConfirmDialog`로 한 번 더 확인한다.
 */
export default function ReviewBlindConsentActions({ reviewId }: Props) {
  const router = useRouter()
  const [isPending, startTransition] = useTransition()
  const [confirmOpen, setConfirmOpen] = useState(false)

  const handleConsent = () => {
    startTransition(async () => {
      const { error, errorCode } = await consentReviewBlindDeleteAction(reviewId)

      if (errorCode === REVIEW_BLIND_ERROR_CODE.REVIEW_BLIND_REQUEST_NOT_APPROVED) {
        toast(REVIEW_BLIND_CONSENT_COPY.NOT_APPROVED)
        router.replace(PAGE_PATHS.NOTIFICATIONS)
        return
      }

      if (error) {
        toast(REVIEW_BLIND_CONSENT_COPY.SUBMIT_FAILED)
        return
      }

      // 리뷰가 삭제됐으므로 상세로 돌아갈 수 없다 — 목록으로 보낸다.
      toast(REVIEW_BLIND_CONSENT_COPY.CONSENT_SUCCESS)
      router.replace(PAGE_PATHS.REVIEWS)
    })
  }

  const handleReject = () => {
    startTransition(async () => {
      const { error, errorCode } = await rejectReviewBlindDeleteAction(reviewId)

      if (errorCode === REVIEW_BLIND_ERROR_CODE.REVIEW_BLIND_REQUEST_NOT_APPROVED) {
        toast(REVIEW_BLIND_CONSENT_COPY.NOT_APPROVED)
        router.replace(PAGE_PATHS.NOTIFICATIONS)
        return
      }

      if (error) {
        toast(REVIEW_BLIND_CONSENT_COPY.SUBMIT_FAILED)
        return
      }

      toast(REVIEW_BLIND_CONSENT_COPY.REJECT_SUCCESS)
      router.replace(PAGE_PATHS.NOTIFICATIONS)
    })
  }

  return (
    <>
      <div className="flex gap-2.5">
        <AppOutlineButton
          type="button"
          className="flex-1 w-auto"
          disabled={isPending}
          onClick={handleReject}
        >
          {REVIEW_BLIND_CONSENT_COPY.REJECT_BUTTON}
        </AppOutlineButton>
        <AppButton
          type="button"
          variant="destructive"
          className="flex-1 h-[50px] text-sm leading-[14px] text-white border-0 disabled:cursor-not-allowed disabled:opacity-50"
          disabled={isPending}
          onClick={() => setConfirmOpen(true)}
        >
          {REVIEW_BLIND_CONSENT_COPY.CONSENT_BUTTON}
        </AppButton>
      </div>

      <AppConfirmDialog
        open={confirmOpen}
        onOpenChange={setConfirmOpen}
        title={REVIEW_BLIND_CONSENT_COPY.CONFIRM_TITLE}
        description={REVIEW_BLIND_CONSENT_COPY.CONFIRM_DESCRIPTION}
        confirmLabel={REVIEW_BLIND_CONSENT_COPY.CONFIRM_LABEL}
        cancelLabel={REVIEW_BLIND_CONSENT_COPY.CANCEL_LABEL}
        onConfirm={handleConsent}
      />
    </>
  )
}
