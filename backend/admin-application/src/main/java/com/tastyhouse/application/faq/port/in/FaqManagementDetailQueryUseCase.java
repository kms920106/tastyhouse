package com.tastyhouse.application.faq.port.in;

import com.tastyhouse.application.faq.port.out.FaqDetailResult;

public interface FaqManagementDetailQueryUseCase {

    FaqDetailResult getFaq(Long id);
}
