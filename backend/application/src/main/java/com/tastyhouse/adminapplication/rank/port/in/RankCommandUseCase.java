package com.tastyhouse.adminapplication.rank.port.in;

/**
 * 랭킹 쓰기 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code RankCommandService})을 알지 않는다. 도입 근거는
 * 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 *
 * <p>집계·기간·경품은 모두 하나의 랭킹 컨텍스트를 다루는 단일 컨트롤러({@code RankApiController})의
 * 연산이므로 연산별로 쪼개지 않고 한 포트에 모은다.
 */
public interface RankCommandUseCase {

    void aggregate(RankAggregateCommand command);

    Long createPeriod(RankPeriodCreateCommand command);

    void updatePeriod(RankPeriodUpdateCommand command);

    void deletePeriod(RankPeriodDeleteCommand command);

    Long createPrize(RankPrizeCreateCommand command);

    void updatePrize(RankPrizeUpdateCommand command);

    void deletePrize(RankPrizeDeleteCommand command);
}
