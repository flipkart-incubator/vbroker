package com.flipkart.vbroker.handlers;

import com.flipkart.vbroker.flatbuf.VRequest;
import com.flipkart.vbroker.flatbuf.VResponse;
import com.flipkart.vbroker.proto.CreateQueuesRequest;
import com.flipkart.vbroker.proto.ProtoQueue;
import com.flipkart.vbroker.utils.FlatbufUtils;
import com.flipkart.vbroker.wrappers.Queue;

import java.util.concurrent.CompletionStage;
import java.util.function.Function;

/**
 * Created by kaushal.hooda on 06/03/18.
 */
public class CreateQueuesRequestHandler implements RequestHandler {
    @Override
    public CompletionStage<VResponse> handle(VRequest vRequest) {
        CreateQueuesRequest createQueuesRequest = FlatbufUtils.getProtoRequest(vRequest).getCreateQueuesRequest();
        createQueuesRequest.getQueuesList().stream().map(new Function<ProtoQueue, Object>() {
            @Override
            public Object apply(ProtoQueue protoQueue) {
                Queue queue = Queue.fromBytes(protoQueue.toByteArray());
                //create the queue and create repsonse...
                return null;
            }
        });
        return null;
    }
}
