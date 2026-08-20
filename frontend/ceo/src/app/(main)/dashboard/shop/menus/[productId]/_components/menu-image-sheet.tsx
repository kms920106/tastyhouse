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
import { GripVertical, Trash2 } from "lucide-react";
import { toast } from "sonner";

import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Field, FieldDescription, FieldLabel } from "@/components/ui/field";
import { Input } from "@/components/ui/input";
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
  changeMenuImageOrderAction,
  deleteMenuImageAction,
  loadMenuImagesAction,
  requestMenuImageAction,
} from "@/feature/product/actions";
import { PRODUCT_IMAGE_ACCEPT } from "@/feature/product/constants";
import type { MenuImage } from "@/feature/product/domain";
import { PRODUCT_DETAIL_COPY, PRODUCT_DETAIL_SCREEN_COPY, PRODUCT_MENU_MESSAGE } from "@/feature/product/message";

import { ShopImagePreview } from "../../../_components/shop-image-preview";

interface MenuImageSheetProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  productId: number;
  shopId: number;
  /** 상세 행의 요약(등록 장수·검수 상태)을 갱신하기 위해 부모에 알린다 */
  onChanged: (imageCount: number, pending: boolean) => void;
}

function SortableImageRow({
  image,
  index,
  disabled,
  onDelete,
}: {
  image: MenuImage;
  index: number;
  disabled: boolean;
  onDelete: (imageId: number) => void;
}) {
  const { attributes, listeners, setNodeRef, transform, transition, isDragging } = useSortable({ id: image.id });

  return (
    <div
      ref={setNodeRef}
      style={{ transform: CSS.Transform.toString(transform), transition }}
      className="flex items-center gap-3 border-b py-3 last:border-b-0"
      data-dragging={isDragging}
    >
      <button
        type="button"
        className="cursor-grab text-muted-foreground disabled:cursor-not-allowed"
        aria-label={PRODUCT_DETAIL_SCREEN_COPY.DRAG_HANDLE_LABEL}
        disabled={disabled}
        {...attributes}
        {...listeners}
      >
        <GripVertical className="size-4" />
      </button>
      <ShopImagePreview
        src={image.imageUrl}
        alt={`${PRODUCT_DETAIL_SCREEN_COPY.IMAGE_ALT_PREFIX}${index + 1}`}
        className="size-16 shrink-0"
      />
      <span className="flex-1 text-muted-foreground text-sm">{index + 1}</span>
      <Button
        type="button"
        size="sm"
        variant="ghost"
        disabled={disabled}
        aria-label={PRODUCT_DETAIL_COPY.IMAGE_DELETE}
        onClick={() => onDelete(image.id)}
      >
        <Trash2 className="size-4" />
      </Button>
    </div>
  );
}

