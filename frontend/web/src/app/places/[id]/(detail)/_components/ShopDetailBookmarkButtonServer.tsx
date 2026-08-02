import { shopRepository } from '@/domains/shop/shop.repository'
import { getIsLoggedIn } from '@/lib/auth-config'
import { PAGE_PATHS } from '@/lib/paths'
import Link from 'next/link'
import { FaRegBookmark } from 'react-icons/fa'
import ShopDetailBookmarkButtonClient from './ShopDetailBookmarkButtonClient'

interface Props {
  shopId: number
}

export default async function ShopDetailBookmarkButtonServer({ shopId }: Props) {
  const isLoggedIn = await getIsLoggedIn()

  if (!isLoggedIn) {
    return (
      <Link
        href={PAGE_PATHS.AUTH_LOGIN}
        className="flex items-center justify-center w-[35px] h-[35px] shrink-0 border border-line box-border rounded-full cursor-pointer"
      >
        <FaRegBookmark size={16} className="text-line" />
      </Link>
    )
  }

  const { error, data } = await shopRepository.getShopBookmark(shopId)

  if (error) {
    return (
      <button className="flex items-center justify-center w-[35px] h-[35px] shrink-0 border border-line box-border rounded-full cursor-pointer">
        <FaRegBookmark size={16} className="text-line" />
      </button>
    )
  }

  if (!data) {
    return (
      <button className="flex items-center justify-center w-[35px] h-[35px] shrink-0 border border-line box-border rounded-full cursor-pointer">
        <FaRegBookmark size={16} className="text-line" />
      </button>
    )
  }

  const { bookmarked } = data

  return <ShopDetailBookmarkButtonClient initialIsBookmarked={bookmarked} shopId={shopId} />
}

/*
  분리하지 않아야 하는 이유

  1. 추상화의 비용 > 이득
  분리하면 오히려 props로 다양한 케이스를 다뤄야 해서 복잡도가 증가합니다.

  // 분리한다면 이렇게 될 것:
  <BookmarkButton
    variant="static"
    isBookmarked={false}
    onClick={undefined}
    disabled={true}
  />

  // vs 현재 코드 (명확함)
  <button className="...">
    <FaRegBookmark size={16} className="text-line" />
  </button>

  2. 의미적으로 다른 컴포넌트
  BookmarkButtonContent: 서버 컴포넌트, 인증/데이터 체크 후 적절한 UI 선택하는 라우터 역할
  BookmarkButtonClient: 클라이언트 컴포넌트, 인터랙션 처리하는 실제 버튼
  에러/fallback UI는 "버튼처럼 생긴 정적 요소"이고, Client는 "실제 동작하는 버튼"입니다. 생김새는 비슷하지만 목적이 다릅니다.

  3. 변경 가능성이 낮음
  에러/fallback UI는 거의 변하지 않는 정적 마크업
  3-4줄의 간단한 JSX
  재사용될 가능성도 낮음 (이 페이지에서만 사용)

  4. YAGNI 원칙
  "You Aren't Gonna Need It" - 실제로 3번째 사용처가 생기거나, 스타일 변경이 여러 곳에서 필요할 때 그때 리팩토링해도 늦지 않습니다.
  컴포넌트 분리를 고려할 시점
  다음 상황이 되면 분리를 고려하세요:
  다른 곳에서도 사용될 때 (실제로 발생 시)
  로직이 복잡해질 때 (조건부 렌더링이 5개 이상 등)
  스타일 variants가 많아질 때 (shadcn/ui Button 처럼)
  테스트가 필요할 정도로 중요해질 때

  결론
  현재는 **premature abstraction(조기 추상화)**에 해당합니다. 코드 중복보다 불필요한 추상화가 더 유지보수를 어렵게 만듭니다. 지금처럼 명확하게 분리된 책임(서버 컴포넌트의 조건 분기 vs 클라이언트 컴포넌트의 인터랙션)을 유지하는 것이 좋습니다. "비슷한 코드 3줄이 추상화보다 낫다" - 이게 10년차의 교훈입니다. 😊
*/
