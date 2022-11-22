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
 *       Bayerische Motoren Werke Aktiengesellschaft (BMW AG) - initial implementation
 *
 */

package org.eclipse.edc.spi.system.vault;

import org.eclipse.edc.spi.security.PrivateKeyResolver;
import org.jetbrains.annotations.Nullable;

public class NoopPrivateKeyResolver implements PrivateKeyResolver {
    @Override
    public <T> @Nullable T resolvePrivateKey(String id, Class<T> keyType) {
        return null;
    }
}
