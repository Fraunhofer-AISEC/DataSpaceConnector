/*
 *  Copyright (c) 2022 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Bayerische Motoren Werke Aktiengesellschaft (BMW AG) - initial API and implementation
 *
 */

package org.eclipse.edc.service.spi.result;

import org.eclipse.edc.spi.result.AbstractResult;
import org.eclipse.edc.spi.result.StoreResult;

import java.util.List;

import static org.eclipse.edc.service.spi.result.ServiceFailure.Reason.BAD_REQUEST;
import static org.eclipse.edc.service.spi.result.ServiceFailure.Reason.CONFLICT;
import static org.eclipse.edc.service.spi.result.ServiceFailure.Reason.NOT_FOUND;

public class ServiceResult<T> extends AbstractResult<T, ServiceFailure> {

    protected ServiceResult(T content, ServiceFailure failure) {
        super(content, failure);
    }

    public static <T> ServiceResult<T> success(T content) {
        return new ServiceResult<>(content, null);
    }

    public static <T> ServiceResult<T> conflict(String message) {
        return new ServiceResult<>(null, new ServiceFailure(List.of(message), CONFLICT));
    }

    public static <T> ServiceResult<T> notFound(String message) {
        return new ServiceResult<>(null, new ServiceFailure(List.of(message), NOT_FOUND));
    }

    public static <T> ServiceResult<T> badRequest(String... message) {
        return badRequest(List.of(message));
    }

    public static <T> ServiceResult<T> badRequest(List<String> messages) {
        return new ServiceResult<>(null, new ServiceFailure(messages, BAD_REQUEST));
    }

    public static <T> ServiceResult<T> success() {
        return ServiceResult.success(null);
    }

    public static <T> ServiceResult<T> from(StoreResult<T> storeResult) {
        if (storeResult.succeeded()) {
            return success(storeResult.getContent());
        }
        switch (storeResult.reason()) {
            case NOT_FOUND:
                return notFound(storeResult.getFailureDetail());
            case ALREADY_EXISTS:
                return conflict(storeResult.getFailureDetail());
            default:
                return badRequest(storeResult.getFailureDetail());
        }
    }

    public static <T> ServiceResult<T> fromFailure(StoreResult<?> storeResult) {
        if (storeResult.succeeded()) {
            throw new IllegalArgumentException("Can only use this method when the argument is a failed result!");
        }
        switch (storeResult.reason()) {
            case NOT_FOUND:
                return notFound(storeResult.getFailureDetail());
            case ALREADY_EXISTS:
                return conflict(storeResult.getFailureDetail());
            default:
                return badRequest(storeResult.getFailureDetail());
        }
    }

    public ServiceFailure.Reason reason() {
        return getFailure().getReason();
    }
}
