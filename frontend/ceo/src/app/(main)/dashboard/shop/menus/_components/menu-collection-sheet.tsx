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
import { ChevronRight, GripVertical, Plus, X } from "lucide-react";
import { toast } from "sonner";

import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Collapsible, CollapsibleContent, CollapsibleTrigger } from "@/components/ui/collapsible";
import {
  Sheet,
  SheetClose,
  SheetContent,
  SheetDescription,
  SheetFooter,
  SheetHeader,
  SheetTitle,
} from "@/components/ui/sheet";
import { Skeleton } from "@/components/ui/skeleton";
import {
  changeMenuCollectionImageOrderAction,
  deleteMenuCollectionImageAction,
  loadMenuCollectionImagesAction,
  requestMenuCollectionImageAction,
} from "@/feature/shop/actions";
import { MENU_COLLECTION_IMAGE_ACCEPT, MENU_COLLECTION_MAX_COUNT } from "@/feature/shop/constants";
import type { MenuCollectionImage } from "@/feature/shop/domain";
import { SHOP_MENU_COLLECTION_COPY, SHOP_MENU_COLLECTION_MESSAGE } from "@/feature/shop/message";

import { ShopImagePreview } from "../../_components/shop-image-preview";

interface MenuCollectionSheetProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  shopId: number;
  /** 서버에서 받은 초기 목록. 시트가 열리면 곧바로 재조회해 확정한다 */
  initialImages?: MenuCollectionImage[];
}

/** 검수 상태 뱃지. `REJECTED` 는 사유를 함께 보여줘야 하므로 카드 본문에서 따로 다룬다 */
function StatusBadge({ status }: { status: MenuCollectionImage["status"] }) {
  if (status === "PENDING") return <Badge variant="secondary">{SHOP_MENU_COLLECTION_COPY.BADGE_PENDING}</Badge>;
  if (status === "APPROVED") return <Badge variant="outline">{SHOP_MENU_COLLECTION_COPY.BADGE_APPROVED}</Badge>;
  if (status === "CANCELED") return <Badge variant="outline">{SHOP_MENU_COLLECTION_COPY.BADGE_CANCELED}</Badge>;
  return <Badge variant="destructive">{SHOP_MENU_COLLECTION_COPY.BADGE_REJECTED}</Badge>;
}

function SortableImageCard({
  image,
  index,
  disabled,
  deleteDisabled,
  onDelete,
}: {
  image: MenuCollectionImage;
  index: number;
  disabled: boolean;
  deleteDisabled: boolean;
  onDelete: (imageId: number) => void;
}) {
  const { attributes, listeners, setNodeRef, transform, transition, isDragging } = useSortable({ id: image.id });

  return (
    <div
      ref={setNodeRef}
      style={{ transform: CSS.Transform.toString(transform), transition }}
      className="flex items-start gap-3 border-b py-3 last:border-b-0"
      data-dragging={isDragging}
    >
      <button
        type="button"
        className="mt-5 cursor-grab text-muted-foreground disabled:cursor-not-allowed"
        aria-label={SHOP_MENU_COLLECTION_COPY.DRAG_HANDLE_LABEL}
        disabled={disabled}
        {...attributes}
        {...listeners}
      >
        <GripVertical className="size-4" />
      </button>

      <ShopImagePreview
        src={image.imageUrl}
        alt={`${SHOP_MENU_COLLECTION_COPY.IMAGE_ALT_PREFIX}${index + 1}`}
        className="size-20 shrink-0"
      />

      <div className="flex min-w-0 flex-1 flex-col gap-1">
        <div className="flex items-center gap-2">
          <span className="text-muted-foreground text-sm">{index + 1}</span>
          <StatusBadge status={image.status} />
        </div>
        {image.status === "REJECTED" && image.rejectReason !== null && (
          <span className="text-destructive text-xs leading-snug">
            {`${SHOP_MENU_COLLECTION_COPY.REJECT_REASON_PREFIX}${image.rejectReason}`}
          </span>
        )}
      </div>

      {/* 마지막 1개는 삭제할 수 없다 — 배너가 비면 손님 화면의 최상단이 사라진다 */}
      <Button
        type="button"
        size="sm"
        variant="ghost"
        disabled={disabled || deleteDisabled}
        aria-label={SHOP_MENU_COLLECTION_COPY.ACTION_DELETE}
        onClick={() => onDelete(image.id)}
      >
        <X className="size-4" />
      </Button>
    </div>
  );
}

