package com.flipkart.vbroker.client;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Node {
    private int brokerId;
    private String hostIp;
    private int hostPort;
}
