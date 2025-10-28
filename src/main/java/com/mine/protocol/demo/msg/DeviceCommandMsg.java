package com.mine.protocol.demo.msg;

import lombok.Getter;
import lombok.Setter;

/**
 * @author zoro.kong
 * &#064;date  2025/6/17
 */
@Setter
@Getter
public class DeviceCommandMsg {
    private String action;
    private Props payload;
}
