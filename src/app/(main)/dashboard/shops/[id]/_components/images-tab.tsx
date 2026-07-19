"use client";

import * as React from "react";

import { toast } from "sonner";

import { ALLOWED_IMAGE_TYPES, MAX_IMAGE_SIZE_BYTES } from "@/api/file/file.dto";
import { Button } from "@/components/ui/button";
import { Field, FieldLabel } from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { Separator } from "@/components/ui/separator";
import { Skeleton } from "@/components/ui/skeleton";
import { Switch } from "@/components/ui/switch";
import {
  createBannerAction,
  createPhotoCategoryAction,
  createPhotoCategoryImageAction,
  deleteBannerAction,
  deletePhotoCategoryAction,
  deletePhotoCategoryImageAction,
  fetchBannersAction,
  fetchPhotoCategoriesAction,
  fetchPhotoCategoryImagesAction,
  updatePhotoCategoryImageAction,
  uploadShopImageAction,
} from "@/feature/shop/actions";
import { PHOTO_CATEGORY_NAME_MAX } from "@/feature/shop/constants";
import type { BannerImage, PhotoCategory, PhotoImage } from "@/feature/shop/domain";
import { SHOP_MESSAGE } from "@/feature/shop/message";

interface TabProps {
  shopId: number;
}

async function uploadAndValidate(file: File): Promise<number | null> {
  if (!(ALLOWED_IMAGE_TYPES as readonly string[]).includes(file.type)) {
    toast.error(SHOP_MESSAGE.IMAGE_TYPE_INVALID);
    return null;
  }
  if (file.size > MAX_IMAGE_SIZE_BYTES) {
    toast.error(SHOP_MESSAGE.IMAGE_SIZE_EXCEEDED);
    return null;
  }
  const formData = new FormData();
  formData.append("file", file);
  const result = await uploadShopImageAction(formData);
  if (!result.success || result.fileId === undefined) {
    toast.error(result.message ?? SHOP_MESSAGE.IMAGE_UPLOAD_FAILED);
    return null;
  }
  return result.fileId;
}

function BannersSection({ shopId }: TabProps) {
  const [banners, setBanners] = React.useState<BannerImage[]>([]);
  const [isLoading, setIsLoading] = React.useState(false);
  const [error, setError] = React.useState<string | null>(null);
  const [isUploading, setIsUploading] = React.useState(false);
  const [isPending, startTransition] = React.useTransition();
  const [sort, setSort] = React.useState(0);

  const load = React.useCallback(() => {
    setIsLoading(true);
    setError(null);
    void fetchBannersAction(shopId).then((result) => {
      setIsLoading(false);
      if (result.success && result.data) {
        setBanners(result.data);
      } else {
        setError(result.message ?? SHOP_MESSAGE.BANNERS_LOAD_FAILED);
      }
    });
  }, [shopId]);

  React.useEffect(() => {
    load();
  }, [load]);

  async function handleFileChange(event: React.ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0];
    if (!file) return;
    setIsUploading(true);
    const fileId = await uploadAndValidate(file);
    setIsUploading(false);
    event.target.value = "";
    if (fileId == null) return;

    startTransition(async () => {
      const { success, message } = await createBannerAction(shopId, { imageFileId: fileId, sort });
      if (success) {
        toast.success(SHOP_MESSAGE.BANNER_CREATE_SUCCESS);
        setSort(0);
        load();
      } else {
        toast.error(message ?? SHOP_MESSAGE.CREATE_UPDATE_FAILED);
      }
    });
  }

  function handleDelete(bannerImageId: number) {
    startTransition(async () => {
      const { success, message } = await deleteBannerAction(bannerImageId);
      if (success) {
        toast.success(SHOP_MESSAGE.BANNER_DELETE_SUCCESS);
        load();
      } else {
        toast.error(message ?? SHOP_MESSAGE.DELETE_FAILED);
      }
    });
  }

  const busy = isUploading || isPending;

  return (
    <div className="space-y-3">
      <h4 className="font-medium text-sm">배너 이미지</h4>
      {error ? (
        <p className="text-destructive text-sm">{error}</p>
      ) : isLoading ? (
        <div className="grid grid-cols-3 gap-2">
          <Skeleton className="h-24 w-full" />
          <Skeleton className="h-24 w-full" />
        </div>
      ) : banners.length ? (
        <div className="grid grid-cols-2 gap-2 sm:grid-cols-3">
          {banners.map((banner) => (
            <div key={banner.id} className="space-y-1">
              {/* biome-ignore lint/performance/noImgElement: CDN URL 미리보기 */}
              <img src={banner.imageUrl} alt="배너" className="h-24 w-full rounded-md border object-cover" />
              <div className="flex items-center justify-between text-muted-foreground text-xs">
                <span>정렬 {banner.sort}</span>
                <button
                  type="button"
                  className="text-destructive disabled:opacity-50"
                  disabled={isPending}
                  onClick={() => handleDelete(banner.id)}
                >
                  삭제
                </button>
              </div>
            </div>
          ))}
        </div>
      ) : (
        <p className="text-muted-foreground text-sm">등록된 배너 이미지가 없습니다.</p>
      )}

      <div className="flex flex-wrap items-end gap-2">
        <Field className="gap-1.5">
          <FieldLabel htmlFor="banner-file">이미지 업로드</FieldLabel>
          <Input id="banner-file" type="file" accept="image/*" onChange={handleFileChange} disabled={busy} />
        </Field>
        <Field className="w-24 gap-1.5">
          <FieldLabel htmlFor="banner-sort">정렬</FieldLabel>
          <Input
            id="banner-sort"
            type="number"
            value={sort}
            onChange={(e) => setSort(Number(e.target.value))}
            disabled={busy}
          />
        </Field>
        {isUploading && <p className="text-muted-foreground text-sm">업로드 중...</p>}
      </div>
    </div>
  );
}

