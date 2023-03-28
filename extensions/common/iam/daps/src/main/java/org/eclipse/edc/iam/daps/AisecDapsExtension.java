/*
 *
 */

package org.eclipse.edc.iam.daps;

import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;

/**
 * Provides specialization of Oauth2 extension to interact with DAPS instance
 */
@Extension(value = AisecDapsExtension.NAME)
public class AisecDapsExtension implements ServiceExtension {

    public static final String NAME = "AISEC-DAPS";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        var monitor = context.getMonitor();
        monitor.info("###############AisecDaps Extension started###############");

    }
}
