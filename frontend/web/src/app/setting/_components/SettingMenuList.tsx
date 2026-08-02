import BorderedSection from '@/components/ui/BorderedSection'
import SectionStack from '@/components/ui/SectionStack'
import { PAGE_PATHS } from '@/lib/paths'
import Link from 'next/link'
import LogoutButton from './LogoutButton'
import { ArrowIcon } from './icons'
import { menuItemLabelClassName, menuItemRowClassName } from './styles'

interface MenuItemRowProps {
  id: string
  label: string
  href: string
}

function MenuItemRow({ label, href }: MenuItemRowProps) {
  return (
    <Link href={href} className={menuItemRowClassName}>
      <span className={menuItemLabelClassName}>{label}</span>
      <ArrowIcon />
    </Link>
  )
}

interface MenuGroup {
  id: string
  items: MenuItemRowProps[]
}

const publicMenuGroups: MenuGroup[] = [
  {
    id: 'benefits',
    items: [
      { id: 'coupons', label: '쿠폰', href: PAGE_PATHS.COUPONS },
      { id: 'events', label: '이벤트', href: PAGE_PATHS.EVENTS },
      { id: 'point', label: '포인트 내역', href: PAGE_PATHS.POINT },
      { id: 'grade', label: '등급안내', href: PAGE_PATHS.GRADE },
    ],
  },
  {
    id: 'support',
    items: [
      { id: 'notices', label: '공지사항', href: PAGE_PATHS.NOTICES },
      { id: 'faqs', label: '자주하는 질문', href: PAGE_PATHS.FAQS },
      { id: 'customer-center', label: '고객센터', href: PAGE_PATHS.CUSTOMER_CENTER },
      { id: 'advertising', label: '광고 및 제휴', href: PAGE_PATHS.ADVERTISING },
      { id: 'bug-reports', label: '버그 제보', href: PAGE_PATHS.BUG_REPORTS },
    ],
  },
  {
    id: 'legal',
    items: [
      { id: 'terms', label: '이용약관', href: PAGE_PATHS.TERMS },
      { id: 'privacy', label: '개인정보취급방침', href: PAGE_PATHS.PRIVACY },
    ],
  },
]

const authMenuGroups: MenuGroup[] = [
  {
    id: 'account',
    items: [
      { id: 'account-info', label: '개인정보 수정', href: PAGE_PATHS.ACCOUNT_INFO },
      { id: 'password', label: '비밀번호 변경', href: PAGE_PATHS.ACCOUNT_PASSWORD },
    ],
  },
]

interface Props {
  isLoggedIn: boolean
}

export default function SettingMenuList({ isLoggedIn }: Props) {
  const menuGroups = isLoggedIn ? [...publicMenuGroups, ...authMenuGroups] : publicMenuGroups

  return (
    <SectionStack>
      {menuGroups.map((group) => (
        <BorderedSection key={group.id}>
          <div className="divide-y divide-line">
            {group.items.map((item) => (
              <MenuItemRow key={item.id} {...item} />
            ))}
          </div>
        </BorderedSection>
      ))}
      <BorderedSection>
        {isLoggedIn ? (
          <LogoutButton />
        ) : (
          <MenuItemRow id="login" label="로그인" href={PAGE_PATHS.AUTH_LOGIN} />
        )}
      </BorderedSection>
    </SectionStack>
  )
}
