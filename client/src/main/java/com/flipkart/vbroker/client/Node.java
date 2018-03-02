package com.flipkart.vbroker.client;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public class Node {
    private int brokerId;
    private String hostIp;
    private int hostPort;
}
