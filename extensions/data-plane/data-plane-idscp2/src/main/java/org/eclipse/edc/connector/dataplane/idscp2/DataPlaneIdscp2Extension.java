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

import org.eclipse.edc.connector.dataplane.idscp2.pipeline.Idscp2Client;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;


/**
 * Provides support for reading data from an IDSCP2 endpoint and sending data to an IDSCP2 endpoint.
 */
@Extension(value = DataPlaneIdscp2Extension.NAME)
public class DataPlaneIdscp2Extension implements ServiceExtension {
    public static final String NAME = "Data Plane IDSCP2";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void initialize(ServiceExtensionContext context) {

        var monitor = context.getMonitor();
        monitor.info("###############IDSCP2 Extension started###############");
        context.getConfig("");


        var client = new Idscp2Client();
        client.init("localhost", "1", context);
        client.send("test-message");
    }
}
