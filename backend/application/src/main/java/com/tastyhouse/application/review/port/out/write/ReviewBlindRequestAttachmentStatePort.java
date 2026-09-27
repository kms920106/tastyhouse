package com.tastyhouse.application.review.port.out.write;

import java.util.List;

public interface ReviewBlindRequestAttachmentStatePort {
    List<ReviewBlindRequestAttachmentState> saveAll(List<ReviewBlindRequestAttachmentState> states);
}
