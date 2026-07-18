"use client";

import * as React from "react";

import { toast } from "sonner";

import { ALLOWED_IMAGE_TYPES, MAX_IMAGE_SIZE_BYTES } from "@/api/file/file.dto";
import { Button } from "@/components/ui/button";
import { Field, FieldLabel } from "@/components/ui/field";
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
import { Switch } from "@/components/ui/switch";
import { addProductImageAction, fetchProductImagesAction, uploadProductImageAction } from "@/feature/product/actions";
import type { ProductListItem } from "@/feature/product/domain";
import { PRODUCT_MESSAGE } from "@/feature/product/message";

interface ProductImagesSheetProps {
  /** 이미지 관리 대상 상품. null 이면 닫힌 상태. */
  product: Pick<ProductListItem, "id" | "name"> | null;
  onOpenChange: (open: boolean) => void;
}

export function ProductImagesSheet({ product, onOpenChange }: ProductImagesSheetProps) {
  const [imageUrls, setImageUrls] = React.useState<string[]>([]);
  const [isLoading, setIsLoading] = React.useState(false);
  const [error, setError] = React.useState<string | null>(null);
  const [isUploading, setIsUploading] = React.useState(false);
  const [isPending, startTransition] = React.useTransition();
  const [uploadedFileId, setUploadedFileId] = React.useState<number | undefined>(undefined);
  const [previewUrl, setPreviewUrl] = React.useState<string | undefined>(undefined);
  const [sort, setSort] = React.useState(0);
  const [visible, setVisible] = React.useState(true);

  const productId = product?.id ?? null;

  const loadImages = React.useCallback(() => {
    if (productId == null) return;

    let active = true;
    setIsLoading(true);
    setError(null);

    void fetchProductImagesAction(productId).then((result) => {
      if (!active) return;
      if (result.success && result.data) {
        setImageUrls(result.data);
      } else {
        setError(result.message ?? PRODUCT_MESSAGE.IMAGES_LOAD_FAILED);
      }
      setIsLoading(false);
    });

    return () => {
      active = false;
    };
  }, [productId]);

  const resetUpload = React.useCallback(() => {
    setUploadedFileId(undefined);
    setPreviewUrl((prev) => {
      if (prev?.startsWith("blob:")) URL.revokeObjectURL(prev);
      return undefined;
    });
    setSort(0);
    setVisible(true);
  }, []);

  React.useEffect(() => {
    if (productId == null) return;
    setImageUrls([]);
    setError(null);
    resetUpload();
    const cleanup = loadImages();
    return cleanup;
  }, [productId, loadImages, resetUpload]);

  // 언마운트 시 objectURL 해제
  React.useEffect(() => {
    return () => {
      setPreviewUrl((prev) => {
        if (prev?.startsWith("blob:")) URL.revokeObjectURL(prev);
        return undefined;
      });
    };
  }, []);

  async function handleFileChange(event: React.ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0];
    if (!file) return;

    if (!(ALLOWED_IMAGE_TYPES as readonly string[]).includes(file.type)) {
      toast.error(PRODUCT_MESSAGE.IMAGE_TYPE_INVALID);
      event.target.value = "";
      return;
    }
    if (file.size > MAX_IMAGE_SIZE_BYTES) {
      toast.error(PRODUCT_MESSAGE.IMAGE_SIZE_EXCEEDED);
      event.target.value = "";
      return;
    }

    setIsUploading(true);
    const formData = new FormData();
    formData.append("file", file);

    const result = await uploadProductImageAction(formData);
    setIsUploading(false);
    // 동일 파일을 다시 선택해도 change 이벤트가 발생하도록 입력값을 초기화한다.
    event.target.value = "";

    if (!result.success || result.fileId === undefined) {
      toast.error(result.message ?? PRODUCT_MESSAGE.IMAGE_UPLOAD_FAILED);
      return;
    }

    setUploadedFileId(result.fileId);
    setPreviewUrl((prev) => {
      if (prev?.startsWith("blob:")) URL.revokeObjectURL(prev);
      return URL.createObjectURL(file);
    });
  }

  function handleAddImage() {
    if (productId == null || uploadedFileId === undefined) {
      toast.error(PRODUCT_MESSAGE.INVALID_INPUT);
      return;
    }
    startTransition(async () => {
      const { success, message } = await addProductImageAction(productId, {
        imageFileId: uploadedFileId,
        sort,
        visible,
      });
      if (success) {
        toast.success(PRODUCT_MESSAGE.IMAGE_ADD_SUCCESS);
        resetUpload();
        loadImages();
      } else {
        toast.error(message ?? PRODUCT_MESSAGE.IMAGE_ADD_FAILED);
      }
    });
  }

  const busy = isUploading || isPending;

  return (
    <Sheet open={product != null} onOpenChange={onOpenChange}>
      <SheetContent className="flex w-full flex-col sm:max-w-md">
        <SheetHeader>
          <SheetTitle>이미지 관리</SheetTitle>
          <SheetDescription>{product ? `"${product.name}" 상품의 이미지를 관리합니다.` : ""}</SheetDescription>
        </SheetHeader>

        <div className="flex-1 space-y-6 overflow-y-auto px-4">
          {/* 등록된 이미지 목록 */}
          <div className="space-y-2">
            <h4 className="font-medium text-sm">등록된 이미지</h4>
            {error ? (
              <p className="text-destructive text-sm">{error}</p>
            ) : isLoading ? (
              <div className="grid grid-cols-2 gap-2">
                <Skeleton className="h-28 w-full" />
                <Skeleton className="h-28 w-full" />
              </div>
            ) : imageUrls.length ? (
              <div className="grid grid-cols-2 gap-2">
                {imageUrls.map((url, index) => (
                  // biome-ignore lint/performance/noImgElement: CDN URL 미리보기
                  <img
                    key={url}
                    src={url}
                    alt={`상품 이미지 ${index + 1}`}
                    className="h-28 w-full rounded-md border object-cover"
                  />
                ))}
              </div>
            ) : (
              <p className="text-muted-foreground text-sm">등록된 이미지가 없습니다.</p>
            )}
          </div>

          {/* 이미지 업로드 + 등록 */}
          <div className="space-y-3">
            <h4 className="font-medium text-sm">이미지 추가</h4>
            <Field className="gap-1.5">
              <FieldLabel htmlFor="product-image-file">이미지 파일</FieldLabel>
              {previewUrl ? (
                // biome-ignore lint/performance/noImgElement: 업로드 직후 blob URL 미리보기
                <img
                  src={previewUrl}
                  alt="업로드 이미지 미리보기"
                  className="h-32 w-full rounded-md border object-cover"
                />
              ) : null}
              <Input id="product-image-file" type="file" accept="image/*" onChange={handleFileChange} disabled={busy} />
              {isUploading && <p className="text-muted-foreground text-sm">업로드 중...</p>}
            </Field>

            <div className="flex flex-wrap items-end gap-2">
              <Field className="w-24 gap-1.5">
                <FieldLabel htmlFor="product-image-sort">정렬</FieldLabel>
                <Input
                  id="product-image-sort"
                  type="number"
                  value={sort}
                  onChange={(e) => setSort(Number(e.target.value))}
                  disabled={busy}
                />
              </Field>
              <Field orientation="horizontal" className="pb-2">
                <FieldLabel htmlFor="product-image-visible">노출</FieldLabel>
                <Switch id="product-image-visible" checked={visible} onCheckedChange={setVisible} disabled={busy} />
              </Field>
            </div>

            <Button type="button" size="sm" onClick={handleAddImage} disabled={busy || uploadedFileId === undefined}>
              {isPending ? "등록 중..." : "이미지 등록"}
            </Button>
          </div>
        </div>

        <SheetFooter>
          <SheetClose asChild>
            <Button variant="outline">닫기</Button>
          </SheetClose>
        </SheetFooter>
      </SheetContent>
    </Sheet>
  );
}
