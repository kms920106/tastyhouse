package com.tastyhouse.application.event.port.in;

public interface EventWinnerCreateUseCase {

    Long createWinner(EventWinnerCreateCommand command);
}
