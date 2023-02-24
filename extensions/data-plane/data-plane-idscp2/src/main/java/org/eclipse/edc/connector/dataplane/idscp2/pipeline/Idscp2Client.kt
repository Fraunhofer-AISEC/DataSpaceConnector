package org.eclipse.edc.connector.dataplane.idscp2.pipeline

import de.fhg.aisec.ids.idscp2.api.configuration.AttestationConfig
import de.fhg.aisec.ids.idscp2.api.configuration.Idscp2Configuration
import de.fhg.aisec.ids.idscp2.api.connection.Idscp2Connection
import de.fhg.aisec.ids.idscp2.api.connection.Idscp2ConnectionAdapter
import de.fhg.aisec.ids.idscp2.api.raregistry.RaProverDriverRegistry
import de.fhg.aisec.ids.idscp2.api.raregistry.RaVerifierDriverRegistry
import de.fhg.aisec.ids.idscp2.core.connection.Idscp2ConnectionImpl
import de.fhg.aisec.ids.idscp2.daps.aisecdaps.AisecDapsDriver
import de.fhg.aisec.ids.idscp2.daps.aisecdaps.AisecDapsDriverConfig
import de.fhg.aisec.ids.idscp2.daps.aisecdaps.SecurityProfile
import de.fhg.aisec.ids.idscp2.daps.aisecdaps.SecurityRequirements
import de.fhg.aisec.ids.idscp2.defaultdrivers.remoteattestation.demo.DemoRaProver
import de.fhg.aisec.ids.idscp2.defaultdrivers.remoteattestation.demo.DemoRaVerifier
import de.fhg.aisec.ids.idscp2.defaultdrivers.securechannel.tls13.NativeTLSDriver
import de.fhg.aisec.ids.idscp2.defaultdrivers.securechannel.tls13.NativeTlsConfiguration
import de.fhg.aisec.ids.idscp2.keystores.KeyStoreUtil
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.nio.file.Paths
import java.security.KeyStore
import java.security.cert.X509Certificate
import org.eclipse.edc.spi.system.ServiceExtensionContext
import org.slf4j.LoggerFactory

class Idscp2Client {
    private val secureChannelDriver = NativeTLSDriver<Idscp2Connection>()
    private var config =  Idscp2Configuration();
    private lateinit var tlsConfig: NativeTlsConfiguration;

    fun init(host: String, alias: String, context: ServiceExtensionContext) {
        // register ra drivers
        RaProverDriverRegistry.registerDriver(
                DemoRaProver.DEMO_RA_PROVER_ID,
                ::DemoRaProver,
                null
        )

        RaVerifierDriverRegistry.registerDriver(
                DemoRaVerifier.DEMO_RA_VERIFIER_ID,
                ::DemoRaVerifier,
                null
        )

        val password =context.config.getString("edc.web.idscp2.keystore.password", "keystore-password").toCharArray();
        val keyStorePath: Path = Paths.get(context.config.getString("edc.web.idscp2.keystore.path", "dataspaceconnector-keystore.jks"));
        val trustStorePath: Path = Paths.get(context.config.getString("edc.web.idscp2.keystore.path", "dataspaceconnector-truststore.jks"));

        val monitor = context.monitor
        monitor.info("Path:" +keyStorePath)

        val securityRequirements = SecurityRequirements.Builder()
                .setRequiredSecurityLevel(SecurityProfile.INVALID)
                .build()

        // Load certificates from local KeyStore
        val ks = KeyStoreUtil.loadKeyStore(keyStorePath, password)
        val certificates = ks.aliases().asSequence().toList()
                .filter { ks.isKeyEntry(it) }
                .map { ks.getCertificateChain(it)[0] as X509Certificate }

        val dapsDriver = AisecDapsDriver(
                AisecDapsDriverConfig.Builder()
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
        )

        val localAttestationConfig = AttestationConfig.Builder()
                .setSupportedRaSuite(arrayOf(DemoRaProver.DEMO_RA_PROVER_ID))
                .setExpectedRaSuite(arrayOf(DemoRaVerifier.DEMO_RA_VERIFIER_ID))
                .setRaTimeoutDelay(300 * 1000L) // 300 seconds
                .build()

        config = Idscp2Configuration.Builder()
                .setAckTimeoutDelay(500) //  500 ms
                .setHandshakeTimeoutDelay(5 * 1000L) // 5 seconds
                .setAttestationConfig(localAttestationConfig)
                .setDapsDriver(dapsDriver)
                .build()

        tlsConfig = NativeTlsConfiguration.Builder()
                .setKeyStorePath(keyStorePath)
                .setKeyStorePassword(password)
                .setKeyPassword(password)
                .setTrustStorePath(trustStorePath)
                .setTrustStorePassword(password)
                .setCertificateAlias(alias)
                .setHost(host)
                .build()
    }

    fun send (message: String) {
        // connect to idscp2 server
        val connectionFuture = secureChannelDriver.connect(::Idscp2ConnectionImpl, config, tlsConfig)
        connectionFuture.thenAccept { connection: Idscp2Connection ->
            LOG.info("Client: New connection with id " + connection.id)
            connection.addConnectionListener(object : Idscp2ConnectionAdapter() {
                override fun onError(t: Throwable) {
                    LOG.error("Client connection error occurred", t)
                }

                override fun onClose() {
                    LOG.info("Client: Connection with id " + connection.id + " has been closed")
                }
            })
            connection.addMessageListener { c: Idscp2Connection, data: ByteArray ->
                LOG.info("Received ping message: " + String(data, StandardCharsets.UTF_8))
                c.close()
            }
            connection.unlockMessaging()
            LOG.info("Send Message")
            connection.nonBlockingSend(message.toByteArray(StandardCharsets.UTF_8))
            LOG.info("Local DAT: " + String(connection.localDat, StandardCharsets.UTF_8))
        }.exceptionally { t: Throwable? ->
            LOG.error("Client endpoint error occurred", t)
            null
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(Idscp2Client::class.java)
    }

    fun convertKeystore(jksFile: String, jksPassword: String, p12File: String, p12Password: String) {
        val jksKeyStore = KeyStore.getInstance("JKS")
        FileInputStream(jksFile).use {
            jksKeyStore.load(it, jksPassword.toCharArray())
        }

        val p12KeyStore = KeyStore.getInstance("PKCS12")
        p12KeyStore.load(null, null)

        val aliases = jksKeyStore.aliases()
        while (aliases.hasMoreElements()) {
            val alias = aliases.nextElement()
            val key = jksKeyStore.getKey(alias, jksPassword.toCharArray())
            val chain = jksKeyStore.getCertificateChain(alias)
            p12KeyStore.setKeyEntry(alias, key, p12Password.toCharArray(), chain)
        }

        FileOutputStream(p12File).use {
            p12KeyStore.store(it, p12Password.toCharArray())
        }
    }
}