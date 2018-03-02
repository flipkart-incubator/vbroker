package com.flipkart.vbroker.handlers;

import com.flipkart.vbroker.flatbuf.ControlResponse;
import com.flipkart.vbroker.flatbuf.VResponse;
import com.flipkart.vbroker.proto.CreateTopicsResponse;
import com.flipkart.vbroker.proto.ProtoRequest;
import com.flipkart.vbroker.proto.ProtoResponse;
import com.flipkart.vbroker.utils.ByteBufUtils;
import com.flipkart.vbroker.utils.FlatbufUtils;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CreateTopicsResponseHandler implements ResponseHandler {

    @Override
    public void handle(VResponse vResponse) {
        CreateTopicsResponse createTopicsResponse = FlatbufUtils.getProtoResponse(vResponse).getCreateTopicsResponse();
        log.info("Response for handling topic create is {}", createTopicsResponse.toString());
    }
}