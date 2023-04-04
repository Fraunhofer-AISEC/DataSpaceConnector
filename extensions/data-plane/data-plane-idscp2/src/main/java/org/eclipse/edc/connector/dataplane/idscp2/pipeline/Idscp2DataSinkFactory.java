package org.eclipse.edc.connector.dataplane.idscp2.pipeline;

import org.eclipse.edc.connector.dataplane.spi.pipeline.DataSink;
import org.eclipse.edc.connector.dataplane.spi.pipeline.DataSinkFactory;
import org.eclipse.edc.spi.result.Result;
import org.eclipse.edc.spi.types.domain.transfer.DataFlowRequest;
import org.jetbrains.annotations.NotNull;

public class Idscp2DataSinkFactory implements DataSinkFactory {
    @Override
    public boolean canHandle(DataFlowRequest request) {
        return false;
    }

    @Override
    public @NotNull Result<Boolean> validate(DataFlowRequest request) {
        return null;
    }

    @Override
    public DataSink createSink(DataFlowRequest request) {
        return null;
    }

    @Override
    public @NotNull Result<Void> validateRequest(DataFlowRequest request) {
        return DataSinkFactory.super.validateRequest(request);
    }
}
