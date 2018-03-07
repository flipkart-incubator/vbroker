package com.flipkart.vbroker.utils;

/**
 * Created by kaushal.hooda on 23/02/18.
 */

import com.flipkart.vbroker.flatbuf.*;
import com.flipkart.vbroker.proto.ProtoRequest;
import com.flipkart.vbroker.proto.ProtoResponse;
import com.google.flatbuffers.FlatBufferBuilder;
import com.google.protobuf.InvalidProtocolBufferException;

import static java.util.Objects.nonNull;

public class FlatbufUtils {

    public static int buildVStatus(FlatBufferBuilder builder, short statusCode, String message) {
        return VStatus.createVStatus(builder, statusCode, builder.createString(message));
    }

    public static VStatus createVStatus(short statusCode, String message) {
        FlatBufferBuilder builder = new FlatBufferBuilder();
        int vStatusOffset = buildVStatus(builder, statusCode, message);
        builder.finish(vStatusOffset);
        return VStatus.getRootAsVStatus(builder.dataBuffer());
    }

    public static ProtoResponse getProtoResponse(VResponse response) {
        ControlResponse controlResponse = (ControlResponse) response.responseMessage(new ControlResponse());
        byte[] protoResponseBytes = ByteBufUtils.getBytes(controlResponse.payloadAsByteBuffer());
        try {
            return ProtoResponse.parseFrom(protoResponseBytes);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }

    public static ProtoRequest getProtoRequest(VRequest vRequest) {
        try {
            ControlRequest controlRequest = (ControlRequest) vRequest.requestMessage(new ControlRequest());
            assert nonNull(controlRequest);

            byte[] protoRequestBytes = ByteBufUtils.getBytes(controlRequest.payloadAsByteBuffer());
            return ProtoRequest.parseFrom(protoRequestBytes);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }

    public static VRequest createVRequest(byte apiVersion, int correlationId, ProtoRequest protoRequest) {
        FlatBufferBuilder builder = new FlatBufferBuilder();
        int payloadOffset = builder.createByteVector(protoRequest.toByteArray());
        int controlRequestOffset = ControlRequest.createControlRequest(builder, payloadOffset);
        int vRequestOffset = VRequest.createVRequest(builder, apiVersion, correlationId, RequestMessage.ControlRequest, controlRequestOffset);
        builder.finish(vRequestOffset);
        return VRequest.getRootAsVRequest(builder.dataBuffer());
    }

    public static VResponse createVResponse(int correlationId, ProtoResponse protoResponse) {
        FlatBufferBuilder builder = new FlatBufferBuilder();
        int protoResponseOffset = builder.createByteVector(protoResponse.toByteArray());
        int controlResponseOffset = ControlResponse.createControlResponse(builder, protoResponseOffset);
        int vResponse = VResponse.createVResponse(builder, correlationId, ResponseMessage.ControlResponse, controlResponseOffset);
        builder.finish(vResponse);
        return VResponse.getRootAsVResponse(builder.dataBuffer());
    }
}
