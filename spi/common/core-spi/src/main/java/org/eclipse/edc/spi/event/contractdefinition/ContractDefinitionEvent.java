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

package org.eclipse.edc.spi.event.contractdefinition;

import org.eclipse.edc.spi.event.Event;

import java.util.Objects;

/**
 *  Class as organizational between level to catch events of type ContractDefinition to catch them together in an Event Subscriber
 *  Contains data related to contract definitions
 */
public abstract class ContractDefinitionEvent extends Event {

    protected String contractDefinitionId;

    public String getContractDefinitionId() {
        return contractDefinitionId;
    }

    public abstract static class Builder<T extends ContractDefinitionEvent, B extends Builder<T, B>> {

        protected final T event;

        protected Builder(T event) {
            this.event = event;
        }


        public B contractDefinitionId(String contractDefinitionId) {
            event.contractDefinitionId = contractDefinitionId;
            return self();
        }

        public abstract B self();


        public T build() {
            Objects.requireNonNull(event.contractDefinitionId);
            return event;
        }

    }
}
