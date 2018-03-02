package com.flipkart.vbroker.protocol.codecs;

import com.flipkart.vbroker.protocol.VRequestEncoder;
import com.flipkart.vbroker.protocol.VResponseDecoder;
import io.netty.channel.CombinedChannelDuplexHandler;

public class VBrokerClientCodec extends CombinedChannelDuplexHandler<VResponseDecoder, VRequestEncoder> {

    public VBrokerClientCodec() {
        super(new VResponseDecoder(), new VRequestEncoder());
    }
}
