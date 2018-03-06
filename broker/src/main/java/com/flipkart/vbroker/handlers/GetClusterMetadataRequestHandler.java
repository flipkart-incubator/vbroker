package com.flipkart.vbroker.handlers;

import com.flipkart.vbroker.flatbuf.StatusCode;
import com.flipkart.vbroker.flatbuf.VRequest;
import com.flipkart.vbroker.flatbuf.VResponse;
import com.flipkart.vbroker.proto.GetClusterMetadataResponse;
import com.flipkart.vbroker.proto.ProtoRequest;
import com.flipkart.vbroker.proto.ProtoResponse;
import com.flipkart.vbroker.proto.VStatus;
import com.flipkart.vbroker.services.ClusterMetadataService;
import com.flipkart.vbroker.utils.FlatbufUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletionStage;

/**
 * Created by kaushal.hooda on 06/03/18.
 */
@AllArgsConstructor
@Slf4j
public class GetClusterMetadataRequestHandler implements RequestHandler {
    private final ClusterMetadataService clusterMetadataService;

    @Override
    public CompletionStage<VResponse> handle(VRequest vRequest) {
        ProtoRequest protoRequest = FlatbufUtils.getProtoRequest(vRequest);
        assert protoRequest.hasGetClusterMetadataRequest();

        return clusterMetadataService.getClusterMetadata().handle((metadata, throwable) -> {
            VStatus.Builder vBuilder = VStatus.newBuilder().setStatusCode(StatusCode.Success);
            if (throwable != null) {
                vBuilder.setMessage(throwable.getMessage()).setStatusCode(StatusCode.Failure);
            }
            GetClusterMetadataResponse metadataResponse = GetClusterMetadataResponse
                .newBuilder()
                .setStatus(vBuilder.build())
                .setClusterMetadata(metadata)
                .build();
            ProtoResponse protoResponse = ProtoResponse.newBuilder().setGetClusterMetadataResponse(metadataResponse).build();
            return FlatbufUtils.createVResponse(vRequest.correlationId(), protoResponse);
        });
    }
}
