# ceo-application

**ceo(점주, `ceo-api`) 하나만 쓰는 application 계층을 담는 앱 모듈.** 앱 마커 제거로 신설됐다. 과거에는 이 코드가 `application` 한 모듈 안에서 `@CeoApp` 마커로 구분됐으나, 지금은 **이 모듈에 있다는 사실 자체가 "ceo 소속"** 이다. 5모듈 공통 규칙의 정본은 `backend/application/AGENTS.md`(코어)이고, 모듈 지도와 배치 기준은 `backend/CLAUDE.md`의 "앱 모듈 경계 규칙" 절이다. 이 문서는 요약만 둔다.

## 무엇이 여기 사는가

- ceo 전용 `@Service`/`@Component` 빈 — 유스케이스당 서비스 1개(`{도메인}{동작}Service`/`{도메인}{관점}QueryService` 188개 — 명령 120 · 조회 68. 예: `auth/service/CeoLoginService`·`shop/service/ShopRequestListQueryService`. 유스케이스 분리 전의 `{도메인}CommandService`/`{도메인}QueryService` 88개는 하나도 남지 않았다)와 ceo만 쓰는 도메인 서비스·검증기(예: 가게 소유권 검증기 `shop/service/ShopOwnershipValidator`, 요청 취소 도메인 서비스 `shop/service/ShopRequestCancellationService` — 유스케이스 분리 전 이름 `ShopRequestCancelService`)
- UseCase 인터페이스와 Command record(`<ctx>/port/in/`). multipart 문자열 파트에서 역직렬화되는 `shop/port/in/ShopStorePriceVerificationItemCommand`도 여기 있다 — 정적 참조가 없어도 죽은 코드가 아니다(`shop/service/ShopStorePriceVerificationRequestService`가 `ObjectMapper`로 만든다 — 유스케이스 분리 전 이름 `ShopStorePriceVerificationCommandService`).
- **ceo 전용 SPI 포트**: `ceo.port.out.ReplyPhraseTextValidatorPort` — 자주 쓰는 답글 문구의 금칙어 검수 포트. 구현 `shop/service/ReplyPhraseProhibitedWordValidatorAdapter`(이 모듈)와 유일한 소비자가 모두 ceo라 코어에서 옮겨왔다. 이 포트를 쓰는 이유(컨텍스트 경계 때문에 `ProhibitedWordValidator`를 직접 부르지 않음)는 `backend/application/AGENTS.md`의 봉인 항목에 있다. 그 밖의 `port.out` 계약(읽기 계약 포함)은 전부 코어에 있다.
- 자바 패키지는 코어와 같은 `com.tastyhouse.application.<ctx>..`다(split package). 클래스를 코어와 이 모듈 사이로 옮겨도 import는 바뀌지 않는다.

## 의존

- `api project(':application')` — 코어(공유 도메인 서비스·`port.out` 계약·리스너·`shared/**`)를 이 모듈의 소비자에게 함께 노출한다.
- `implementation project(':domain')`, `implementation project(':security-core')`, 그리고 `spring-security-core`·`spring-web`·`jackson-databind`·`spring-tx`.
- **이 모듈을 의존하는 쪽**: `ceo-api`(`implementation`)뿐이다.
- **다른 앱 모듈(`web`·`admin`·`batch-application`)을 의존하지 않는다.** 앱 간 수평 의존을 컴파일러가 막는 것이 이 모듈이 존재하는 이유다.

## 규칙

