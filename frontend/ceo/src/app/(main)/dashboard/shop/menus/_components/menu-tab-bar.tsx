"use client";

import { useRouter } from "next/navigation";

import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { MENU_TABS, type MenuTab } from "@/feature/product/constants";
import { MENU_TAB_COPY } from "@/feature/product/message";

interface MenuTabBarProps {
  /** 현재 열려 있는 탭. 화면이 스스로 알고 넘긴다 — URL 파싱을 두 번 하지 않는다 */
  activeTab: MenuTab;
  shopId?: number;
  disabled?: boolean;
}

/**
 * 메뉴 탭 / 옵션 탭 전환.
 *
 * PDF 요구서 10건 전부가 *"메뉴·옵션 관리 → 옵션 탭"* 을 전제하는데 두 화면이 별 라우트로 갈려
 * 있어 조작 흐름이 어긋났다. **탭은 진입 경로를 하나로 모으는 용도**이고 실체는 여전히 두 라우트다
 * (`/option-groups` 는 합치기 화면의 부모라 유지한다).
 *
 * `availability-filter-bar.tsx` 의 `Tabs` + searchParam 패턴을 따르지만, 여기서는 탭이 라우트를
 * 가르므로 URL 파라미터가 아니라 `router.push` 로 경로를 바꾼다.
 */
export function MenuTabBar({ activeTab, shopId, disabled }: MenuTabBarProps) {
  const router = useRouter();

  function handleChange(nextTab: string) {
    if (nextTab === activeTab) return;

    const query = shopId === undefined ? "" : `?shopId=${shopId}`;
    router.push(
      nextTab === MENU_TABS.OPTION ? `/dashboard/shop/menus/option-groups${query}` : `/dashboard/shop/menus${query}`,
    );
  }

  return (
    <Tabs value={activeTab} onValueChange={handleChange}>
      <TabsList>
        <TabsTrigger value={MENU_TABS.MENU} disabled={disabled}>
          {MENU_TAB_COPY.TAB_MENU}
        </TabsTrigger>
        <TabsTrigger value={MENU_TABS.OPTION} disabled={disabled}>
          {MENU_TAB_COPY.TAB_OPTION}
        </TabsTrigger>
      </TabsList>
    </Tabs>
  );
}
