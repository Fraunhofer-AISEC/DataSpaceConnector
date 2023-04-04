package org.eclipse.edc.connector.dataplane.idscp2.pipeline;

import org.eclipse.edc.connector.dataplane.spi.pipeline.DataSource;
import org.eclipse.edc.connector.dataplane.spi.pipeline.DataSourceFactory;
import org.eclipse.edc.spi.result.Result;
import org.eclipse.edc.spi.types.domain.transfer.DataFlowRequest;
import org.jetbrains.annotations.NotNull;

public class Idscp2DataSourceFactory implements DataSourceFactory {
    @Override
    public boolean canHandle(DataFlowRequest request) {
        return false;
    }

    @Override
    public @NotNull Result<Boolean> validate(DataFlowRequest request) {
        return null;
    }

    @Override
    public DataSource createSource(DataFlowRequest request) {
        return null;
    }

    @Override
    public @NotNull Result<Void> validateRequest(DataFlowRequest request) {
        return DataSourceFactory.super.validateRequest(request);
    }
}
