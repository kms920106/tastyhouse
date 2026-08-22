import { productRepository } from '@/domains/product/product.repository'
import ProductNutritionDisclosure from './ProductNutritionDisclosure'

interface Props {
  productId: number
}

/**
 * 메뉴 상세의 "영양성분 및 알레르기성분 표시 보기" 영역.
 *
 * **미입력이면 버튼 자체를 그리지 않는다** — 열었는데 비어 있으면 표시 의무를 지킨 것처럼
 * 오해를 준다. 대부분의 메뉴가 미입력이므로 이 판정을 서버에서 한 번 해 두고, 실제 수치는
 * 펼칠 때 클라이언트가 다시 가져온다(`ProductNutritionDisclosure`).
 *
 * 조회 실패도 감춘다 — 부가 영역이라 메뉴 상세 전체를 막을 이유가 없다.
 */
export default async function ProductNutritionSection({ productId }: Props) {
  const { error, data } = await productRepository.getProductNutrition(productId)

  if (error || !data) {
    return null
  }

  return <ProductNutritionDisclosure productId={productId} />
}
