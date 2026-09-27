package com.tastyhouse.application.ceo.port.in;

import java.util.List;

import com.tastyhouse.application.ceo.port.out.CeoReplyPhraseResult;
import com.tastyhouse.application.shared.marker.CeoApp;

@CeoApp
public interface CeoReplyPhraseQueryUseCase {

    List<CeoReplyPhraseResult> getReplyPhrases(Long ceoId);
}
