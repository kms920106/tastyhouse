import type { IconName } from './icon-registry'
import type { OrderMethodType } from '@/domains/order'

export type LayoutTabKey = 'home' | 'rank' | 'review' | 'place' | 'mypage'

export type MemberGradeIconCode = '01' | '02' | '03' | '04' | '05'

export function getLayoutTabIcon(tabKey: LayoutTabKey, active: boolean): IconName {
  return `layout/${tabKey}-${active ? 'on' : 'off'}` as IconName
}

export type MemberGradeIconSize = 40 | 120

export function getMemberGradeIconNameBySize(
  code: MemberGradeIconCode,
  size: MemberGradeIconSize,
): IconName {
  return `rank/level-${code}-${size}` as IconName
}

export function getMemberGradeIconName(code: MemberGradeIconCode): IconName {
  return getMemberGradeIconNameBySize(code, 40)
}

export type RankTopNo = 1 | 2 | 3

export function getRankPodiumIconName(rankNo: RankTopNo): IconName {
  return `rank/rank-0${rankNo}` as IconName
}

const ORDER_METHOD_ICON_SLUG: Record<OrderMethodType, 'table' | 'reservation' | 'delivery' | 'packaging'> = {
  TABLE: 'table',
  RESERVATION: 'reservation',
  DELIVERY:    'delivery',
  TAKEOUT:     'packaging',
}

export function getOrderMethodIconName(
  method: OrderMethodType,
  active: boolean,
): IconName {
  return `place/order-method/${ORDER_METHOD_ICON_SLUG[method]}-${active ? 'on' : 'off'}` as IconName
}
