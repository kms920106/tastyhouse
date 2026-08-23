"use client";

import { useDroppable } from "@dnd-kit/core";
import { SortableContext, useSortable, verticalListSortingStrategy } from "@dnd-kit/sortable";
import { CSS } from "@dnd-kit/utilities";
import { GripVertical, PackageX } from "lucide-react";

import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";
import { Empty, EmptyDescription, EmptyHeader, EmptyMedia, EmptyTitle } from "@/components/ui/empty";
import type { MenuBoardGroup, MenuBoardRow } from "@/feature/product/domain";
import {
  PRODUCT_AVAILABILITY_COPY,
  PRODUCT_IMPORT_COPY,
  PRODUCT_MENU_COPY,
  PRODUCT_MESSAGE,
} from "@/feature/product/message";
import { cn } from "@/lib/utils";

import { MenuRow } from "./menu-row";
import { toGroupDragId, toMenuDragId } from "./use-menu-sort";

interface MenuGroupListProps {
  /** 조회 실패 시 undefined — 셸(가게 선택기·버튼)은 살아있어야 하므로 목록만 안내로 대체한다 */
  groups?: MenuBoardGroup[];
  errorMessage?: string;
  selectedIds: ReadonlySet<number>;
  disabled?: boolean;
  onSelectionChange: (next: ReadonlySet<number>) => void;
  onEditGroup: (group: MenuBoardGroup) => void;
  onDeleteGroup: (group: MenuBoardGroup) => void;
  onOpenDetail: (productId: number) => void;
  /** 메뉴판에서 제외(링크 해제) */
  onExcludeMenu: (row: MenuBoardRow) => void;
  /** 그 그룹으로 다른 가게 메뉴를 불러온다. 미분류 그룹은 대상이 아니다 */
  onImportMenus: (group: MenuBoardGroup) => void;
}

export function MenuGroupList({
  groups,
  errorMessage,
  selectedIds,
  disabled,
  onSelectionChange,
  onEditGroup,
  onDeleteGroup,
  onOpenDetail,
  onExcludeMenu,
  onImportMenus,
}: MenuGroupListProps) {
  if (groups === undefined) {
    return (
      <Empty>
        <EmptyHeader>
          <EmptyMedia variant="icon">
            <PackageX />
          </EmptyMedia>
          <EmptyTitle>{PRODUCT_MESSAGE.LOAD_FAILED}</EmptyTitle>
          {errorMessage && <EmptyDescription>{errorMessage}</EmptyDescription>}
        </EmptyHeader>
      </Empty>
    );
  }

  if (groups.length === 0) {
    return (
      <Empty>
        <EmptyHeader>
          <EmptyMedia variant="icon">
            <PackageX />
          </EmptyMedia>
          <EmptyTitle>{PRODUCT_MENU_COPY.EMPTY_GROUPS}</EmptyTitle>
          <EmptyDescription>{PRODUCT_MENU_COPY.EMPTY_GROUPS_DESCRIPTION}</EmptyDescription>
        </EmptyHeader>
      </Empty>
    );
  }

  function toggleRow(id: number, checked: boolean) {
    const next = new Set(selectedIds);
    if (checked) next.add(id);
    else next.delete(id);
    onSelectionChange(next);
  }

  /** 그룹 헤더 체크박스는 하위 전체를 한 번에 선택/해제한다 */
  function toggleGroup(rows: MenuBoardRow[], checked: boolean) {
    const next = new Set(selectedIds);
    for (const row of rows) {
      if (checked) next.add(row.id);
      else next.delete(row.id);
    }
    onSelectionChange(next);
  }

  return (
    <SortableContext
      items={groups.map((group) => toGroupDragId(group.categoryId))}
      strategy={verticalListSortingStrategy}
    >
      <div className="flex flex-col gap-4">
        {groups.map((group) => (
          <MenuGroupCard
            key={toGroupDragId(group.categoryId)}
            group={group}
            selectedIds={selectedIds}
            disabled={disabled}
            onToggleGroup={(checked) => toggleGroup(group.products, checked)}
            onToggleRow={toggleRow}
            onEditGroup={() => onEditGroup(group)}
            onDeleteGroup={() => onDeleteGroup(group)}
            onOpenDetail={onOpenDetail}
            onExcludeMenu={onExcludeMenu}
            onImportMenus={() => onImportMenus(group)}
          />
        ))}
      </div>
    </SortableContext>
  );
}

interface MenuGroupCardProps {
  group: MenuBoardGroup;
  selectedIds: ReadonlySet<number>;
  disabled?: boolean;
  onToggleGroup: (checked: boolean) => void;
  onToggleRow: (id: number, checked: boolean) => void;
  onEditGroup: () => void;
  onDeleteGroup: () => void;
  onOpenDetail: (productId: number) => void;
  onExcludeMenu: (row: MenuBoardRow) => void;
  onImportMenus: () => void;
}

