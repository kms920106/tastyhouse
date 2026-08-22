import { Fingerprint, KeyRound, type LucideIcon, PowerOff, ShieldCheck, Store, UtensilsCrossed } from "lucide-react";

export interface NavSubItem {
  title: string;
  url: string;
  icon?: LucideIcon;
  comingSoon?: boolean;
  newTab?: boolean;
  isNew?: boolean;
}

export interface NavMainItem {
  title: string;
  url: string;
  icon?: LucideIcon;
  subItems?: NavSubItem[];
  comingSoon?: boolean;
  newTab?: boolean;
  isNew?: boolean;
}

export interface NavGroup {
  id: number;
  label?: string;
  items: NavMainItem[];
}

export const sidebarItems: NavGroup[] = [
  {
    id: 1,
    label: "Pages",
    items: [
      {
        title: "가게 관리",
        url: "/dashboard/shop",
        icon: Store,
      },
      {
        // 이 화면은 사이드바에 없어 URL 이나 메뉴판 내부 링크로만 도달할 수 있었다.
        title: "메뉴·옵션 관리",
        url: "/dashboard/shop/menus",
        icon: UtensilsCrossed,
      },
      {
        title: "전체현황·임시중지",
        url: "/dashboard/shop-status",
        icon: PowerOff,
      },
      {
        title: "Authentication",
        url: "/auth",
        icon: Fingerprint,
        subItems: [
          { title: "Login v1", url: "/auth/v1/login", newTab: true },
          { title: "Login v2", url: "/auth/v2/login", newTab: true },
          { title: "Register v1", url: "/auth/v1/register", newTab: true },
          { title: "Register v2", url: "/auth/v2/register", newTab: true },
        ],
      },
    ],
  },
  {
    id: 2,
    label: "내 계정",
    items: [
      {
        title: "개인정보 접속기록",
        url: "/dashboard/account/login-history",
        icon: KeyRound,
      },
      {
        title: "시스템 접근권한 이력",
        url: "/dashboard/account/shop-access-history",
        icon: ShieldCheck,
      },
    ],
  },
];
