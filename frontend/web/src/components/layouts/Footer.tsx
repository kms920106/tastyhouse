'use client'

import Icon from '@/components/ui/Icon'
import { getLayoutTabIcon, type LayoutTabKey } from '@/components/ui/icon-helpers'
import { PAGE_PATHS } from '@/lib/paths'
import { cn } from '@/lib/utils'
import Link from 'next/link'
import { usePathname } from 'next/navigation'

interface FooterNavigationItem {
  label: string
  href: string
  tabKey: LayoutTabKey
}

const FOOTER_NAVIGATION: FooterNavigationItem[] = [
  { label: '홈', href: PAGE_PATHS.HOME, tabKey: 'home' },
  { label: '랭킹', href: PAGE_PATHS.RANKS, tabKey: 'rank' },
  { label: '리뷰', href: PAGE_PATHS.REVIEWS, tabKey: 'review' },
  { label: '플레이스', href: PAGE_PATHS.PLACES, tabKey: 'place' },
  { label: '마이페이지', href: PAGE_PATHS.MY_PAGE, tabKey: 'mypage' },
]

export default function Footer() {
  const pathname = usePathname()

  const isActive = (href: string) => {
    if (href === '/') {
      return pathname === '/'
    }
    return pathname.startsWith(href)
  }

  return (
    <footer
      className="fixed bottom-0 left-0 right-0 w-full bg-white border border-line box-border z-50"
      role="navigation"
      aria-label="Main Navigation"
    >
      <nav className="h-[70px]">
        <ul className="flex items-center justify-around h-full">
          {FOOTER_NAVIGATION.map((item) => {
            const active = isActive(item.href)
            return (
              <li key={item.href} className="flex-1">
                <Link
                  href={item.href}
                  className="flex flex-col items-center justify-center gap-2.5 h-full"
                  aria-current={active ? 'page' : undefined}
                >
                  <div className="relative w-6 h-6 flex items-center justify-center">
                    <Icon name={getLayoutTabIcon(item.tabKey, active)} fill style={{ objectFit: 'contain' }} />
                  </div>
                  <span
                    className={cn(
                      'text-[10px] leading-[10px] transition-colors duration-200',
                      active ? 'text-main' : 'text-[#aaaaaa]',
                    )}
                  >
                    {item.label}
                  </span>
                </Link>
              </li>
            )
          })}
        </ul>
      </nav>
    </footer>
  )
}
