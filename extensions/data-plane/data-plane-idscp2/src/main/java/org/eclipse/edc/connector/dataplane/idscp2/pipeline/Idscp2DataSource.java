/*
 *  Copyright (c) 2023 Fraunhofer AISEC
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Fraunhofer AISEC - implementation
 *
 */

package org.eclipse.edc.connector.dataplane.idscp2.pipeline;

import de.fhg.aisec.ids.idscp2.api.Idscp2EndpointListener;
import de.fhg.aisec.ids.idscp2.api.configuration.AttestationConfig;
import de.fhg.aisec.ids.idscp2.api.configuration.Idscp2Configuration;
import de.fhg.aisec.ids.idscp2.api.connection.Idscp2Connection;
import de.fhg.aisec.ids.idscp2.api.connection.Idscp2ConnectionAdapter;
import de.fhg.aisec.ids.idscp2.api.raregistry.RaProverDriverRegistry;
import de.fhg.aisec.ids.idscp2.api.raregistry.RaVerifierDriverRegistry;
import de.fhg.aisec.ids.idscp2.daps.aisecdaps.AisecDapsDriver;
import de.fhg.aisec.ids.idscp2.daps.aisecdaps.AisecDapsDriverConfig;
import de.fhg.aisec.ids.idscp2.daps.aisecdaps.SecurityProfile;
import de.fhg.aisec.ids.idscp2.daps.aisecdaps.SecurityRequirements;
import de.fhg.aisec.ids.idscp2.defaultdrivers.remoteattestation.demo.DemoRaProver;
import de.fhg.aisec.ids.idscp2.defaultdrivers.remoteattestation.demo.DemoRaVerifier;
import de.fhg.aisec.ids.idscp2.defaultdrivers.securechannel.tls13.NativeTLSDriver;
import de.fhg.aisec.ids.idscp2.defaultdrivers.securechannel.tls13.NativeTlsConfiguration;
import de.fhg.aisec.ids.idscp2.keystores.KeyStoreUtil;
import org.eclipse.edc.connector.dataplane.spi.pipeline.DataSource;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class Idscp2DataSource implements DataSource, Idscp2EndpointListener<Idscp2Connection> {
    private static final Logger LOG = LoggerFactory.getLogger(Idscp2DataSink.class);
    private NativeTLSDriver<Idscp2Connection> secureChannelDriver = new NativeTLSDriver<>();
    private Idscp2Configuration config;
    private NativeTlsConfiguration tlsConfig;
    private Monitor monitor;
    private String name;

    public void  init(String host, Integer port, String alias, ServiceExtensionContext context)  {
        // register ra drivers
        RaProverDriverRegistry.INSTANCE.registerDriver(
                DemoRaProver.DEMO_RA_PROVER_ID,
                DemoRaProver::new,
                null
        );

        RaVerifierDriverRegistry.INSTANCE.registerDriver(
                DemoRaVerifier.DEMO_RA_VERIFIER_ID,
                DemoRaVerifier::new,
                null
        );

        char[] password = context.getConfig().getString("edc.web.idscp2.keystore.password", "keystore-password").toCharArray();
        Path keyStorePath = Paths.get(context.getConfig().getString("edc.web.idscp2.keystore.path", "dataspaceconnector-keystore.jks"));
        Path trustStorePath = Paths.get(context.getConfig().getString("edc.web.idscp2.keystore.path", "dataspaceconnector-truststore.jks"));

        Monitor monitor = context.getMonitor();

        SecurityRequirements securityRequirements = new SecurityRequirements.Builder()
                .setRequiredSecurityLevel(SecurityProfile.INVALID)
                .build();

        KeyStore ks = KeyStoreUtil.INSTANCE.loadKeyStore(keyStorePath, password);
        List<X509Certificate> certificates = getKeyChain(ks);

        AisecDapsDriver dapsDriver = new AisecDapsDriver(
                new AisecDapsDriverConfig.Builder()
                        .setKeyStorePath(keyStorePath)
                        .setKeyStorePassword(password)
                        .setKeyPassword(password)
                        .setKeyAlias("1")
                        .setTrustStorePath(trustStorePath)
                        .setTrustStorePassword(password)
                        .setDapsUrl("https://daps-dev.aisec.fraunhofer.de")
                        .setTransportCerts(certificates)
                        .setSecurityRequirements(securityRequirements)
                        .build()
        );

        AttestationConfig localAttestationConfig = new AttestationConfig.Builder()
                .setSupportedRaSuite(new String[] {DemoRaProver.DEMO_RA_PROVER_ID})
                .setExpectedRaSuite(new String[] {DemoRaVerifier.DEMO_RA_VERIFIER_ID})
                .setRaTimeoutDelay(300 * 1000L) // 300 seconds
                .build();

        config = new Idscp2Configuration.Builder()
                .setAckTimeoutDelay(500) //  500 ms
                .setHandshakeTimeoutDelay(5 * 1000L) // 5 seconds
                .setAttestationConfig(localAttestationConfig)
                .setDapsDriver(dapsDriver)
                .build();

        tlsConfig = new NativeTlsConfiguration.Builder()
                .setKeyStorePath(keyStorePath)
                .setKeyStorePassword(password)
                .setKeyPassword(password)
                .setTrustStorePath(trustStorePath)
                .setTrustStorePassword(password)
                .setCertificateAlias(alias)
                .setHost(host)
                .setServerPort(port)
                .build();
    }

    public static List<X509Certificate> getKeyChain(KeyStore keyStore) {
        List<X509Certificate> certificates = new ArrayList<>();
        try {
            Enumeration<String> aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                if (keyStore.isKeyEntry(alias)) {
                    Certificate[] chain = keyStore.getCertificateChain(alias);
                    if (chain != null && chain.length > 0 && chain[0] instanceof X509Certificate) {
                        certificates.add((X509Certificate) chain[0]);
                    }
                }
            }
        } catch (KeyStoreException e) {
            // handle exception
            e.printStackTrace();
        }
        return certificates;
    }

    @Override
    public Stream<Part> openPartStream() {
        return null;
    }

    public interface ServerListener {
        void response(String res);
    }

    private ServerListener listener = null;

    public void setListener(ServerListener listener) {
        this.listener = listener;
    }



    @Override
    public void onConnection(Idscp2Connection connection) {
        LOG.info("Server: New connection with id " + connection.getId());
        connection.addConnectionListener(new Idscp2ConnectionAdapter() {
            @Override
            public void onError(Throwable t) {
                LOG.error("Server connection error occurred", t);
            }

            @Override
            public void onClose() {
                LOG.info("Server: Connection with id " + connection.getId() + " has been closed");
            }
        });
        connection.addMessageListener((c, data) -> {
            LOG.info("Received message: " + new String(data, StandardCharsets.UTF_8).trim());
            // TODO:
            // Do something with received message
            if (listener != null) {
                listener.response(data.toString());
            }
        });
    }

    public static class Builder {
        private final Idscp2DataSource dataSource;

        public static Builder newInstance() {
            return new Builder();
        }

        private Builder() {
            dataSource = new Idscp2DataSource();
        }

        public Builder name(String name) {
            dataSource.name = name;
            return this;
        }

        public Builder monitor(Monitor monitor) {
            dataSource.monitor = monitor;
            return this;
        }

        public Idscp2DataSource build() {
            Objects.requireNonNull(dataSource.monitor, "monitor");
            return dataSource;
        }
    }
}
