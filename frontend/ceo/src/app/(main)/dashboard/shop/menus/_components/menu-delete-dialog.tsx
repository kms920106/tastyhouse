"use client";

import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogClose,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { PRODUCT_MENU_COPY } from "@/feature/product/message";

/**
 * 삭제 확인 대상.
 *
 * 메뉴 일괄 삭제와 메뉴그룹 삭제는 확인 문구만 다르고 흐름이 같아 한 다이얼로그가 겸한다 —
 * 각각 따로 만들면 [삭제하기] 버튼의 pending 처리를 두 벌 유지해야 한다.
 */
export type MenuDeleteTarget = { scope: "menus"; count: number } | { scope: "group"; categoryId: number; name: string };

interface MenuDeleteDialogProps {
  target: MenuDeleteTarget | null;
  pending?: boolean;
  onOpenChange: (open: boolean) => void;
  onConfirm: () => void;
}

export function MenuDeleteDialog({ target, pending, onOpenChange, onConfirm }: MenuDeleteDialogProps) {
  const isGroup = target?.scope === "group";

  return (
    <Dialog open={target !== null} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>
            {isGroup ? PRODUCT_MENU_COPY.DIALOG_GROUP_DELETE_TITLE : PRODUCT_MENU_COPY.DIALOG_MENU_DELETE_TITLE}
          </DialogTitle>
          <DialogDescription>
            {isGroup
              ? PRODUCT_MENU_COPY.DIALOG_GROUP_DELETE_DESCRIPTION
              : PRODUCT_MENU_COPY.DIALOG_MENU_DELETE_DESCRIPTION}
          </DialogDescription>
        </DialogHeader>

        {/* 무엇을 지우는지 다시 보여준다 — 일괄 삭제는 되돌릴 수 없어 개수 확인이 마지막 방어선이다. */}
        <p className="text-sm font-medium">
          {isGroup ? (
            target.name
          ) : (
            <>
              {PRODUCT_MENU_COPY.BULK_PREFIX} {target?.count ?? 0}
              {PRODUCT_MENU_COPY.BULK_SUFFIX}
            </>
          )}
        </p>

        <DialogFooter>
          <Button type="button" variant="destructive" disabled={pending} onClick={onConfirm}>
            {PRODUCT_MENU_COPY.BUTTON_CONFIRM_DELETE}
          </Button>
          <DialogClose asChild>
            <Button variant="outline" disabled={pending}>
              {PRODUCT_MENU_COPY.BUTTON_CANCEL}
            </Button>
          </DialogClose>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
