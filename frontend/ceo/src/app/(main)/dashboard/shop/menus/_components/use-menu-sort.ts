"use client";

import * as React from "react";

import { useRouter } from "next/navigation";

import type { DragEndEvent, DragStartEvent } from "@dnd-kit/core";
import { PointerSensor, useSensor, useSensors } from "@dnd-kit/core";
import { arrayMove } from "@dnd-kit/sortable";
import { toast } from "sonner";

import {
  changeMenuCategoryOrderAction,
  changeMenuOrderAction,
  moveMenuCategoryAction,
} from "@/feature/product/actions";
import type { MenuBoardGroup } from "@/feature/product/domain";
import { PRODUCT_MENU_COPY, PRODUCT_MENU_MESSAGE } from "@/feature/product/message";

/**
 * 드래그 항목 식별자.
 *
 * `DndContext` 는 문자열/숫자 id 하나로만 항목을 구분하므로, **그룹과 메뉴가 같은 id 공간을
 * 공유하면 서로를 잘못 집는다**(그룹 3번과 메뉴 3번). 접두사로 갈래를 구분한다.
 * 미분류 그룹은 `categoryId` 가 null 이라 별도 토큰을 쓴다 — 문자열 `"null"` 로 뭉뚱그리면
 * 이름이 `null` 인 실제 그룹과 충돌할 여지가 있어 전용 상수를 둔다.
 */
const GROUP_ID_PREFIX = "group:";
const MENU_ID_PREFIX = "menu:";
const UNCATEGORIZED_TOKEN = "uncategorized";

export function toGroupDragId(categoryId: number | null): string {
  return `${GROUP_ID_PREFIX}${categoryId ?? UNCATEGORIZED_TOKEN}`;
}

export function toMenuDragId(productId: number): string {
  return `${MENU_ID_PREFIX}${productId}`;
}

function parseGroupDragId(dragId: string): { categoryId: number | null } | null {
  if (!dragId.startsWith(GROUP_ID_PREFIX)) return null;
  const raw = dragId.slice(GROUP_ID_PREFIX.length);
  return { categoryId: raw === UNCATEGORIZED_TOKEN ? null : Number(raw) };
}

function parseMenuDragId(dragId: string): number | null {
  if (!dragId.startsWith(MENU_ID_PREFIX)) return null;
  return Number(dragId.slice(MENU_ID_PREFIX.length));
}

/**
 * 서버가 stale 요청을 거부했음을 뜻하는 에러코드.
 *
 * 다른 탭에서 메뉴를 추가·삭제한 뒤 이 화면에서 드래그하면 요청 id 집합이 서버의 현재 집합과
 * 어긋난다. 사용자가 고칠 수 있는 상황이 아니라 **목록을 다시 받아야** 하는 상황이므로,
 * 실패 문구 대신 새로고침을 안내하고 `router.refresh()` 로 목록을 되살린다.
 */
const SORT_STALE_MESSAGES: readonly string[] = [
  PRODUCT_MENU_MESSAGE.ORDER_CHANGE_FAILED,
  PRODUCT_MENU_MESSAGE.CATEGORY_MOVE_FAILED,
];

/**
 * 서버가 내려준 문구로 stale 을 판정한다.
 *
 * 액션이 errorCode 를 그대로 넘겨주지 않고 한국어 `message` 만 반환하므로(이 앱은 프론트에서
 * errorCode → 문구 맵을 만들지 않는다), 백엔드가 `PRODUCT_ORDER_TARGET_MISMATCH` /
 * `PRODUCT_CATEGORY_ORDER_TARGET_MISMATCH` 에 붙여 둔 "최신 상태와 일치하지 않습니다" 문구를
 * 부분 일치로 알아본다(`docs/tasks/backend.md` §1-4).
 */
const SORT_STALE_MESSAGE_MARKER = "최신 상태와 일치하지 않습니다";

function isSortStale(message: string | undefined): boolean {
  if (message === undefined) return false;
  if (SORT_STALE_MESSAGES.includes(message)) return false;
  return message.includes(SORT_STALE_MESSAGE_MARKER);
}

interface UseMenuSortOptions {
  shopId?: number;
  /** 서버가 내려준 확정 목록. 액션이 실패하면 미리보기를 이 값으로 되돌린다 */
  groups: MenuBoardGroup[];
}

