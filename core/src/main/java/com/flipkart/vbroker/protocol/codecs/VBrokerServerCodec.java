package com.flipkart.vbroker.protocol.codecs;

import com.flipkart.vbroker.protocol.VRequestDecoder;
import com.flipkart.vbroker.protocol.VResponseEncoder;
import io.netty.channel.CombinedChannelDuplexHandler;

public class VBrokerServerCodec extends CombinedChannelDuplexHandler<VRequestDecoder, VResponseEncoder> {

    public VBrokerServerCodec() {
        super(new VRequestDecoder(), new VResponseEncoder());
    }
}
