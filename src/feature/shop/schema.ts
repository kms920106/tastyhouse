import { z } from "zod";

import {
  ADDRESS_MAX,
  AMENITY_DISPLAY_NAME_MAX,
  AMENITY_OPTIONS,
  CLOSED_DAY_TYPE_OPTIONS,
  DAY_TYPE_OPTIONS,
  EDITOR_CHOICE_CONTENT_MAX,
  EDITOR_CHOICE_TITLE_MAX,
  FOOD_TYPE_DISPLAY_NAME_MAX,
  FOOD_TYPE_OPTIONS,
  ORDER_METHOD_OPTIONS,
  PHOTO_CATEGORY_NAME_MAX,
  SHOP_NAME_MAX,
  TAG_NAME_MAX,
} from "./constants";

const emptyToUndefined = (value: string) => (value.trim() === "" ? undefined : value.trim());

// HH:mm:ss 형식 검증
const timeStringSchema = z
  .string()
  .regex(/^([01]\d|2[0-3]):([0-5]\d):([0-5]\d)$/, { message: "시간은 HH:mm:ss 형식이어야 합니다." });

// ===== Phase A. 가게 본체 =====

export const shopFormSchema = z.object({
  stationId: z.number({ message: "지하철역 ID를 입력해 주세요." }).int().positive({
    message: "지하철역 ID는 양수여야 합니다.",
  }),
  name: z
    .string()
    .trim()
    .min(1, { message: "가게 이름을 입력해 주세요." })
    .max(SHOP_NAME_MAX, { message: `가게 이름은 최대 ${SHOP_NAME_MAX}자까지 입력할 수 있습니다.` }),
  latitude: z.number({ message: "위도를 입력해 주세요." }).min(-90).max(90),
  longitude: z.number({ message: "경도를 입력해 주세요." }).min(-180).max(180),
  roadAddress: z
    .string()
    .trim()
    .min(1, { message: "도로명 주소를 입력해 주세요." })
    .max(ADDRESS_MAX, { message: `주소는 최대 ${ADDRESS_MAX}자까지 입력할 수 있습니다.` }),
  lotAddress: z
    .string()
    .trim()
    .min(1, { message: "지번 주소를 입력해 주세요." })
    .max(ADDRESS_MAX, { message: `주소는 최대 ${ADDRESS_MAX}자까지 입력할 수 있습니다.` }),
  phoneNumber: z
    .string()
    .max(20, { message: "전화번호는 최대 20자까지 입력할 수 있습니다." })
    .transform(emptyToUndefined)
    .optional(),
  thumbnailImageFileId: z.number().int().positive().optional(),
});

export type ShopFormValues = z.infer<typeof shopFormSchema>;

// ===== Phase B. 운영시간 · 휴게시간 · 정기휴무일 =====

export const businessHourSchema = z
  .object({
    dayType: z.enum(DAY_TYPE_OPTIONS, { message: "요일 구분을 선택해 주세요." }),
    openTime: timeStringSchema,
    closeTime: timeStringSchema,
    isClosed: z.boolean(),
  })
  .superRefine((data, ctx) => {
    if (!data.isClosed && data.openTime >= data.closeTime) {
      ctx.addIssue({
        code: "custom",
        message: "종료 시간은 시작 시간보다 이후여야 합니다.",
        path: ["closeTime"],
      });
    }
  });

export type BusinessHourFormValues = z.infer<typeof businessHourSchema>;

export const breakTimeSchema = z
  .object({
    dayType: z.enum(DAY_TYPE_OPTIONS, { message: "요일 구분을 선택해 주세요." }),
    startTime: timeStringSchema,
    endTime: timeStringSchema,
  })
  .superRefine((data, ctx) => {
    if (data.startTime >= data.endTime) {
      ctx.addIssue({
        code: "custom",
        message: "종료 시간은 시작 시간보다 이후여야 합니다.",
        path: ["endTime"],
      });
    }
  });

export type BreakTimeFormValues = z.infer<typeof breakTimeSchema>;

export const closedDaySchema = z.object({
  closedDayType: z.enum(CLOSED_DAY_TYPE_OPTIONS, { message: "휴무 유형을 선택해 주세요." }),
});

export type ClosedDayFormValues = z.infer<typeof closedDaySchema>;

// ===== Phase C. 편의시설 · 음식종류 · 태그 =====

export const amenityCategorySchema = z.object({
  amenity: z.enum(AMENITY_OPTIONS, { message: "편의시설 종류를 선택해 주세요." }),
  displayName: z
    .string()
    .trim()
    .min(1, { message: "노출명을 입력해 주세요." })
    .max(AMENITY_DISPLAY_NAME_MAX, { message: `노출명은 최대 ${AMENITY_DISPLAY_NAME_MAX}자까지 입력할 수 있습니다.` }),
  activeImageFileId: z.number({ message: "활성 이미지를 업로드해 주세요." }).int().positive(),
  inactiveImageFileId: z.number({ message: "비활성 이미지를 업로드해 주세요." }).int().positive(),
  sort: z.number({ message: "정렬 순서를 입력해 주세요." }).int(),
  visible: z.boolean(),
});

