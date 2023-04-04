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

import org.eclipse.edc.connector.dataplane.idscp2.pipeline.Idscp2DataSink;
import org.eclipse.edc.connector.dataplane.idscp2.pipeline.Idscp2DataSinkFactory;
import org.eclipse.edc.connector.dataplane.idscp2.pipeline.Idscp2DataSource;
import org.eclipse.edc.connector.dataplane.idscp2.pipeline.Idscp2DataSourceFactory;
import org.eclipse.edc.connector.dataplane.spi.pipeline.PipelineService;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;


/**
 * Provides support for reading data from an IDSCP2 endpoint and sending data to an IDSCP2 endpoint.
 */
@Extension(value = DataPlaneIdscp2Extension.NAME)
public class DataPlaneIdscp2Extension implements ServiceExtension {
    public static final String NAME = "Data Plane IDSCP2";
    Idscp2DataSink client = null;
    Idscp2DataSource server = null;

    @Override
    public String name() {
        return NAME;
    }

    @Inject
    private PipelineService pipelineService;


    @Override
    public void initialize(ServiceExtensionContext context) {


        var monitor = context.getMonitor();
        monitor.info("###############IDSCP2 Extension started###############");

        var sourceFactory = new Idscp2DataSourceFactory();
        pipelineService.registerFactory(sourceFactory);

        var sinkFactory = new Idscp2DataSinkFactory();
        pipelineService.registerFactory(sinkFactory);
    }

    /*
    public void initClient(String host, Integer port) {
        client = new Idscp2DataSink();
        client.init(host, port, "1", context);
    }

    public String send(String message) {
        client.send(message);
        return ""; // TODO
    }

    public void initServer(String host, Integer port) {
        server = new Idscp2DataSource();
        server.setListener(new Idscp2DataSource.ServerListener() {
            @Override
            public void response(String res) {
                //TODO
            }
        });
        server.init(host, port, "1", context);
    }

     */

}
