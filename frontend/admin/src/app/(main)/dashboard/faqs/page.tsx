import { faqService } from "@/api/faq/faq.service";
import { FAQ_MESSAGE } from "@/feature/faq/message";
import logger from "@/lib/logger";
import { parseNonNegativeInt, parseOptionalBoolean, parseSearchString } from "@/lib/utils";

import { Faqs } from "./_components/faqs";

export default async function Page({ searchParams }: PageProps<"/dashboard/faqs">) {
  const {
    page: pageParam,
    size: sizeParam,
    categoryId: categoryIdParam,
    question: questionParam,
    visible: visibleParam,
  } = await searchParams;

  const question = parseSearchString(questionParam);
  const visible = parseOptionalBoolean(visibleParam);
  const page = parseNonNegativeInt(pageParam, 0);
  const size = parseNonNegativeInt(sizeParam, 10);

  const rawCategoryId = Array.isArray(categoryIdParam) ? categoryIdParam[0] : categoryIdParam;
  const parsedCategoryId = Number(rawCategoryId);
  const categoryId = Number.isInteger(parsedCategoryId) && parsedCategoryId > 0 ? parsedCategoryId : undefined;

  const [{ error, data, pagination }, { error: categoriesError, data: categories }] = await Promise.all([
    faqService.getFaqs({ categoryId, question, visible }, { page, size }),
    faqService.getCategories(),
  ]);

  if (error || !data || !pagination) {
    logger.error({ reason: error, data, pagination }, "FAQ 목록 조회 실패");
    throw new Error(FAQ_MESSAGE.LIST_LOAD_FAILED);
  }

  if (categoriesError || !categories) {
    logger.error({ reason: categoriesError, categories }, "FAQ 카테고리 목록 조회 실패");
    throw new Error(FAQ_MESSAGE.LIST_LOAD_FAILED);
  }

  return (
    <Faqs
      faqs={data}
      pagination={pagination}
      categories={categories}
      initialCategoryId={categoryId}
      initialQuestion={question}
      initialVisible={visible}
    />
  );
}
