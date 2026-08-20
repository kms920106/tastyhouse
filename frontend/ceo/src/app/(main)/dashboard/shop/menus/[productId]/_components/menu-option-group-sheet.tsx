"use client";

import * as React from "react";

import {
  closestCenter,
  DndContext,
  type DragEndEvent,
  KeyboardSensor,
  PointerSensor,
  useSensor,
  useSensors,
} from "@dnd-kit/core";
import { restrictToParentElement, restrictToVerticalAxis } from "@dnd-kit/modifiers";
import {
  arrayMove,
  SortableContext,
  sortableKeyboardCoordinates,
  useSortable,
  verticalListSortingStrategy,
} from "@dnd-kit/sortable";
import { CSS } from "@dnd-kit/utilities";
import { GripVertical } from "lucide-react";
import { toast } from "sonner";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";
import {
  Dialog,
  DialogClose,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { FieldLabel } from "@/components/ui/field";
import {
  Sheet,
  SheetClose,
  SheetContent,
  SheetDescription,
  SheetFooter,
  SheetHeader,
  SheetTitle,
} from "@/components/ui/sheet";
import {
  changeLinkedOptionGroupOrderAction,
  linkOptionGroupAction,
  unlinkOptionGroupAction,
} from "@/feature/product/actions";
import type { LinkedProductSummary, MenuOptionGroup } from "@/feature/product/domain";
import {
  OPTION_GROUP_SCREEN_COPY,
  PRODUCT_DETAIL_COPY,
  PRODUCT_DETAIL_SCREEN_COPY,
  PRODUCT_MENU_MESSAGE,
} from "@/feature/product/message";

interface MenuOptionGroupSheetProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  productId: number;
  shopId: number;
  /** 가게의 전체 옵션그룹. 연결 후보 목록의 원본이다 */
  optionGroups: MenuOptionGroup[];
  /** 그룹별 "이 그룹을 쓰는 메뉴 목록"(§5-2). 연결 여부 판정과 해제 영향 안내에 함께 쓴다 */
  linkedProductsByGroupId: Record<number, LinkedProductSummary[]>;
}

function SortableGroupRow({
  group,
  otherLinkedCount,
  isLastLink,
  disabled,
  onUnlink,
}: {
  group: MenuOptionGroup;
  otherLinkedCount: number;
  isLastLink: boolean;
  disabled: boolean;
  onUnlink: (group: MenuOptionGroup) => void;
}) {
  const { attributes, listeners, setNodeRef, transform, transition, isDragging } = useSortable({ id: group.id });

  return (
    <div
      ref={setNodeRef}
      style={{ transform: CSS.Transform.toString(transform), transition }}
      className="flex flex-col gap-1 border-b py-3 last:border-b-0"
      data-dragging={isDragging}
    >
      <div className="flex items-center gap-3">
        <button
          type="button"
          className="cursor-grab text-muted-foreground disabled:cursor-not-allowed"
          aria-label={PRODUCT_DETAIL_SCREEN_COPY.OPTION_GROUP_DRAG_HANDLE_LABEL}
          disabled={disabled}
          {...attributes}
          {...listeners}
        >
          <GripVertical className="size-4" />
        </button>
        <span className="min-w-0 flex-1 truncate text-sm">{group.name}</span>
        {group.required && <Badge variant="outline">{OPTION_GROUP_SCREEN_COPY.BADGE_REQUIRED}</Badge>}
        <Button
          type="button"
          size="sm"
          variant="outline"
          disabled={disabled || isLastLink}
          onClick={() => onUnlink(group)}
        >
          {PRODUCT_DETAIL_COPY.OPTION_GROUP_UNLINK}
        </Button>
      </div>
      <span className="pl-7 text-muted-foreground text-xs leading-snug">
        {isLastLink
          ? PRODUCT_DETAIL_COPY.OPTION_GROUP_LAST_LINK_NOTICE
          : otherLinkedCount > 0
            ? `${PRODUCT_DETAIL_COPY.OPTION_GROUP_LINKED_COUNT_PREFIX}${otherLinkedCount}${PRODUCT_DETAIL_COPY.OPTION_GROUP_LINKED_COUNT_SUFFIX}`
            : PRODUCT_DETAIL_SCREEN_COPY.OPTION_GROUP_ONLY_LINK}
      </span>
    </div>
  );
}