function PhotoCategoryImages({ categoryId }: { categoryId: number }) {
  const [images, setImages] = React.useState<PhotoImage[]>([]);
  const [isLoading, setIsLoading] = React.useState(false);
  const [error, setError] = React.useState<string | null>(null);
  const [isUploading, setIsUploading] = React.useState(false);
  const [isPending, startTransition] = React.useTransition();
  const [sort, setSort] = React.useState(0);

  const load = React.useCallback(() => {
    setIsLoading(true);
    setError(null);
    void fetchPhotoCategoryImagesAction(categoryId).then((result) => {
      setIsLoading(false);
      if (result.success && result.data) {
        setImages(result.data);
      } else {
        setError(result.message ?? SHOP_MESSAGE.PHOTO_IMAGES_LOAD_FAILED);
      }
    });
  }, [categoryId]);

  React.useEffect(() => {
    load();
  }, [load]);

  async function handleFileChange(event: React.ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0];
    if (!file) return;
    setIsUploading(true);
    const fileId = await uploadAndValidate(file);
    setIsUploading(false);
    event.target.value = "";
    if (fileId == null) return;

    startTransition(async () => {
      const { success, message } = await createPhotoCategoryImageAction(categoryId, {
        imageFileId: fileId,
        sort,
        visible: true,
      });
      if (success) {
        toast.success(SHOP_MESSAGE.PHOTO_IMAGE_CREATE_SUCCESS);
        setSort(0);
        load();
      } else {
        toast.error(message ?? SHOP_MESSAGE.CREATE_UPDATE_FAILED);
      }
    });
  }

  function handleToggleVisible(image: PhotoImage) {
    startTransition(async () => {
      const { success, message } = await updatePhotoCategoryImageAction(image.id, {
        imageFileId: image.imageFileId,
        sort: image.sort,
        visible: !image.visible,
      });
      if (success) {
        toast.success(SHOP_MESSAGE.PHOTO_IMAGE_UPDATE_SUCCESS);
        load();
      } else {
        toast.error(message ?? SHOP_MESSAGE.CREATE_UPDATE_FAILED);
      }
    });
  }

  function handleDelete(imageId: number) {
    startTransition(async () => {
      const { success, message } = await deletePhotoCategoryImageAction(imageId);
      if (success) {
        toast.success(SHOP_MESSAGE.PHOTO_IMAGE_DELETE_SUCCESS);
        load();
      } else {
        toast.error(message ?? SHOP_MESSAGE.DELETE_FAILED);
      }
    });
  }

  const busy = isUploading || isPending;

  return (
    <div className="space-y-2 rounded-md border p-3">
      {error ? (
        <p className="text-destructive text-sm">{error}</p>
      ) : isLoading ? (
        <Skeleton className="h-20 w-full" />
      ) : images.length ? (
        <div className="grid grid-cols-2 gap-2 sm:grid-cols-4">
          {images.map((image) => (
            <div key={image.id} className="space-y-1">
              {/* biome-ignore lint/performance/noImgElement: CDN URL 미리보기 */}
              <img src={image.imageUrl} alt="포토" className="h-20 w-full rounded-md border object-cover" />
              <div className="flex items-center justify-between text-muted-foreground text-xs">
                <span>정렬 {image.sort}</span>
                <Switch checked={image.visible} onCheckedChange={() => handleToggleVisible(image)} disabled={busy} />
                <button
                  type="button"
                  className="text-destructive disabled:opacity-50"
                  disabled={busy}
                  onClick={() => handleDelete(image.id)}
                >
                  삭제
                </button>
              </div>
            </div>
          ))}
        </div>
      ) : (
        <p className="text-muted-foreground text-sm">등록된 이미지가 없습니다.</p>
      )}

      <div className="flex flex-wrap items-end gap-2 pt-1">
        <Field className="gap-1.5">
          <FieldLabel htmlFor={`photo-image-file-${categoryId}`}>이미지 업로드</FieldLabel>
          <Input
            id={`photo-image-file-${categoryId}`}
            type="file"
            accept="image/*"
            onChange={handleFileChange}
            disabled={busy}
          />
        </Field>
        <Field className="w-24 gap-1.5">
          <FieldLabel htmlFor={`photo-image-sort-${categoryId}`}>정렬</FieldLabel>
          <Input
            id={`photo-image-sort-${categoryId}`}
            type="number"
            value={sort}
            onChange={(e) => setSort(Number(e.target.value))}
            disabled={busy}
          />
        </Field>
        {isUploading && <p className="text-muted-foreground text-sm">업로드 중...</p>}
      </div>
    </div>
  );
}

