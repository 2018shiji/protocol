package com.module.protocol.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PingResponse {
    private String remoteIP;
    private Date responseTime;
}