export function MenuOptionGroupSheet({
  open,
  onOpenChange,
  productId,
  shopId,
  optionGroups,
  linkedProductsByGroupId,
}: MenuOptionGroupSheetProps) {
  const [isPending, startTransition] = React.useTransition();
  const [linkDialogOpen, setLinkDialogOpen] = React.useState(false);
  const [selectedGroupIds, setSelectedGroupIds] = React.useState<number[]>([]);

  const sensors = useSensors(
    useSensor(PointerSensor),
    useSensor(KeyboardSensor, { coordinateGetter: sortableKeyboardCoordinates }),
  );

  // 이 메뉴에 연결된 그룹은 "그룹의 연결 메뉴 목록에 이 메뉴가 있는가"로 판정한다 —
  // 메뉴 상세 응답에 연결 목록이 없어서(백엔드 스펙 공백) 역조회가 유일한 경로다.
  const initialLinkedGroups = React.useMemo(
    () =>
      optionGroups.filter((group) =>
        (linkedProductsByGroupId[group.id] ?? []).some((product) => product.id === productId),
      ),
    [optionGroups, linkedProductsByGroupId, productId],
  );

  const [linkedGroups, setLinkedGroups] = React.useState<MenuOptionGroup[]>(initialLinkedGroups);

  // 서버 데이터가 갱신되면(revalidate 후 재렌더) 로컬 순서를 서버 순서로 되돌린다.
  React.useEffect(() => {
    setLinkedGroups(initialLinkedGroups);
  }, [initialLinkedGroups]);

  React.useEffect(() => {
    if (!linkDialogOpen) setSelectedGroupIds([]);
  }, [linkDialogOpen]);

  const linkedGroupIds = new Set(linkedGroups.map((group) => group.id));
  const linkableGroups = optionGroups.filter((group) => !linkedGroupIds.has(group.id));

  function handleDragEnd(event: DragEndEvent) {
    const { active, over } = event;
    if (over === null || active.id === over.id) return;

    const oldIndex = linkedGroups.findIndex((group) => group.id === active.id);
    const newIndex = linkedGroups.findIndex((group) => group.id === over.id);
    if (oldIndex < 0 || newIndex < 0) return;

    // 드래그 중 미리보기는 로컬 배열로 보여주되 낙관적 업데이트는 하지 않는다.
    // 실패하면 서버 데이터로 되돌린다(위 effect 가 초기값을 다시 밀어 넣는다).
    const preview = arrayMove(linkedGroups, oldIndex, newIndex);
    setLinkedGroups(preview);

    startTransition(async () => {
      // `sort` 를 계산해 보내지 않고 확정된 id 배열만 보낸다 — 서버가 0..N-1 로 정규화한다.
      const { success, message } = await changeLinkedOptionGroupOrderAction(
        productId,
        shopId,
        preview.map((group) => group.id),
      );
      if (success) {
        toast.success(PRODUCT_MENU_MESSAGE.ORDER_CHANGE_SUCCESS);
      } else {
        toast.error(message ?? PRODUCT_MENU_MESSAGE.ORDER_CHANGE_FAILED);
        setLinkedGroups(initialLinkedGroups);
      }
    });
  }

  function handleUnlink(group: MenuOptionGroup) {
    startTransition(async () => {
      const { success, message } = await unlinkOptionGroupAction(productId, group.id, shopId);
      if (success) {
        toast.success(PRODUCT_MENU_MESSAGE.OPTION_GROUP_UNLINK_SUCCESS);
        setLinkedGroups((previous) => previous.filter((item) => item.id !== group.id));
      } else {
        // 마지막 연결 해제는 서버가 `PRODUCT_OPTION_GROUP_LAST_LINK_CANNOT_UNLINK` 로 막는다.
        // 버튼도 비활성화해 두지만 최종 판정은 서버이므로 서버 문구를 그대로 노출한다.
        toast.error(message ?? PRODUCT_MENU_MESSAGE.OPTION_GROUP_UNLINK_FAILED);
      }
    });
  }

  function handleLink() {
    if (selectedGroupIds.length === 0) return;

    startTransition(async () => {
      // 연결은 그룹당 한 번씩 호출한다 — 일괄 엔드포인트가 없고, 한 건이 실패해도
      // 나머지는 반영돼야 하므로 순차로 돌리고 실패만 모아 알린다.
      const failures: string[] = [];
      for (const optionGroupId of selectedGroupIds) {
        const { success, message } = await linkOptionGroupAction(productId, optionGroupId, shopId);
        if (!success) failures.push(message ?? PRODUCT_MENU_MESSAGE.OPTION_GROUP_LINK_FAILED);
      }

      if (failures.length > 0) {
        toast.error(failures[0]);
      } else {
        toast.success(PRODUCT_MENU_MESSAGE.OPTION_GROUP_LINK_SUCCESS);
      }

      const linkedNow = optionGroups.filter(
        (group) => linkedGroupIds.has(group.id) || selectedGroupIds.includes(group.id),
      );
      setLinkedGroups(linkedNow);
      setLinkDialogOpen(false);
    });
  }

  return (
    <>
      <Sheet open={open} onOpenChange={onOpenChange}>
        <SheetContent className="flex w-full flex-col sm:max-w-md">
          <SheetHeader>
            <SheetTitle>{PRODUCT_DETAIL_COPY.SHEET_OPTION_GROUP_TITLE}</SheetTitle>
            <SheetDescription>{PRODUCT_DETAIL_COPY.OPTION_GROUP_LINK_DIALOG_DESCRIPTION}</SheetDescription>
          </SheetHeader>

          <div className="flex flex-1 flex-col gap-3 overflow-y-auto px-4">
            <Button
              type="button"
              size="sm"
              variant="outline"
              className="w-fit"
              disabled={isPending || linkableGroups.length === 0}
              onClick={() => setLinkDialogOpen(true)}
            >
              {PRODUCT_DETAIL_COPY.OPTION_GROUP_LINK}
            </Button>

            {linkedGroups.length === 0 ? (
              <span className="py-4 text-muted-foreground text-sm">{PRODUCT_DETAIL_COPY.OPTION_GROUP_EMPTY}</span>
            ) : (
              <DndContext
                sensors={sensors}
                collisionDetection={closestCenter}
                modifiers={[restrictToVerticalAxis, restrictToParentElement]}
                onDragEnd={handleDragEnd}
              >
                <SortableContext items={linkedGroups.map((group) => group.id)} strategy={verticalListSortingStrategy}>
                  {linkedGroups.map((group) => {
                    const linkedProducts = linkedProductsByGroupId[group.id] ?? [];
                    const otherLinkedCount = linkedProducts.filter((product) => product.id !== productId).length;
                    // 연결이 0건이 되면 어디서도 보이지 않는 고아 그룹이 되므로 마지막 연결은 막는다.
                    // 그룹을 지우려면 옵션그룹 관리 화면에서 삭제해야 한다.
                    const isLastLink = otherLinkedCount === 0;

                    return (
                      <SortableGroupRow
                        key={group.id}
                        group={group}
                        otherLinkedCount={otherLinkedCount}
                        isLastLink={isLastLink}
                        disabled={isPending}
                        onUnlink={handleUnlink}
                      />
                    );
                  })}
                </SortableContext>
              </DndContext>
            )}
          </div>

          <SheetFooter>
            <SheetClose asChild>
              <Button variant="outline" disabled={isPending}>
                {PRODUCT_DETAIL_SCREEN_COPY.BUTTON_CANCEL}
              </Button>
            </SheetClose>
          </SheetFooter>
        </SheetContent>
      </Sheet>

      <Dialog open={linkDialogOpen} onOpenChange={setLinkDialogOpen}>
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle>{PRODUCT_DETAIL_COPY.OPTION_GROUP_LINK_DIALOG_TITLE}</DialogTitle>
            <DialogDescription>{PRODUCT_DETAIL_COPY.OPTION_GROUP_LINK_DIALOG_DESCRIPTION}</DialogDescription>
          </DialogHeader>

          <div className="flex max-h-80 flex-col gap-3 overflow-y-auto">
            {linkableGroups.length === 0 ? (
              <span className="text-muted-foreground text-sm">{PRODUCT_DETAIL_COPY.OPTION_GROUP_LINK_EMPTY}</span>
            ) : (
              linkableGroups.map((group) => (
                <div key={group.id} className="flex items-center gap-2">
                  <Checkbox
                    id={`menu-option-group-link-${group.id}`}
                    checked={selectedGroupIds.includes(group.id)}
                    onCheckedChange={(checked) =>
                      setSelectedGroupIds((previous) =>
                        checked === true ? [...previous, group.id] : previous.filter((id) => id !== group.id),
                      )
                    }
                    disabled={isPending}
                  />
                  <FieldLabel htmlFor={`menu-option-group-link-${group.id}`} className="font-normal">
                    {group.name}
                  </FieldLabel>
                </div>
              ))
            )}
          </div>

          <DialogFooter>
            <Button type="button" onClick={handleLink} disabled={isPending || selectedGroupIds.length === 0}>
              {`${PRODUCT_DETAIL_COPY.OPTION_GROUP_LINK} (${selectedGroupIds.length}${PRODUCT_DETAIL_SCREEN_COPY.OPTION_GROUP_SELECTED_SUFFIX})`}
            </Button>
            <DialogClose asChild>
              <Button variant="outline" disabled={isPending}>
                {PRODUCT_DETAIL_SCREEN_COPY.BUTTON_CANCEL}
              </Button>
            </DialogClose>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </>
  );
}
