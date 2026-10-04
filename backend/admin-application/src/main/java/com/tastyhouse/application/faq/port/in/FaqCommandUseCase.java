package com.tastyhouse.application.faq.port.in;

public interface FaqCommandUseCase {

    Long createFaq(FaqCreateCommand command);

    void updateFaq(FaqUpdateCommand command);

    void deleteFaq(FaqDeleteCommand command);
}
