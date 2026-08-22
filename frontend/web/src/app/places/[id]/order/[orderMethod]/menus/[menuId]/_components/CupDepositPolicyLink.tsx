'use client'

import { fetchCupDepositPolicyContent } from '@/actions/policies'
import { toast } from '@/components/ui/AppToaster'
import AppTermsDialog from '@/components/ui/AppTermsDialog'
import { useState } from 'react'

const DIALOG_TITLE = '일회용컵 보증금 안내'

/**
 * 옵션 선택 화면의 '자세히' 링크. 신규 엔드포인트를 만들지 않고 기존 정책문서(POLICY_DOCUMENT)
 * 조회 경로를 그대로 재사용한다 — auth 회원가입 약관 다이얼로그와 동일한 fetch-on-click 패턴.
 */
export default function CupDepositPolicyLink() {
  const [open, setOpen] = useState(false)
  const [htmlContent, setHtmlContent] = useState('')

  const handleOpen = async () => {
    try {
      const content = await fetchCupDepositPolicyContent()
      setHtmlContent(content)
      setOpen(true)
    } catch {
      toast('안내 내용을 불러오는데 실패했습니다.')
    }
  }

  return (
    <>
      <button
        type="button"
        onClick={handleOpen}
        className="text-xs leading-[12px] text-[#aaaaaa] underline cursor-pointer"
      >
        자세히
      </button>
      <AppTermsDialog open={open} onOpenChange={setOpen} title={DIALOG_TITLE}>
        <div dangerouslySetInnerHTML={{ __html: htmlContent }} />
      </AppTermsDialog>
    </>
  )
}
