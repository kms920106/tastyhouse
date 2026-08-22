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
import { GripVertical, Pencil, Trash2 } from "lucide-react";

import { Button } from "@/components/ui/button";
import type { MenuOption } from "@/feature/product/domain";
import { formatPrice } from "@/feature/product/format";
import { OPTION_GROUP_SCREEN_COPY, PRODUCT_OPTION_GROUP_COPY } from "@/feature/product/message";

interface OptionListProps {
  options: MenuOption[];
  disabled?: boolean;
  onEdit: (option: MenuOption) => void;
  onDelete: (option: MenuOption) => void;
  /** 드롭 후 확정된 id 배열. `sort` 숫자는 서버가 인덱스로 정규화하므로 계산하지 않는다 */
  onReorder: (optionIds: number[]) => void;
}

interface SortableOptionRowProps {
  option: MenuOption;
  disabled?: boolean;
  onEdit: () => void;
  onDelete: () => void;
}

/**
 * 보증금 관련 요약 문구.
 *
 * 개인컵 옵션은 컵을 제공하지 않아 `cupCount` 가 비고 할인 금액만 있다 — 두 값 중 채워진 쪽으로
 * 갈래를 판별한다(서버 모델도 같은 방식이다). 일반 옵션은 둘 다 비어 있어 `undefined` 다.
 */
function toDepositSummary(option: MenuOption): string | undefined {
  if (option.personalCupDiscountAmount !== null) {
    return PRODUCT_OPTION_GROUP_COPY.OPTION_PERSONAL_CUP_SUMMARY(option.personalCupDiscountAmount);
  }

  if (option.cupCount !== null && option.depositAmount !== null) {
    return PRODUCT_OPTION_GROUP_COPY.OPTION_DEPOSIT_SUMMARY(option.cupCount, option.depositAmount);
  }

  return undefined;
}

function SortableOptionRow({ option, disabled, onEdit, onDelete }: SortableOptionRowProps) {
  const { attributes, listeners, setNodeRef, transform, transition, isDragging } = useSortable({
    id: option.id,
    disabled,
  });

  const depositSummary = toDepositSummary(option);

  return (
    <li
      ref={setNodeRef}
      style={{ transform: CSS.Transform.toString(transform), transition }}
      data-dragging={isDragging}
      className="flex items-center gap-2 border-b py-2 last:border-b-0 data-[dragging=true]:opacity-60"
    >
      {/* 손잡이에만 리스너를 건다 — 행 전체가 드래그되면 [수정]/[삭제] 클릭이 드래그로 먹힌다. */}
      <button
        type="button"
        aria-label={OPTION_GROUP_SCREEN_COPY.DRAG_HANDLE_LABEL}
        className="cursor-grab touch-none text-muted-foreground disabled:cursor-not-allowed"
        disabled={disabled}
        {...attributes}
        {...listeners}
      >
        <GripVertical className="size-4" />
      </button>

      <div className="flex min-w-0 flex-1 flex-col">
        <span className="truncate text-sm">{option.name}</span>
        {/* 보증금·개인컵 할인은 추가금과 다른 축이라 가격 옆이 아니라 옵션명 아래 별 줄로 붙인다. */}
        {depositSummary !== undefined && <span className="text-muted-foreground text-xs">{depositSummary}</span>}
      </div>
      <span className="text-muted-foreground text-sm">{formatPrice(option.additionalPrice)}</span>

      <Button type="button" variant="ghost" size="sm" disabled={disabled} onClick={onEdit}>
        <Pencil />
        {OPTION_GROUP_SCREEN_COPY.BUTTON_EDIT_OPTION}
      </Button>
      <Button type="button" variant="ghost" size="sm" disabled={disabled} onClick={onDelete}>
        <Trash2 />
        {OPTION_GROUP_SCREEN_COPY.BUTTON_DELETE_OPTION}
      </Button>
    </li>
  );
}

export function OptionList({ options, disabled, onEdit, onDelete, onReorder }: OptionListProps) {
  /**
   * 드래그 미리보기용 로컬 배열.
   *
   * **낙관적 업데이트가 아니다.** 서버 응답을 기다리는 동안 손을 뗀 위치를 보여주기만 하고,
   * 액션이 성공하면 `revalidatePath` 로 새 `options` 가 내려와 아래 동기화 effect 가 이 로컬
   * 배열을 버린다. 실패하면 서버 상태가 그대로이므로 같은 effect 가 원래 순서로 되돌린다.
   */
  const [preview, setPreview] = React.useState<MenuOption[] | null>(null);

  // `options` 참조가 바뀌면(= 서버 데이터 갱신) 미리보기를 폐기해 서버 상태를 진실로 삼는다.
  // biome-ignore lint/correctness/useExhaustiveDependencies: options 참조 변경 자체가 신호다
  React.useEffect(() => {
    setPreview(null);
  }, [options]);

  const rows = preview ?? options;

  const sensors = useSensors(
    useSensor(PointerSensor, { activationConstraint: { distance: 4 } }),
    useSensor(KeyboardSensor, { coordinateGetter: sortableKeyboardCoordinates }),
  );

  function handleDragEnd(event: DragEndEvent) {
    const { active, over } = event;
    if (!over || active.id === over.id) return;

    const from = rows.findIndex((row) => row.id === active.id);
    const to = rows.findIndex((row) => row.id === over.id);
    if (from === -1 || to === -1) return;

    const next = arrayMove(rows, from, to);
    setPreview(next);
    onReorder(next.map((row) => row.id));
  }

  if (rows.length === 0) {
    return (
      <p className="rounded-md border border-dashed p-4 text-center text-muted-foreground text-sm">
        {PRODUCT_OPTION_GROUP_COPY.EMPTY_OPTIONS}
      </p>
    );
  }

  return (
    <DndContext
      sensors={sensors}
      collisionDetection={closestCenter}
      modifiers={[restrictToVerticalAxis, restrictToParentElement]}
      onDragEnd={handleDragEnd}
    >
      <SortableContext items={rows.map((row) => row.id)} strategy={verticalListSortingStrategy}>
        <ul className="flex flex-col">
          {rows.map((option) => (
            <SortableOptionRow
              key={option.id}
              option={option}
              disabled={disabled}
              onEdit={() => onEdit(option)}
              onDelete={() => onDelete(option)}
            />
          ))}
        </ul>
      </SortableContext>
    </DndContext>
  );
}
