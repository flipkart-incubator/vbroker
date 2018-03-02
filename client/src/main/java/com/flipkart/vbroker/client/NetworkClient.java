package com.flipkart.vbroker.client;

import com.flipkart.vbroker.flatbuf.VRequest;
import com.flipkart.vbroker.flatbuf.VResponse;

import java.io.Closeable;
import java.util.concurrent.CompletionStage;

public interface NetworkClient extends Closeable {

    public CompletionStage<VResponse> send(Node node, VRequest vRequest);

}
