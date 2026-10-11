package com.tastyhouse.application.ceo.port.out;

import java.util.List;

public interface CeoReplyPhraseQueryPort {

    List<CeoReplyPhraseResult> findReplyPhrases(Long ceoId);
}