export function MenuImageSheet({ open, onOpenChange, productId, shopId, onChanged }: MenuImageSheetProps) {
  const [isPending, startTransition] = React.useTransition();
  const [isLoading, setIsLoading] = React.useState(false);
  const [images, setImages] = React.useState<MenuImage[]>([]);
  const [pendingStatus, setPendingStatus] = React.useState<string | null>(null);
  const [rejectReason, setRejectReason] = React.useState<string | null>(null);
  const [selectedFile, setSelectedFile] = React.useState<File | null>(null);
  const fileInputRef = React.useRef<HTMLInputElement>(null);

  const sensors = useSensors(
    useSensor(PointerSensor),
    useSensor(KeyboardSensor, { coordinateGetter: sortableKeyboardCoordinates }),
  );

  const reload = React.useCallback(async () => {
    setIsLoading(true);
    const { success, message, data } = await loadMenuImagesAction(productId, shopId);
    setIsLoading(false);

    if (!success || !data) {
      toast.error(message ?? PRODUCT_MENU_MESSAGE.IMAGE_LOAD_FAILED);
      return;
    }

    setImages(data.images);
    setPendingStatus(data.pendingRequest?.status ?? null);
    setRejectReason(data.pendingRequest?.rejectReason ?? null);
    onChanged(data.images.length, data.pendingRequest?.status === "PENDING");
  }, [productId, shopId, onChanged]);

  React.useEffect(() => {
    if (open) void reload();
  }, [open, reload]);

  // 같은 대상에 PENDING 2건이 생기지 않게 서버가 `*_ALREADY_PENDING` 으로 막는다.
  // 버튼을 미리 잠가 사용자가 400 을 받고 나서야 알게 되는 상황을 없앤다.
  const hasPendingRequest = pendingStatus === "PENDING";

  function handleUpload() {
    if (selectedFile === null) {
      toast.error(PRODUCT_MENU_MESSAGE.IMAGE_FILE_REQUIRED);
      return;
    }

    const formData = new FormData();
    formData.append("file", selectedFile);

    startTransition(async () => {
      // 규격(1280×960·15MB·JPG/PNG)은 화면이 **안내만** 하고 판정은 서버가 한다 —
      // 브라우저에서 해상도를 재도 서버가 `ImageIO` 로 다시 보므로 두 판정이 어긋날 수 있다.
      // `PRODUCT_IMAGE_SPEC_INVALID` 의 한국어 문구를 그대로 노출한다.
      const { success, message } = await requestMenuImageAction(productId, shopId, formData);
      if (!success) {
        toast.error(message ?? PRODUCT_MENU_MESSAGE.IMAGE_REQUEST_FAILED);
        return;
      }
      toast.success(PRODUCT_MENU_MESSAGE.IMAGE_REQUEST_SUCCESS);
      setSelectedFile(null);
      if (fileInputRef.current) fileInputRef.current.value = "";
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
      // `sort` 숫자를 계산해 보내지 않는다. 확정된 id 배열만 보내고 서버가 0..N-1 로 정규화한다.
      const { success, message } = await changeMenuImageOrderAction(
        productId,
        shopId,
        preview.map((image) => image.id),
      );
      if (!success) {
        toast.error(message ?? PRODUCT_MENU_MESSAGE.IMAGE_ORDER_FAILED);
      } else {
        toast.success(PRODUCT_MENU_MESSAGE.IMAGE_ORDER_SUCCESS);
      }
      await reload();
    });
  }

  function handleDelete(imageId: number) {
    startTransition(async () => {
      // 순서 변경·삭제는 검수 대상이 아니라 즉시 반영된다(검수 대상은 새 이미지의 "내용"이다).
      const { success, message } = await deleteMenuImageAction(imageId, shopId);
      if (!success) {
        toast.error(message ?? PRODUCT_MENU_MESSAGE.IMAGE_DELETE_FAILED);
        return;
      }
      toast.success(PRODUCT_MENU_MESSAGE.IMAGE_DELETE_SUCCESS);
      await reload();
    });
  }

  const disabled = isPending || isLoading;

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent className="flex w-full flex-col sm:max-w-md">
        <SheetHeader>
          <SheetTitle className="flex items-center gap-2">
            {PRODUCT_DETAIL_COPY.SHEET_IMAGE_TITLE}
            {hasPendingRequest && <Badge variant="secondary">{PRODUCT_DETAIL_COPY.BADGE_PENDING}</Badge>}
            {pendingStatus === "REJECTED" && <Badge variant="destructive">{PRODUCT_DETAIL_COPY.BADGE_REJECTED}</Badge>}
          </SheetTitle>
          <SheetDescription>{PRODUCT_DETAIL_COPY.IMAGE_APPROVAL_NOTICE}</SheetDescription>
        </SheetHeader>

        <div className="flex flex-1 flex-col gap-6 overflow-y-auto px-4">
          {pendingStatus === "REJECTED" && rejectReason !== null && (
            <Alert variant="destructive">
              <AlertTitle>{PRODUCT_DETAIL_COPY.BADGE_REJECTED}</AlertTitle>
              <AlertDescription>{`${PRODUCT_DETAIL_COPY.REJECT_REASON_PREFIX}${rejectReason}`}</AlertDescription>
            </Alert>
          )}

          <Field className="gap-1.5">
            <FieldLabel htmlFor="menu-image-file">{PRODUCT_DETAIL_COPY.IMAGE_UPLOAD_LABEL}</FieldLabel>
            <Input
              id="menu-image-file"
              ref={fileInputRef}
              type="file"
              accept={PRODUCT_IMAGE_ACCEPT}
              disabled={disabled || hasPendingRequest}
              onChange={(event) => setSelectedFile(event.target.files?.[0] ?? null)}
            />
            <FieldDescription>{PRODUCT_DETAIL_COPY.IMAGE_SPEC_HELP}</FieldDescription>
            {hasPendingRequest && (
              <FieldDescription>{PRODUCT_DETAIL_SCREEN_COPY.IMAGE_PENDING_NOTICE}</FieldDescription>
            )}
            <Button
              type="button"
              size="sm"
              className="w-fit"
              disabled={disabled || hasPendingRequest}
              onClick={handleUpload}
            >
              {PRODUCT_DETAIL_COPY.IMAGE_UPLOAD_SUBMIT}
            </Button>
          </Field>

          <div className="flex flex-col gap-1.5">
            <span className="font-medium text-sm">{PRODUCT_DETAIL_COPY.ROW_IMAGE}</span>
            <span className="text-muted-foreground text-xs leading-snug">{PRODUCT_DETAIL_COPY.IMAGE_SORT_HELP}</span>

            {isLoading ? (
              <Skeleton className="h-20 w-full" />
            ) : images.length === 0 ? (
              <span className="py-4 text-muted-foreground text-sm">{PRODUCT_DETAIL_COPY.IMAGE_EMPTY}</span>
            ) : (
              <DndContext
                sensors={sensors}
                collisionDetection={closestCenter}
                modifiers={[restrictToVerticalAxis, restrictToParentElement]}
                onDragEnd={handleDragEnd}
              >
                <SortableContext items={images.map((image) => image.id)} strategy={verticalListSortingStrategy}>
                  {images.map((image, index) => (
                    <SortableImageRow
                      key={image.id}
                      image={image}
                      index={index}
                      disabled={disabled}
                      onDelete={handleDelete}
                    />
                  ))}
                </SortableContext>
              </DndContext>
            )}
          </div>
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
  );
}