/**
 * 메뉴판 2단 드래그 정렬.
 *
 * **낙관적 업데이트를 하지 않는다.** 순서는 손님 화면에 즉시 반영되는 값이라, 서버가 거부한
 * 배치를 화면에 남겨 두면 점주가 반영된 것으로 오인한다(품절·숨김 화면이 세운 선례).
 * 대신 **드래그 중에는 로컬 미리보기 배열을 보여주고**, 액션이 끝나면 미리보기를 버려
 * `revalidatePath` 로 갱신된 서버 상태를 그대로 그린다 — 실패하면 되돌리는 코드가 따로 필요 없이
 * 미리보기를 버리는 것만으로 원복된다.
 *
 * **`sort` 숫자를 계산해 보내지 않는다.** 확정된 id 배열만 보내고 서버가 인덱스로 0..N-1 로
 * 정규화한다(`docs/tasks/backend.md` §4) — "sort 충돌"이라는 개념 자체를 없애는 설계다.
 */
export function useMenuSort({ shopId, groups }: UseMenuSortOptions) {
  const router = useRouter();
  const [isSorting, startTransition] = React.useTransition();

  // 드래그가 확정된 뒤 서버 응답이 오기 전까지 보여줄 배치. null 이면 서버 상태를 그대로 쓴다.
  const [preview, setPreview] = React.useState<MenuBoardGroup[] | null>(null);
  const [activeDragId, setActiveDragId] = React.useState<string | null>(null);

  // 서버 목록이 갱신되면(revalidate 완료) 미리보기를 버린다 — 그대로 두면 실패한 배치가
  // 화면에 눌러앉아 새로고침 전까지 사라지지 않는다.
  // biome-ignore lint/correctness/useExhaustiveDependencies: groups 참조가 바뀐 시점에만 버린다
  React.useEffect(() => {
    setPreview(null);
  }, [groups]);

  const displayGroups = preview ?? groups;

  /**
   * 체크박스·버튼과 드래그를 한 행에 함께 두기 위한 활성화 거리.
   *
   * 8px 이동 전에는 드래그로 보지 않아, 손잡이가 아닌 곳을 눌렀을 때의 클릭이 살아난다.
   */
  const sensors = useSensors(useSensor(PointerSensor, { activationConstraint: { distance: 8 } }));

  const reportFailure = React.useCallback(
    (message: string | undefined, fallback: string) => {
      if (isSortStale(message)) {
        toast.error(PRODUCT_MENU_COPY.SORT_STALE_TITLE, { description: PRODUCT_MENU_COPY.SORT_STALE_DESCRIPTION });
        router.refresh();
        return;
      }

      toast.error(message ?? fallback);
    },
    [router],
  );

  const handleDragStart = React.useCallback((event: DragStartEvent) => {
    setActiveDragId(String(event.active.id));
  }, []);

  const handleDragEnd = React.useCallback(
    (event: DragEndEvent) => {
      setActiveDragId(null);

      const { active, over } = event;
      if (shopId === undefined || over === null) return;

      const activeId = String(active.id);
      const overId = String(over.id);
      if (activeId === overId) return;

      const activeGroup = parseGroupDragId(activeId);

      // ===== 그룹끼리 순서 변경 =====
      if (activeGroup !== null) {
        const overGroup = parseGroupDragId(overId);
        if (overGroup === null) return;

        const fromIndex = groups.findIndex((group) => group.categoryId === activeGroup.categoryId);
        const toIndex = groups.findIndex((group) => group.categoryId === overGroup.categoryId);
        if (fromIndex < 0 || toIndex < 0) return;

        const nextGroups = arrayMove(groups, fromIndex, toIndex);
        setPreview(nextGroups);

        // 미분류 그룹(categoryId === null)은 실체가 있는 카테고리가 아니라 서버가 정렬 대상으로
        // 받지 않는다 — id 배열에서 빼고 보낸다.
        const orderedCategoryIds = nextGroups
          .map((group) => group.categoryId)
          .filter((categoryId): categoryId is number => categoryId !== null);

        startTransition(async () => {
          const result = await changeMenuCategoryOrderAction(shopId, orderedCategoryIds);
          if (!result.success) {
            setPreview(null);
            reportFailure(result.message, PRODUCT_MENU_MESSAGE.ORDER_CHANGE_FAILED);
          }
        });
        return;
      }

      // ===== 메뉴 이동 =====
      const activeProductId = parseMenuDragId(activeId);
      if (activeProductId === null) return;

      const sourceGroupIndex = groups.findIndex((group) =>
        group.products.some((product) => product.id === activeProductId),
      );
      if (sourceGroupIndex < 0) return;

      // 놓은 대상은 다른 메뉴이거나(그 자리에 끼워 넣기) 빈 그룹의 드롭 영역이다(끝에 붙이기).
      const overProductId = parseMenuDragId(overId);
      const overGroupTarget = parseGroupDragId(overId);

      const targetGroupIndex =
        overProductId !== null
          ? groups.findIndex((group) => group.products.some((product) => product.id === overProductId))
          : overGroupTarget !== null
            ? groups.findIndex((group) => group.categoryId === overGroupTarget.categoryId)
            : -1;
      if (targetGroupIndex < 0) return;

      const sourceGroup = groups[sourceGroupIndex];
      const targetGroup = groups[targetGroupIndex];

      // ----- 같은 그룹 내 순서 변경 -----
      if (sourceGroupIndex === targetGroupIndex) {
        const fromIndex = sourceGroup.products.findIndex((product) => product.id === activeProductId);
        const toIndex =
          overProductId !== null
            ? sourceGroup.products.findIndex((product) => product.id === overProductId)
            : sourceGroup.products.length - 1;
        if (fromIndex < 0 || toIndex < 0 || fromIndex === toIndex) return;

        const nextProducts = arrayMove(sourceGroup.products, fromIndex, toIndex);
        setPreview(
          groups.map((group, index) => (index === sourceGroupIndex ? { ...group, products: nextProducts } : group)),
        );

        startTransition(async () => {
          const result = await changeMenuOrderAction(
            shopId,
            sourceGroup.categoryId,
            nextProducts.map((product) => product.id),
          );
          if (!result.success) {
            setPreview(null);
            reportFailure(result.message, PRODUCT_MENU_MESSAGE.ORDER_CHANGE_FAILED);
          }
        });
        return;
      }

      // ----- 다른 그룹으로 이동 -----
      const movedProduct = sourceGroup.products.find((product) => product.id === activeProductId);
      if (movedProduct === undefined) return;

      const nextSourceProducts = sourceGroup.products.filter((product) => product.id !== activeProductId);

      // 놓은 위치를 그대로 살린다. 메뉴 위에 놓았으면 그 앞자리, 그룹 영역에 놓았으면 맨 끝이다.
      const dropIndex =
        overProductId !== null
          ? targetGroup.products.findIndex((product) => product.id === overProductId)
          : targetGroup.products.length;
      const insertIndex = dropIndex < 0 ? targetGroup.products.length : dropIndex;

      const nextTargetProducts = [
        ...targetGroup.products.slice(0, insertIndex),
        movedProduct,
        ...targetGroup.products.slice(insertIndex),
      ];

      setPreview(
        groups.map((group, index) =>
          index === sourceGroupIndex
            ? { ...group, products: nextSourceProducts }
            : index === targetGroupIndex
              ? { ...group, products: nextTargetProducts }
              : group,
        ),
      );

      startTransition(async () => {
        // **`targetOrderedProductIds` 를 반드시 함께 보낸다.** 이동 대상 id 만 보내면 서버가
        // 도착 그룹의 맨 끝에 append 해 사용자가 놓은 위치가 무시된다(`backend.md` §4-3).
        const result = await moveMenuCategoryAction(
          shopId,
          targetGroup.categoryId,
          [activeProductId],
          nextTargetProducts.map((product) => product.id),
        );
        if (!result.success) {
          setPreview(null);
          reportFailure(result.message, PRODUCT_MENU_MESSAGE.CATEGORY_MOVE_FAILED);
        }
      });
    },
    [groups, reportFailure, shopId],
  );

  const handleDragCancel = React.useCallback(() => {
    setActiveDragId(null);
  }, []);

  return {
    sensors,
    displayGroups,
    activeDragId,
    isSorting,
    handleDragStart,
    handleDragEnd,
    handleDragCancel,
  };
}