function MenuGroupCard({
  group,
  selectedIds,
  disabled,
  onToggleGroup,
  onToggleRow,
  onEditGroup,
  onDeleteGroup,
  onOpenDetail,
  onExcludeMenu,
  onImportMenus,
}: MenuGroupCardProps) {
  const dragId = toGroupDragId(group.categoryId);

  // 미분류 그룹은 실체가 있는 카테고리가 아니라 정렬·수정·삭제 대상이 아니다 —
  // 서버의 카테고리 순서 API 가 받지 못하는 값이라 조작 자체를 막는다.
  const isUncategorized = group.categoryId === null;

  const { attributes, listeners, setNodeRef, transform, transition, isDragging } = useSortable({
    id: dragId,
    disabled: disabled === true || isUncategorized,
  });

  /**
   * 빈 그룹에도 메뉴를 떨어뜨릴 수 있어야 한다.
   *
   * `SortableContext` 는 항목이 없으면 충돌 대상이 되지 않아, 빈 그룹은 드롭을 받지 못한다 —
   * 그러면 메뉴를 새 그룹으로 옮길 방법이 사라진다. 그룹 자체를 droppable 로 등록해
   * 그룹 영역에 놓으면 맨 끝에 붙도록 한다(`use-menu-sort.ts` 의 `overGroupTarget` 갈래).
   */
  const { setNodeRef: setDroppableRef, isOver } = useDroppable({ id: dragId, disabled });

  const selectedInGroup = group.products.filter((row) => selectedIds.has(row.id)).length;
  // 하위가 일부만 선택된 상태는 indeterminate 로 표시한다.
  const groupChecked =
    selectedInGroup === 0 ? false : selectedInGroup === group.products.length ? true : ("indeterminate" as const);

  return (
    <section
      ref={setNodeRef}
      style={{ transform: CSS.Translate.toString(transform), transition }}
      className={cn(
        "bg-background flex flex-col rounded-md border p-4",
        isDragging && "relative z-10 opacity-60 shadow-md",
        isOver && "ring-primary/40 ring-2",
      )}
    >
      <div className="flex flex-wrap items-center gap-2 border-b pb-3">
        <button
          type="button"
          className="text-muted-foreground hover:text-foreground cursor-grab touch-none disabled:cursor-not-allowed disabled:opacity-30"
          aria-label={PRODUCT_MENU_COPY.DRAG_HANDLE_LABEL}
          disabled={disabled === true || isUncategorized}
          {...attributes}
          {...listeners}
        >
          <GripVertical className="size-4" />
        </button>

        <Checkbox
          id={`menu-board-group-${dragId}`}
          checked={groupChecked}
          disabled={disabled === true || group.products.length === 0}
          onCheckedChange={(checked) => onToggleGroup(checked === true)}
        />
        <label htmlFor={`menu-board-group-${dragId}`} className="text-sm font-medium">
          {group.categoryName ?? PRODUCT_AVAILABILITY_COPY.NO_CATEGORY}
        </label>
        <span className="text-muted-foreground text-sm">
          {group.products.length}
          {PRODUCT_MENU_COPY.GROUP_MENU_COUNT_SUFFIX}
        </span>

        {/* 미분류는 수정·삭제할 실체가 없다 — 버튼 자체를 두지 않는다. */}
        {!isUncategorized && (
          <div className="ml-auto flex items-center gap-2">
            <Button type="button" size="sm" variant="outline" disabled={disabled} onClick={onEditGroup}>
              {PRODUCT_MENU_COPY.BUTTON_EDIT}
            </Button>
            <Button type="button" size="sm" variant="outline" disabled={disabled} onClick={onDeleteGroup}>
              {PRODUCT_MENU_COPY.BUTTON_DELETE}
            </Button>
          </div>
        )}
      </div>

      <div ref={setDroppableRef} className="min-h-12">
        <SortableContext
          items={group.products.map((product) => toMenuDragId(product.id))}
          strategy={verticalListSortingStrategy}
        >
          {group.products.length === 0 ? (
            <p className="text-muted-foreground py-4 text-sm">{PRODUCT_MENU_COPY.EMPTY_MENUS_IN_GROUP}</p>
          ) : (
            group.products.map((row) => (
              <MenuRow
                key={row.id}
                row={row}
                checked={selectedIds.has(row.id)}
                disabled={disabled}
                onCheckedChange={(checked) => onToggleRow(row.id, checked)}
                onOpenDetail={() => onOpenDetail(row.id)}
                onExclude={() => onExcludeMenu(row)}
              />
            ))
          )}
        </SortableContext>
      </div>

      {/* 미분류는 불러온 메뉴를 넣을 실체가 없다 — 서버가 메뉴그룹 id 를 요구하므로 버튼을 두지 않는다 */}
      {!isUncategorized && (
        <div className="border-t pt-3">
          <Button type="button" size="sm" variant="outline" disabled={disabled} onClick={onImportMenus}>
            {PRODUCT_IMPORT_COPY.TRIGGER}
          </Button>
        </div>
      )}
    </section>
  );
}
