/*
 *  Copyright (c) 2022 Fraunhofer AISEC
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Fraunhofer AISEC
 *
 */

package org.eclipse.edc.connector.dataplane.idscp2;

import org.eclipse.edc.junit.extensions.EdcExtension;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(EdcExtension.class)
public class DataPlaneIdscp2ExtensionTest {
    static DataPlaneIdscp2Extension ie = null;
    @BeforeAll
    public static void setUp() {
        ie = new DataPlaneIdscp2Extension();
    }

    @AfterAll
    public static void tearDown() {
        //
    }

    @Test
    void sendToClient(){
      //

    }

    @Test
    void receive(){
        //

    }
}
