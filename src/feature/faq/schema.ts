import { z } from "zod";

export const FAQ_QUESTION_MAX = 500;
export const FAQ_CATEGORY_NAME_MAX = 100;

export const faqCategoryFormSchema = z.object({
  name: z
    .string()
    .trim()
    .min(1, { message: "카테고리 이름을 입력해 주세요." })
    .max(FAQ_CATEGORY_NAME_MAX, {
      message: `카테고리 이름은 최대 ${FAQ_CATEGORY_NAME_MAX}자까지 입력할 수 있습니다.`,
    }),
  sort: z.number().int({ message: "정렬 순서는 정수로 입력해 주세요." }),
  visible: z.boolean(),
});

export type FaqCategoryFormValues = z.infer<typeof faqCategoryFormSchema>;

export const faqFormSchema = z.object({
  faqCategoryId: z.number().int().positive({ message: "카테고리를 선택해 주세요." }),
  question: z
    .string()
    .trim()
    .min(1, { message: "질문을 입력해 주세요." })
    .max(FAQ_QUESTION_MAX, {
      message: `질문은 최대 ${FAQ_QUESTION_MAX}자까지 입력할 수 있습니다.`,
    }),
  answer: z.string().trim().min(1, { message: "답변을 입력해 주세요." }),
  sort: z.number().int({ message: "정렬 순서는 정수로 입력해 주세요." }),
  visible: z.boolean(),
});

export type FaqFormValues = z.infer<typeof faqFormSchema>;
