package com.tastyhouse.application.ceo.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.util.List;

import com.tastyhouse.application.ceo.port.out.CeoReplyPhraseResult;

@CeoApp
public interface CeoReplyPhraseQueryUseCase {

    List<CeoReplyPhraseResult> getReplyPhrases(Long ceoId);
}
