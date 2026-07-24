import { z } from "zod";

export const PRODUCT_NAME_MAX = 200;
export const PRODUCT_DESC_MAX = 1000;
export const OPTION_GROUP_NAME_MAX = 100;
export const OPTION_NAME_MAX = 100;
export const CATEGORY_NAME_MAX = 100;

/** 정률 할인 상한 (%) */
export const DISCOUNT_RATE_MAX = 100;
/** 맵기 단계 상한 */
export const SPICINESS_MAX = 5;

const emptyToUndefined = (value: string) => (value.trim() === "" ? undefined : value.trim());

// 미지정 허용 숫자(0 이상). 폼에서 빈 입력은 undefined 로 넘긴다.
const optionalNonNegativeInt = z.number().int().min(0, { message: "0 이상의 값을 입력해 주세요." }).optional();

export const productFormSchema = z
  .object({
    // 수정 모드에서도 폼에는 포함하되 update 요청 body 에서는 제외한다.
    shopId: z.number({ message: "매장 ID를 입력해 주세요." }).int().positive({ message: "매장 ID는 양수여야 합니다." }),
    productCategoryId: optionalNonNegativeInt,
    name: z
      .string()
      .trim()
      .min(1, { message: "상품 이름을 입력해 주세요." })
      .max(PRODUCT_NAME_MAX, {
        message: `상품 이름은 최대 ${PRODUCT_NAME_MAX}자까지 입력할 수 있습니다.`,
      }),
    description: z
      .string()
      .max(PRODUCT_DESC_MAX, {
        message: `설명은 최대 ${PRODUCT_DESC_MAX}자까지 입력할 수 있습니다.`,
      })
      .transform(emptyToUndefined)
      .optional(),
    originalPrice: z
      .number({ message: "정가를 입력해 주세요." })
      .int()
      .min(0, { message: "정가는 0 이상이어야 합니다." }),
    discountPrice: optionalNonNegativeInt,
    discountRate: z
      .number()
      .min(0, { message: "할인율은 0 이상이어야 합니다." })
      .max(DISCOUNT_RATE_MAX, { message: `할인율은 ${DISCOUNT_RATE_MAX} 이하여야 합니다.` })
      .optional(),
    rating: optionalNonNegativeInt,
    reviewCount: optionalNonNegativeInt,
    representative: z.boolean(),
    spiciness: z
      .number()
      .int()
      .min(0, { message: "맵기 단계는 0 이상이어야 합니다." })
      .max(SPICINESS_MAX, { message: `맵기 단계는 ${SPICINESS_MAX} 이하여야 합니다.` })
      .optional(),
    soldOut: z.boolean(),
    visible: z.boolean(),
    sort: z.number({ message: "정렬 순서를 입력해 주세요." }).int(),
  })
  .superRefine((data, ctx) => {
    // 할인가는 정가를 초과할 수 없다.
    if (data.discountPrice !== undefined && data.discountPrice > data.originalPrice) {
      ctx.addIssue({
        code: "custom",
        message: "할인가는 정가를 초과할 수 없습니다.",
        path: ["discountPrice"],
      });
    }
  });

export type ProductFormValues = z.infer<typeof productFormSchema>;

export const optionGroupSchema = z.object({
  name: z
    .string()
    .trim()
    .min(1, { message: "옵션 그룹 이름을 입력해 주세요." })
    .max(OPTION_GROUP_NAME_MAX, {
      message: `옵션 그룹 이름은 최대 ${OPTION_GROUP_NAME_MAX}자까지 입력할 수 있습니다.`,
    }),
  description: z
    .string()
    .max(OPTION_GROUP_NAME_MAX, {
      message: `설명은 최대 ${OPTION_GROUP_NAME_MAX}자까지 입력할 수 있습니다.`,
    })
    .transform(emptyToUndefined)
    .optional(),
  required: z.boolean(),
  multipleSelect: z.boolean(),
  minSelect: optionalNonNegativeInt,
  maxSelect: z.number().int().min(1, { message: "최대 선택 수는 1 이상이어야 합니다." }).optional(),
  sort: z.number({ message: "정렬 순서를 입력해 주세요." }).int(),
  visible: z.boolean(),
});

export type OptionGroupFormValues = z.infer<typeof optionGroupSchema>;

export const optionSchema = z.object({
  name: z
    .string()
    .trim()
    .min(1, { message: "옵션 이름을 입력해 주세요." })
    .max(OPTION_NAME_MAX, {
      message: `옵션 이름은 최대 ${OPTION_NAME_MAX}자까지 입력할 수 있습니다.`,
    }),
  additionalPrice: z.number({ message: "추가 금액을 입력해 주세요." }).int().min(0, {
    message: "추가 금액은 0 이상이어야 합니다.",
  }),
  sort: z.number({ message: "정렬 순서를 입력해 주세요." }).int(),
  soldOut: z.boolean(),
  visible: z.boolean(),
});

export type OptionFormValues = z.infer<typeof optionSchema>;

export const categorySchema = z.object({
  shopId: z.number({ message: "매장 ID를 입력해 주세요." }).int().positive({
    message: "매장 ID는 양수여야 합니다.",
  }),
  name: z
    .string()
    .trim()
    .min(1, { message: "카테고리 이름을 입력해 주세요." })
    .max(CATEGORY_NAME_MAX, {
      message: `카테고리 이름은 최대 ${CATEGORY_NAME_MAX}자까지 입력할 수 있습니다.`,
    }),
  sort: z.number({ message: "정렬 순서를 입력해 주세요." }).int(),
  visible: z.boolean(),
});

export type CategoryFormValues = z.infer<typeof categorySchema>;

export const productImageSchema = z.object({
  imageFileId: z.number({ message: "이미지를 업로드해 주세요." }).int().positive({
    message: "이미지를 업로드해 주세요.",
  }),
  sort: z.number({ message: "정렬 순서를 입력해 주세요." }).int(),
  visible: z.boolean(),
});

export type ProductImageFormValues = z.infer<typeof productImageSchema>;
