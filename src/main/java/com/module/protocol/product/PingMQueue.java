package com.module.protocol.product;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Getter
@Component
public class PingMQueue {

    private BlockingQueue<PingResponse> queue = new LinkedBlockingQueue(3000);


}
