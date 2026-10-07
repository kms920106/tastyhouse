package com.tastyhouse.application.ceo.port.in;

import java.util.List;

import com.tastyhouse.application.ceo.port.out.CeoReplyPhraseResult;

public interface CeoReplyPhraseListQueryUseCase {

    List<CeoReplyPhraseResult> getReplyPhrases(Long ceoId);
}