function PhotoCategoriesSection({ shopId }: TabProps) {
  const [categories, setCategories] = React.useState<PhotoCategory[]>([]);
  const [isLoading, setIsLoading] = React.useState(false);
  const [error, setError] = React.useState<string | null>(null);
  const [isPending, startTransition] = React.useTransition();
  const [name, setName] = React.useState("");
  const [expandedId, setExpandedId] = React.useState<number | null>(null);

  const load = React.useCallback(() => {
    setIsLoading(true);
    setError(null);
    void fetchPhotoCategoriesAction(shopId).then((result) => {
      setIsLoading(false);
      if (result.success && result.data) {
        setCategories(result.data);
      } else {
        setError(result.message ?? SHOP_MESSAGE.PHOTO_CATEGORIES_LOAD_FAILED);
      }
    });
  }, [shopId]);

  React.useEffect(() => {
    load();
  }, [load]);

  function handleCreate() {
    const trimmed = name.trim();
    if (!trimmed) return;
    startTransition(async () => {
      const { success, message } = await createPhotoCategoryAction(shopId, { name: trimmed });
      if (success) {
        toast.success(SHOP_MESSAGE.PHOTO_CATEGORY_CREATE_SUCCESS);
        setName("");
        load();
      } else {
        toast.error(message ?? SHOP_MESSAGE.CREATE_UPDATE_FAILED);
      }
    });
  }

  function handleDelete(categoryId: number) {
    startTransition(async () => {
      const { success, message } = await deletePhotoCategoryAction(categoryId);
      if (success) {
        toast.success(SHOP_MESSAGE.PHOTO_CATEGORY_DELETE_SUCCESS);
        if (expandedId === categoryId) setExpandedId(null);
        load();
      } else {
        toast.error(message ?? SHOP_MESSAGE.DELETE_FAILED);
      }
    });
  }

  return (
    <div className="space-y-3">
      <h4 className="font-medium text-sm">포토 카테고리</h4>
      {error ? (
        <p className="text-destructive text-sm">{error}</p>
      ) : isLoading ? (
        <Skeleton className="h-16 w-full" />
      ) : categories.length ? (
        <div className="space-y-2">
          {categories.map((category) => (
            <div key={category.id} className="space-y-2">
              <div className="flex items-center justify-between rounded-md border px-3 py-2 text-sm">
                <button
                  type="button"
                  className="font-medium"
                  onClick={() => setExpandedId(expandedId === category.id ? null : category.id)}
                >
                  {category.name}
                </button>
                <div className="flex gap-2">
                  <Button
                    type="button"
                    size="sm"
                    variant="outline"
                    onClick={() => setExpandedId(expandedId === category.id ? null : category.id)}
                  >
                    {expandedId === category.id ? "이미지 닫기" : "이미지 관리"}
                  </Button>
                  <Button
                    type="button"
                    size="sm"
                    variant="ghost"
                    className="text-destructive"
                    disabled={isPending}
                    onClick={() => handleDelete(category.id)}
                  >
                    삭제
                  </Button>
                </div>
              </div>
              {expandedId === category.id ? <PhotoCategoryImages categoryId={category.id} /> : null}
            </div>
          ))}
        </div>
      ) : (
        <p className="text-muted-foreground text-sm">등록된 포토 카테고리가 없습니다.</p>
      )}

      <div className="flex items-end gap-2">
        <Field className="flex-1 gap-1.5">
          <FieldLabel htmlFor="photo-category-name">카테고리 이름</FieldLabel>
          <Input
            id="photo-category-name"
            placeholder="예: 가게 외관"
            maxLength={PHOTO_CATEGORY_NAME_MAX}
            value={name}
            onChange={(e) => setName(e.target.value)}
            disabled={isPending}
          />
        </Field>
        <Button type="button" size="sm" onClick={handleCreate} disabled={isPending || !name.trim()}>
          카테고리 추가
        </Button>
      </div>
    </div>
  );
}

export function ImagesTab({ shopId }: TabProps) {
  return (
    <div className="space-y-6">
      <BannersSection shopId={shopId} />
      <Separator />
      <PhotoCategoriesSection shopId={shopId} />
    </div>
  );
}
