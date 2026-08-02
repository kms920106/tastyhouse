'use client'

import { logout } from '@/actions/auth'
import AppConfirmDialog from '@/components/ui/AppConfirmDialog'
import { useState } from 'react'
import { ArrowIcon } from './icons'
import { menuItemLabelClassName, menuItemRowClassName } from './styles'

export default function LogoutButton() {
  const [logoutDialogOpen, setLogoutDialogOpen] = useState(false)

  const handleLogoutConfirm = async () => {
    await logout()
  }

  return (
    <>
      <button
        onClick={() => setLogoutDialogOpen(true)}
        className={`w-full cursor-pointer ${menuItemRowClassName}`}
      >
        <span className={menuItemLabelClassName}>로그아웃</span>
        <ArrowIcon />
      </button>
      <AppConfirmDialog
        open={logoutDialogOpen}
        onOpenChange={setLogoutDialogOpen}
        title="로그아웃 하시겠습니까?"
        description={`현재 계정에서 로그아웃됩니다.\n계속 진행하시겠습니까?`}
        onConfirm={handleLogoutConfirm}
      />
    </>
  )
}
