package org.eclipse.edc.connector.dataplane.idscp2.pipeline;
import de.fhg.aisec.ids.idscp2.api.configuration.AttestationConfig;
import de.fhg.aisec.ids.idscp2.api.configuration.Idscp2Configuration;
import de.fhg.aisec.ids.idscp2.api.connection.Idscp2Connection;
import de.fhg.aisec.ids.idscp2.api.connection.Idscp2ConnectionAdapter;
import de.fhg.aisec.ids.idscp2.api.raregistry.RaProverDriverRegistry;
import de.fhg.aisec.ids.idscp2.api.raregistry.RaVerifierDriverRegistry;
import de.fhg.aisec.ids.idscp2.core.connection.Idscp2ConnectionImpl;
import de.fhg.aisec.ids.idscp2.daps.aisecdaps.AisecDapsDriver;
import de.fhg.aisec.ids.idscp2.daps.aisecdaps.AisecDapsDriverConfig;
import de.fhg.aisec.ids.idscp2.daps.aisecdaps.SecurityProfile;
import de.fhg.aisec.ids.idscp2.daps.aisecdaps.SecurityRequirements;
import de.fhg.aisec.ids.idscp2.defaultdrivers.remoteattestation.demo.DemoRaProver;
import de.fhg.aisec.ids.idscp2.defaultdrivers.remoteattestation.demo.DemoRaVerifier;
import de.fhg.aisec.ids.idscp2.defaultdrivers.securechannel.tls13.NativeTLSDriver;
import de.fhg.aisec.ids.idscp2.defaultdrivers.securechannel.tls13.NativeTlsConfiguration;
import de.fhg.aisec.ids.idscp2.keystores.KeyStoreUtil;

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
import java.util.concurrent.CompletableFuture;

import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Idscp2ClientJ {
    private static final Logger LOG = LoggerFactory.getLogger(Idscp2Client.class);

    private NativeTLSDriver<Idscp2Connection> secureChannelDriver;
    private Idscp2Configuration config;
    private NativeTlsConfiguration tlsConfig;

    public void  init(String host, String alias, ServiceExtensionContext context) {
        // register ra drivers
        RaProverDriverRegistry regProv;
        regProv.registerDriver(
                DemoRaProver.DEMO_RA_PROVER_ID,
                DemoRaProver.class,
                null
        );

        RaVerifierDriverRegistry regVer;
        regVer.registerDriver(
                DemoRaVerifier.DEMO_RA_VERIFIER_ID,
                DemoRaVerifier.class,
                null
        );

        char[] password = context.getConfig().getString("edc.web.idscp2.keystore.password", "keystore-password").toCharArray();
        Path keyStorePath = Paths.get(context.getConfig().getString("edc.web.idscp2.keystore.path", "dataspaceconnector-keystore.jks"));
        Path trustStorePath = Paths.get(context.getConfig().getString("edc.web.idscp2.keystore.path", "dataspaceconnector-truststore.jks"));

        Monitor monitor = context.getMonitor();
        monitor.info("Path:" +keyStorePath);

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

    public void send(String message) {
        // connect to idscp2 server
        CompletableFuture<Idscp2Connection> connectionFuture = secureChannelDriver.connect(Idscp2ConnectionImpl.class, config, tlsConfig);
        connectionFuture.thenAccept(connection -> {
            LOG.info("Client: New connection with id " + connection.getId());
            connection.addConnectionListener(new Idscp2ConnectionAdapter() {
                @Override
                public void onError(Throwable t) {
                   LOG.error("Client connection error occurred", t);
                }

                @Override
                public void onClose() {
                    LOG.info("Client: Connection with id " + connection.getId() + " has been closed");
                }
            });
            connection.addMessageListener((c, data) -> {
                LOG.info("Received ping message: " + new String(data, StandardCharsets.UTF_8));
                c.close();
            });
            connection.unlockMessaging();
            LOG.info("Send Message");
            connection.nonBlockingSend(message.getBytes(StandardCharsets.UTF_8));
            LOG.info("Local DAT: " + new String(connection.getLocalDynamicAttributeToken(), StandardCharsets.UTF_8));
        }).exceptionally(t -> {
            LOG.error("Client endpoint error occurred", t);
            return null;
        });
    }
}
