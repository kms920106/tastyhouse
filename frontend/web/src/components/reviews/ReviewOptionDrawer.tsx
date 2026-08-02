'use client'

import {
  Drawer,
  DrawerClose,
  DrawerContent,
  DrawerDescription,
  DrawerTitle,
  DrawerTrigger,
} from '@/components/ui/shadcn/drawer'
import { useMyProfile } from '@/domains/member/member.hook'
import { PAGE_PATHS } from '@/lib/paths'
import { copyToClipboard, share } from '@/lib/share'
import Link from 'next/link'
import { useCallback } from 'react'
import { toast } from '../ui/AppToaster'
import ReviewOptionButton from './ReviewOptionButton'

interface Props {
  reviewId: number
  memberId: number
  memberNickname: string
  content: string
}

export default function ReviewOptionDrawer({ reviewId, memberId, memberNickname, content }: Props) {
  const { memberProfile } = useMyProfile()

  const currentMemberId = memberProfile?.id ?? null
  const isMyReview = currentMemberId !== null && currentMemberId === memberId

  const getShareUrl = useCallback(() => {
    return `${window.location.origin}${PAGE_PATHS.REVIEW_DETAIL(reviewId)}`
  }, [reviewId])

  const handleShare = useCallback(async () => {
    await share({
      title: `${memberNickname}님의 리뷰`,
      text: content.slice(0, 100),
      url: getShareUrl(),
    })
  }, [memberNickname, content, getShareUrl])

  const handleCopyLink = useCallback(async () => {
    const success = await copyToClipboard(getShareUrl())
    if (success) {
      toast('링크가 복사되었습니다.')
    }
  }, [getShareUrl])

  return (
    <Drawer autoFocus>
      <DrawerTrigger asChild>
        <ReviewOptionButton />
      </DrawerTrigger>
      <DrawerContent className="bg-transparent p-[15px] border-none">
        <DrawerTitle className="sr-only">리뷰 옵션</DrawerTitle>
        <DrawerDescription className="sr-only">신고, 공유, 링크 복사</DrawerDescription>
        <div className="text-center bg-white rounded-[14px]">
          {isMyReview && (
            <>
              <DrawerClose asChild>
                <Link
                  href={PAGE_PATHS.REVIEW_EDIT(reviewId)}
                  className="block w-full py-[20.5px] text-sm leading-[14px]"
                >
                  수정
                </Link>
              </DrawerClose>
              <div className="h-px bg-[#f6f6f6]" />
            </>
          )}
          <DrawerClose asChild>
            <button className="w-full py-[20.5px] text-sm leading-[14px]" onClick={handleCopyLink}>
              링크 복사
            </button>
          </DrawerClose>
          <div className="h-px bg-[#f6f6f6]" />
          <DrawerClose asChild>
            <button className="w-full py-[20.5px] text-sm leading-[14px]" onClick={handleShare}>
              공유
            </button>
          </DrawerClose>
          <div className="h-px bg-[#f6f6f6]" />
          <DrawerClose asChild>
            <button className="w-full py-[20.5px] text-sm leading-[14px]">신고</button>
          </DrawerClose>
          <div className="h-px bg-[#f6f6f6]" />
          <DrawerClose asChild>
            <button className="w-full py-[20.5px] text-sm leading-[14px]">차단</button>
          </DrawerClose>
        </div>
      </DrawerContent>
    </Drawer>
  )
}
