package org.iii.esd.api.request;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.iii.esd.api.request.trial.DnpAiRequest;
import org.iii.esd.api.request.trial.DnpSrRequest;

public class RequestTest {

    @Test
    public void testBuildRequest() {
        DnpAiRequest<DnpSrRequest.AiRequestValue> request = buildAiSrRequest();
        System.out.println(request.toString());
    }

    public static DnpAiRequest<DnpSrRequest.AiRequestValue> buildAiSrRequest() {
        List<DnpSrRequest.AiRequestValue> values = new ArrayList<>();
        values.add(DnpSrRequest.AiRequestValue
                .builder()
                .recordOrder(1)
                .recordTime(Instant.now().getEpochSecond())
                .power(1.0)
                .genEnergy(2.0)
                .drEnergy(3.0)
                .soc(50D)
                .status(0D)
                .build());

        return DnpAiRequest.<DnpSrRequest.AiRequestValue>builder()
                           .list(Collections
                                   .singletonList(DnpSrRequest.buildDnpSrRequest(311, values)))
                           .build();
    }
}