export function MenuCollectionSheet({ open, onOpenChange, shopId, initialImages }: MenuCollectionSheetProps) {
  const [isPending, startTransition] = React.useTransition();
  const [isLoading, setIsLoading] = React.useState(false);
  const [images, setImages] = React.useState<MenuCollectionImage[]>(initialImages ?? []);
  const [deleteTargetId, setDeleteTargetId] = React.useState<number | null>(null);
  const fileInputRef = React.useRef<HTMLInputElement>(null);

  const sensors = useSensors(
    useSensor(PointerSensor),
    useSensor(KeyboardSensor, { coordinateGetter: sortableKeyboardCoordinates }),
  );

  const reload = React.useCallback(async () => {
    setIsLoading(true);
    const { success, message, data } = await loadMenuCollectionImagesAction(shopId);
    setIsLoading(false);

    if (!success || !data) {
      toast.error(message ?? SHOP_MENU_COLLECTION_COPY.LOAD_FAILED);
      return;
    }

    setImages(data);
  }, [shopId]);

  React.useEffect(() => {
    if (open) void reload();
  }, [open, reload]);

  function handleFileChange(event: React.ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0] ?? null;
    event.target.value = ""; // 같은 파일 재선택 허용
    if (file === null) return;

    const formData = new FormData();
    formData.append("file", file);

    startTransition(async () => {
      // 규격(1280×960·15MB·JPG/PNG)은 화면이 **안내만** 하고 판정은 서버가 한다 —
      // 브라우저에서 해상도를 재도 서버가 `ImageIO` 로 다시 보므로 두 판정이 어긋날 수 있다.
      // 서버가 `..._SPEC_INVALID` 로 거절하면 그 한국어 문구를 그대로 노출하고 목록에 넣지 않는다.
      const { success, message } = await requestMenuCollectionImageAction(shopId, images.length, formData);
      if (!success) {
        toast.error(message ?? SHOP_MENU_COLLECTION_MESSAGE.SPEC_INVALID);
        return;
      }
      toast.success(SHOP_MENU_COLLECTION_MESSAGE.UPLOAD_SUCCESS);
      await reload();
    });
  }

  function handleDragEnd(event: DragEndEvent) {
    const { active, over } = event;
    if (over === null || active.id === over.id) return;

    const oldIndex = images.findIndex((image) => image.id === active.id);
    const newIndex = images.findIndex((image) => image.id === over.id);
    if (oldIndex < 0 || newIndex < 0) return;

    // 드래그 중에는 로컬 배열로 미리보기를 보여주되 낙관적 업데이트는 하지 않는다 —
    // 서버 응답 후 다시 읽어 확정하고, 실패하면 서버 상태로 되돌린다.
    const preview = arrayMove(images, oldIndex, newIndex);
    setImages(preview);

    startTransition(async () => {
      // `sort` 숫자를 계산해 보내지 않는다. 확정된 id 배열 전량을 보내고 서버가 0..N-1 로 정규화한다.
      // 다른 탭에서 목록이 바뀌었으면 서버가 `..._ORDER_TARGET_MISMATCH` 로 거절하므로 재조회로 맞춘다.
      const { success, message } = await changeMenuCollectionImageOrderAction(
        shopId,
        preview.map((image) => image.id),
      );
      if (!success) {
        toast.error(message ?? SHOP_MENU_COLLECTION_COPY.ORDER_MISMATCH);
      } else {
        toast.success(SHOP_MENU_COLLECTION_MESSAGE.ORDER_SUCCESS);
      }
      await reload();
    });
  }

  function handleConfirmDelete() {
    if (deleteTargetId === null) return;
    const imageId = deleteTargetId;

    startTransition(async () => {
      // 순서 변경·삭제는 검수 대상이 아니라 즉시 반영된다(검수 대상은 새 이미지의 "내용"이다).
      const { success, message } = await deleteMenuCollectionImageAction(shopId, imageId, images.length);
      setDeleteTargetId(null);
      if (!success) {
        toast.error(message ?? SHOP_MENU_COLLECTION_COPY.DELETE_FAILED);
        return;
      }
      toast.success(SHOP_MENU_COLLECTION_MESSAGE.DELETE_SUCCESS);
      await reload();
    });
  }

  const disabled = isPending || isLoading;
  const isFull = images.length >= MENU_COLLECTION_MAX_COUNT;
  // 마지막 1개는 지울 수 없다. 0개일 때도 잠기지만 그 경우 목록 자체가 비어 있어 버튼이 없다.
  const deleteDisabled = images.length <= 1;

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent className="flex w-full flex-col sm:max-w-md">
        <SheetHeader>
          <SheetTitle>{SHOP_MENU_COLLECTION_COPY.SHEET_TITLE}</SheetTitle>
          <SheetDescription>{SHOP_MENU_COLLECTION_COPY.SHEET_DESCRIPTION}</SheetDescription>
        </SheetHeader>

        <div className="flex flex-1 flex-col gap-4 overflow-y-auto px-4">
          {/* 규격 안내는 상시 노출한다 — 파일 선택 전에 보이지 않으면 규격 미달 파일을 고른 뒤에야 알게 된다. */}
          <Alert>
            <AlertTitle>{SHOP_MENU_COLLECTION_MESSAGE.SPEC_HINT}</AlertTitle>
            <AlertDescription>{SHOP_MENU_COLLECTION_MESSAGE.APPROVAL_HINT}</AlertDescription>
          </Alert>

          <div className="flex items-center justify-between gap-2">
            <span className="font-medium text-sm">
              {images.length} / {MENU_COLLECTION_MAX_COUNT}
            </span>
            <input
              ref={fileInputRef}
              type="file"
              accept={MENU_COLLECTION_IMAGE_ACCEPT}
              className="hidden"
              onChange={handleFileChange}
            />
            {/* 6개가 차면 버튼을 잠근다 — 눌러서 400 을 받고 나서야 알게 되는 상황을 없앤다. */}
            <Button
              type="button"
              size="sm"
              variant="outline"
              disabled={disabled || isFull}
              onClick={() => fileInputRef.current?.click()}
            >
              <Plus className="size-4" />
              {SHOP_MENU_COLLECTION_COPY.BUTTON_ADD}
            </Button>
          </div>

          {isFull && <p className="text-muted-foreground text-xs">{SHOP_MENU_COLLECTION_MESSAGE.LIMIT_EXCEEDED}</p>}
          {deleteDisabled && images.length === 1 && (
            <p className="text-muted-foreground text-xs">{SHOP_MENU_COLLECTION_MESSAGE.LAST_CANNOT_DELETE}</p>
          )}

          {isLoading ? (
            <Skeleton className="h-24 w-full" />
          ) : images.length === 0 ? (
            <span className="py-4 text-muted-foreground text-sm">{SHOP_MENU_COLLECTION_COPY.EMPTY}</span>
          ) : (
            <DndContext
              id="menu-collection-sheet"
              sensors={sensors}
              collisionDetection={closestCenter}
              modifiers={[restrictToVerticalAxis, restrictToParentElement]}
              onDragEnd={handleDragEnd}
            >
              <SortableContext items={images.map((image) => image.id)} strategy={verticalListSortingStrategy}>
                {images.map((image, index) => (
                  <SortableImageCard
                    key={image.id}
                    image={image}
                    index={index}
                    disabled={disabled}
                    deleteDisabled={deleteDisabled}
                    onDelete={setDeleteTargetId}
                  />
                ))}
              </SortableContext>
            </DndContext>
          )}

          <Collapsible className="group/criteria rounded-md border px-3 py-2">
            <CollapsibleTrigger className="flex w-full items-center justify-between gap-2 text-left font-medium text-sm">
              {SHOP_MENU_COLLECTION_COPY.CRITERIA_TITLE}
              <ChevronRight className="size-4 transition-transform duration-200 group-data-[state=open]/criteria:rotate-90" />
            </CollapsibleTrigger>
            <CollapsibleContent>
              <ol className="mt-2 list-decimal space-y-1 pl-4 text-muted-foreground text-xs leading-snug">
                {SHOP_MENU_COLLECTION_COPY.CRITERIA_ITEMS.map((item) => (
                  <li key={item}>{item}</li>
                ))}
              </ol>
            </CollapsibleContent>
          </Collapsible>
        </div>

        <SheetFooter>
          <SheetClose asChild>
            <Button variant="outline" disabled={isPending}>
              {SHOP_MENU_COLLECTION_COPY.BUTTON_CLOSE}
            </Button>
          </SheetClose>
        </SheetFooter>

        <AlertDialog
          open={deleteTargetId !== null}
          onOpenChange={(next) => {
            if (!next) setDeleteTargetId(null);
          }}
        >
          <AlertDialogContent>
            <AlertDialogHeader>
              <AlertDialogTitle>{SHOP_MENU_COLLECTION_COPY.DELETE_CONFIRM_TITLE}</AlertDialogTitle>
              <AlertDialogDescription>{SHOP_MENU_COLLECTION_COPY.DELETE_CONFIRM_DESCRIPTION}</AlertDialogDescription>
            </AlertDialogHeader>
            <AlertDialogFooter>
              <AlertDialogCancel>{SHOP_MENU_COLLECTION_COPY.DELETE_CONFIRM_CANCEL}</AlertDialogCancel>
              <AlertDialogAction disabled={disabled} onClick={handleConfirmDelete}>
                {SHOP_MENU_COLLECTION_COPY.DELETE_CONFIRM_ACTION}
              </AlertDialogAction>
            </AlertDialogFooter>
          </AlertDialogContent>
        </AlertDialog>
      </SheetContent>
    </Sheet>
  );
}
