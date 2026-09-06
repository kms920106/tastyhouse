package com.tastyhouse.application.faq.port.in;

import com.tastyhouse.application.shared.marker.AdminApp;

@AdminApp
public interface FaqCommandUseCase {

    Long createFaq(FaqCreateCommand command);

    void updateFaq(FaqUpdateCommand command);

    void deleteFaq(FaqDeleteCommand command);
}
