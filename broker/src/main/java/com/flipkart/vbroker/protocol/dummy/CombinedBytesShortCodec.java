package com.flipkart.vbroker.protocol.dummy;

import io.netty.channel.CombinedChannelDuplexHandler;

public class CombinedBytesShortCodec extends
        CombinedChannelDuplexHandler<BytesToShortDecoder, ShortToBytesEncoder> {

    public CombinedBytesShortCodec() {
        super(new BytesToShortDecoder(), new ShortToBytesEncoder());
    }
}
