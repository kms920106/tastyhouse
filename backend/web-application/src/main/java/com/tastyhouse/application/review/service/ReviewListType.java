package com.tastyhouse.application.review.service;

import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.WebErrorCode;

public enum ReviewListType {

    ALL,
    FOLLOWING;

    public static ReviewListType from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new ApplicationException(WebErrorCode.REVIEW_LIST_TYPE_UNKNOWN,
                WebErrorCode.REVIEW_LIST_TYPE_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}
