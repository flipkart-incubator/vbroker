package com.flipkart.vbroker.services;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CuratorCreateOp {

    private final String path;
    private final byte[] data;
}