export type AmenityCategoryFormValues = z.infer<typeof amenityCategorySchema>;

export const foodTypeCategorySchema = z.object({
  foodType: z.enum(FOOD_TYPE_OPTIONS, { message: "음식 종류를 선택해 주세요." }),
  displayName: z
    .string()
    .trim()
    .min(1, { message: "노출명을 입력해 주세요." })
    .max(FOOD_TYPE_DISPLAY_NAME_MAX, {
      message: `노출명은 최대 ${FOOD_TYPE_DISPLAY_NAME_MAX}자까지 입력할 수 있습니다.`,
    }),
  activeImageFileId: z.number({ message: "활성 이미지를 업로드해 주세요." }).int().positive(),
  inactiveImageFileId: z.number({ message: "비활성 이미지를 업로드해 주세요." }).int().positive(),
  sort: z.number({ message: "정렬 순서를 입력해 주세요." }).int(),
  visible: z.boolean(),
});

export type FoodTypeCategoryFormValues = z.infer<typeof foodTypeCategorySchema>;

export const shopAmenitySchema = z.object({
  amenityCategoryId: z.number({ message: "편의시설을 선택해 주세요." }).int().positive(),
});

export type ShopAmenityFormValues = z.infer<typeof shopAmenitySchema>;

export const shopFoodTypeSchema = z.object({
  foodTypeCategoryId: z.number({ message: "음식종류를 선택해 주세요." }).int().positive(),
});

export type ShopFoodTypeFormValues = z.infer<typeof shopFoodTypeSchema>;

export const tagSchema = z.object({
  tagName: z
    .string()
    .trim()
    .min(1, { message: "태그명을 입력해 주세요." })
    .max(TAG_NAME_MAX, { message: `태그명은 최대 ${TAG_NAME_MAX}자까지 입력할 수 있습니다.` }),
});

export type TagFormValues = z.infer<typeof tagSchema>;

// ===== Phase D. 주문수단 =====

export const orderMethodSchema = z.object({
  orderMethod: z.enum(ORDER_METHOD_OPTIONS, { message: "주문수단을 선택해 주세요." }),
});

export type OrderMethodFormValues = z.infer<typeof orderMethodSchema>;

// ===== Phase E. 배너 · 포토 이미지 =====

export const bannerSchema = z.object({
  imageFileId: z.number({ message: "이미지를 업로드해 주세요." }).int().positive({
    message: "이미지를 업로드해 주세요.",
  }),
  sort: z.number({ message: "정렬 순서를 입력해 주세요." }).int(),
});

export type BannerFormValues = z.infer<typeof bannerSchema>;

export const photoCategorySchema = z.object({
  name: z
    .string()
    .trim()
    .min(1, { message: "카테고리 이름을 입력해 주세요." })
    .max(PHOTO_CATEGORY_NAME_MAX, {
      message: `카테고리 이름은 최대 ${PHOTO_CATEGORY_NAME_MAX}자까지 입력할 수 있습니다.`,
    }),
});

export type PhotoCategoryFormValues = z.infer<typeof photoCategorySchema>;

export const photoImageSchema = z.object({
  imageFileId: z.number({ message: "이미지를 업로드해 주세요." }).int().positive({
    message: "이미지를 업로드해 주세요.",
  }),
  sort: z.number({ message: "정렬 순서를 입력해 주세요." }).int(),
  visible: z.boolean(),
});

export type PhotoImageFormValues = z.infer<typeof photoImageSchema>;

export const photoImageUpdateSchema = z.object({
  imageFileId: z.number({ message: "이미지를 업로드해 주세요." }).int().positive({
    message: "이미지를 업로드해 주세요.",
  }),
  sort: z.number({ message: "정렬 순서를 입력해 주세요." }).int(),
  visible: z.boolean(),
});

export type PhotoImageUpdateFormValues = z.infer<typeof photoImageUpdateSchema>;

// ===== Phase F. 테하 초이스 =====

export const editorChoiceSchema = z.object({
  shopId: z.number({ message: "가게 ID를 입력해 주세요." }).int().positive({
    message: "가게 ID는 양수여야 합니다.",
  }),
  title: z
    .string()
    .trim()
    .min(1, { message: "제목을 입력해 주세요." })
    .max(EDITOR_CHOICE_TITLE_MAX, { message: `제목은 최대 ${EDITOR_CHOICE_TITLE_MAX}자까지 입력할 수 있습니다.` }),
  content: z
    .string()
    .trim()
    .min(1, { message: "내용을 입력해 주세요." })
    .max(EDITOR_CHOICE_CONTENT_MAX, {
      message: `내용은 최대 ${EDITOR_CHOICE_CONTENT_MAX}자까지 입력할 수 있습니다.`,
    }),
});

export type EditorChoiceFormValues = z.infer<typeof editorChoiceSchema>;
