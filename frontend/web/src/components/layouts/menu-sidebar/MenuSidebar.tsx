'use client'

import IconLink from '@/components/ui/IconLink'
import { Sidebar, SidebarContent, SidebarHeader, useSidebar } from '@/components/ui/shadcn/sidebar'
import { PAGE_PATHS } from '@/lib/paths'
import Icon from '@/components/ui/Icon'
import { useEffect, useState } from 'react'
import MenuSidebarBanners from './MenuSidebarBanners'
import MenuSidebarFoodTypes from './MenuSidebarFoodTypes'
import MenuSidebarProfile from './MenuSidebarProfile'

interface Props {
  isLoggedIn: boolean
}

function MenuSidebarContent({ isLoggedIn }: Props) {
  const { setOpenMobile } = useSidebar()

  return (
    <>
      <SidebarHeader className="gap-0 p-0">
        <div className="flex items-center justify-end gap-[18px] p-[15px]">
          <IconLink href={PAGE_PATHS.SETTING} className="w-[18px] h-[18px]">
            <Icon name="setting-black" />
          </IconLink>
          <IconLink href={PAGE_PATHS.SEARCH} className="w-[18px] h-[18px]">
            <Icon name="search" />
          </IconLink>
          <button
            onClick={() => setOpenMobile(false)}
            className="flex items-center justify-center w-[18px] h-[18px]"
          >
            <Icon name="close" />
          </button>
        </div>
      </SidebarHeader>
      <SidebarContent className="p-0 flex flex-col overflow-y-auto">
        <MenuSidebarProfile isLoggedIn={isLoggedIn} />
        <MenuSidebarFoodTypes />
        <MenuSidebarBanners />
      </SidebarContent>
    </>
  )
}

export default function MenuSidebar({ isLoggedIn }: Props) {
  const { open, openMobile } = useSidebar()
  const [hasOpened, setHasOpened] = useState(false)

  useEffect(() => {
    if (open || openMobile) {
      setHasOpened(true)
    }
  }, [open, openMobile])

  return (
    <Sidebar side="left" collapsible="offcanvas">
      {hasOpened && <MenuSidebarContent isLoggedIn={isLoggedIn} />}
    </Sidebar>
  )
}
