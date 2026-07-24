import {
  Banknote,
  Bug,
  Calendar,
  CalendarDays,
  ChartBar,
  Fingerprint,
  Forklift,
  GalleryHorizontal,
  Gauge,
  GraduationCap,
  Handshake,
  HelpCircle,
  Kanban,
  LayoutDashboard,
  ListTodo,
  Lock,
  type LucideIcon,
  Mail,
  Megaphone,
  MessageSquare,
  Package,
  ReceiptText,
  ShoppingBag,
  Soup,
  SquareArrowUpRight,
  Star,
  Store,
  TicketPercent,
  Trophy,
  UserRound,
  Users,
} from "lucide-react";

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
    label: "Dashboards",
    items: [
      {
        title: "Default",
        url: "/dashboard/default",
        icon: LayoutDashboard,
      },
      {
        title: "CRM",
        url: "/dashboard/crm",
        icon: ChartBar,
      },
      {
        title: "Finance",
        url: "/dashboard/finance",
        icon: Banknote,
      },
      {
        title: "Analytics",
        url: "/dashboard/analytics",
        icon: Gauge,
      },
      {
        title: "Productivity",
        url: "/dashboard/productivity",
        icon: ListTodo,
      },
      {
        title: "E-commerce",
        url: "/dashboard/ecommerce",
        icon: ShoppingBag,
      },
      {
        title: "Academy",
        url: "/dashboard/academy",
        icon: GraduationCap,
        isNew: true,
      },
      {
        title: "Logistics",
        url: "/dashboard/logistics",
        icon: Forklift,
      },
    ],
  },
  {
    id: 2,
    label: "Pages",
    items: [
      {
        title: "Email",
        url: "/dashboard/mail",
        icon: Mail,
      },
      {
        title: "Chat",
        url: "/dashboard/chat",
        icon: MessageSquare,
      },
      {
        title: "Calendar",
        url: "/dashboard/coming-soon",
        icon: Calendar,
        comingSoon: true,
      },
      {
        title: "Kanban",
        url: "/dashboard/kanban",
        icon: Kanban,
      },
      {
        title: "Invoice",
        url: "/dashboard/invoice",
        icon: ReceiptText,
      },
      {
        title: "공지사항",
        url: "/dashboard/notices",
        icon: Megaphone,
      },
      {
        title: "FAQ",
        url: "/dashboard/faqs",
        icon: HelpCircle,
      },
      {
        title: "배너",
        url: "/dashboard/banners",
        icon: GalleryHorizontal,
      },
      {
        title: "쿠폰",
        url: "/dashboard/coupons",
        icon: TicketPercent,
      },
      {
        title: "이벤트",
        url: "/dashboard/events",
        icon: CalendarDays,
      },
      {
        title: "회원",
        url: "/dashboard/members",
        icon: UserRound,
      },
      {
        title: "주문",
        url: "/dashboard/orders",
        icon: Package,
      },
      {
        title: "가게",
        url: "/dashboard/shops",
        icon: Store,
      },
      {
        title: "상품",
        url: "/dashboard/products",
        icon: Soup,
      },
      {
        title: "랭킹",
        url: "/dashboard/ranks",
        icon: Trophy,
      },
      {
        title: "리뷰",
        url: "/dashboard/reviews",
        icon: Star,
      },
      {
        title: "제휴 신청",
        url: "/dashboard/partnership-requests",
        icon: Handshake,
      },
      {
        title: "버그 제보",
        url: "/dashboard/bug-reports",
        icon: Bug,
      },
      {
        title: "Users",
        url: "/dashboard/users",
        icon: Users,
      },
      {
        title: "Roles",
        url: "/dashboard/roles",
        icon: Lock,
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
    id: 3,
    label: "Legacy",
    items: [
      {
        title: "Dashboards",
        url: "/dashboard/default-v1",
        subItems: [
          { title: "Default V1", url: "/dashboard/default-v1" },
          { title: "CRM V1", url: "/dashboard/crm-v1" },
          { title: "Finance V1", url: "/dashboard/finance-v1" },
          { title: "Analytics V1", url: "/dashboard/analytics-v1" },
        ],
      },
    ],
  },
  {
    id: 4,
    label: "Misc",
    items: [
      {
        title: "Others",
        url: "/dashboard/coming-soon",
        icon: SquareArrowUpRight,
        comingSoon: true,
      },
    ],
  },
];