- **`@Configuration`과 도메인 이벤트 리스너(`@TransactionalEventListener`)를 여기 두지 않는다** — 코어에만 둔다(`backend/application/src/test/java/com/tastyhouse/application/architecture/LayerRulesTest.java` → `listenersAndConfigsShouldResideInCore`). 리스너가 앱 모듈에 있으면 다른 앱이 같은 이벤트를 발행할 때 후속 처리가 조용히 사라진다.
- **빈이면 `@Service`/`@Component`를 단다.** 앱 마커는 없다.
- **유스케이스 하나 = 포트 하나 = 서비스 하나다.** 포트(`<ctx>/port/in/*UseCase`)는 추상 메서드 1개, 서비스는 그 포트 1개만 구현하고 이름은 포트명의 `UseCase`→`Service`, 생성자를 뺀 public 메서드는 1개다. 가드는 코어 `backend/application/src/test/java/com/tastyhouse/application/architecture/LayerRulesTest.java`의 `useCaseServicesShouldImplementExactlyOneUseCase`·`useCasesShouldDeclareSingleOperation`·`useCaseServiceNameShouldMatchPort`·`useCaseServicesShouldHaveSinglePublicOperation`이고, 명령/조회 판별은 testFixtures `UseCaseServices`(구현 포트명이 `QueryUseCase`로 끝나면 조회)다. 근거는 `backend/application/AGENTS.md`의 봉인·가드 목록 "유스케이스 서비스 1:1 규칙 4종" 항목이다. 여러 연산이 함께 쓰는 로딩·검증은 같은 패키지의 `{도메인}Owner{명사}Reader`/`Validator`(`@Component`)로 뺀다 — 이 협력 빈은 `ServiceContextBoundaryTest`의 검사 대상이 된다.
- **이 모듈 전용 에러코드는 `CeoErrorCode`(`backend/ceo-application/src/main/java/com/tastyhouse/application/shared/exception/CeoErrorCode.java`)에 둔다; 두 번째 앱이 쓰면 코어 `ApplicationErrorCode`로 올린다.** 예외는 `ApplicationException(CeoErrorCode.X)`로 던진다. 규칙 정본은 `backend/application/AGENTS.md`의 봉인·가드 목록 "에러 카탈로그 가드" 항목이다.
- **`ProductAvailabilityFailure`는 `ErrorCodeSpec`을 필드로 싣는 유일한 예외다 (봉인)** — `backend/ceo-application/src/main/java/com/tastyhouse/application/product/service/ProductAvailabilityFailure.java`. 코드를 던지지 않고 싣기만 하는 값 record라 가장 좁은 enum 규칙의 예외이며, 함께 domain `product/model`에서 옮겨 온 `ProductAvailabilityChangeResult`를 ceo-application만 만들고 쓰기 때문에 이 모듈에 둔다. 이 예외를 다른 타입으로 넓히지 않는다.
- **두 번째 앱이 쓰게 되면 코어로 옮긴다.** 이 모듈에는 ceo 하나만 쓰는 것만 둔다.
- **앱 전용 SPI 포트가 생기면 이 모듈이 소유한다** — 그 포트의 구현(벤더)이 ceo에만 조립될 때다. 코어에 두면 다른 앱의 코어 빈이 주입해도 컴파일이 통과해 기동 시점에야 실패한다.
- **이 모듈에는 앱 소속 표식 리소스가 없다.** `ceo-api`의 스캔(`ApplicationLayerScanConfig`)은 필터 없이 `com.tastyhouse.application`을 훑으므로, 클래스패스에 다른 앱 모듈이 섞이면 그 앱의 빈이 전부 뜬다("클래스패스 존재 = 활성화"). `ceo-api`의 `ApplicationModuleClasspathTest`가 `com.tastyhouse.application` 클래스의 출처 모듈 집합이 `{application, ceo-application}`인지를 `ModuleOrigin`으로 판정해 그 사고를 막는다. 과거에는 `src/main/resources/META-INF/tastyhouse/application-module.properties`(`app=ceo`) 표식으로 판정했으나 삭제했다.
- 아키텍처 테스트(ArchUnit)는 이 모듈이 아니라 코어 `application`의 테스트에 있다. 코어가 `testImplementation project(':ceo-application')`로 이 모듈을 본다.
- 공유 테스트 더블은 코어 testFixtures의 `com.tastyhouse.testsupport.<ctx>..`에 있다(`testImplementation(testFixtures(project(':application')))`).
- **상품 가격 조회의 소유 검증은 `ProductOwnerQueryPort#findExposurePeriod`를 재사용한다.** 대상: `backend/ceo-application/src/main/java/com/tastyhouse/application/product/service/ProductPriceQueryService.java` → `getPrices`. 메서드 이름은 노출 기간 조회지만, 조건이 `product.id` 일치 + 미삭제이고 `shopId`를 돌려주므로 이전 `ProductPriceService#loadOwnedProduct`(write 포트 `findAllByShopIdAndIdIn`)와 같은 404(`PRODUCT_NOT_FOUND`)를 낸다. `ProductExposureQueryService#getExposure`도 같은 방식이다. `existsProductInShop`은 shop 링크 기준이라 의미가 달라 쓰지 않는다. 이관 근거는 `backend/CLAUDE.md`의 "간접 경로 판정" 절이다.
- **UseCase 구현 서비스·리스너는 package-private이다**(`public` 금지). 가드는 코어 `backend/application/src/test/java/com/tastyhouse/application/architecture/LayerRulesTest.java`의 `useCaseImplementationsShouldNotBePublic`·`listenersAndConfigsShouldNotBePublic`이고, 정본은 `backend/application/AGENTS.md`의 봉인·가드 목록 "UseCase 구현 서비스·리스너·설정에 `public`을 붙이지 않는다" 항목이다.
