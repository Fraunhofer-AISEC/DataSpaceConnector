/*
 *  Copyright (c) 2021 Microsoft Corporation
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Fraunhofer Institute for Applied and Integrated Security - Base implementation
 *
 */

package org.eclipse.dataspaceconnector.iam.oauth2.core.identity;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.edc.iam.oauth2.identity.Oauth2ServiceImpl;
import org.eclipse.edc.spi.iam.TokenParameters;
import org.eclipse.edc.spi.monitor.Monitor;

@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
@Path("/")
public class DATRequestApiController {

    private final Monitor monitor;
    private final Oauth2ServiceImpl oauth2Service;

    public DATRequestApiController(Monitor monitor, Oauth2ServiceImpl oauth2Service) {
        this.monitor = monitor;
        this.oauth2Service = oauth2Service;
    }

    @GET
    @Path("dat-request")
    public String getDatToken() {
        monitor.info("Received a DAT request");

        var tokenParameters = TokenParameters.Builder.newInstance().audience("audience").scope("idsc:IDS_CONNECTOR_ATTRIBUTES_ALL").build();
        var result = oauth2Service.obtainClientCredentials(tokenParameters);

        if (result.succeeded()) {
            monitor.info("DAT request succeeded");
            return "{\"response\":\"" + result.getContent().getToken() + "\"}";
        }
        monitor.info("DAT request failed");
        monitor.info(result.getFailureDetail());
        return "{\"response\":\"Error processing DAT request\"}";
    }
}