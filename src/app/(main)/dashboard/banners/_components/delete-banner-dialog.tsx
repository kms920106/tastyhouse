"use client";

import * as React from "react";

import { toast } from "sonner";

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
import { deleteBannerAction } from "@/feature/banner/actions";
import type { BannerListItem } from "@/feature/banner/domain";
import { BANNER_MESSAGE } from "@/feature/banner/message";

interface DeleteBannerDialogProps {
  banner: BannerListItem | null;
  onOpenChange: (open: boolean) => void;
}

export function DeleteBannerDialog({ banner, onOpenChange }: DeleteBannerDialogProps) {
  const [isPending, startTransition] = React.useTransition();

  function handleDelete() {
    if (!banner) return;
    startTransition(async () => {
      const { success, message } = await deleteBannerAction(banner.id);
      if (success) {
        toast.success(BANNER_MESSAGE.DELETE_SUCCESS);
        onOpenChange(false);
      } else {
        toast.error(message ?? BANNER_MESSAGE.DELETE_FAILED);
      }
    });
  }

  return (
    <AlertDialog open={banner != null} onOpenChange={onOpenChange}>
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>배너를 삭제하시겠습니까?</AlertDialogTitle>
          <AlertDialogDescription>
            {banner
              ? `"${banner.title ?? `배너 #${banner.id}`}" 배너가 영구적으로 삭제됩니다. 이 작업은 되돌릴 수 없습니다.`
              : ""}
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel disabled={isPending}>취소</AlertDialogCancel>
          <AlertDialogAction
            onClick={(event) => {
              event.preventDefault();
              handleDelete();
            }}
            disabled={isPending}
          >
            {isPending ? "삭제 중..." : "삭제"}
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
}
