'use client'

import { RankInfoModal } from '@/components/modals/RankInfoModal'
import { useState } from 'react'
import { MdInfo } from 'react-icons/md'

export default function RankInfoModalButton() {
  const [showRankInfoModal, setShowRankInfoModal] = useState(false)

  return (
    <>
      <MdInfo
        size="18"
        color="#dddddd"
        onClick={() => setShowRankInfoModal(true)}
        className="cursor-pointer"
      />
      <RankInfoModal open={showRankInfoModal} onOpenChange={setShowRankInfoModal} />
    </>
  )
}
