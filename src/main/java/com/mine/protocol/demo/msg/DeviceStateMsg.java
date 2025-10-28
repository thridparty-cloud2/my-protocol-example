package com.mine.protocol.demo.msg;

import lombok.Getter;
import lombok.Setter;

/**
 * @author zoro.kong
 * &#064;className  DeviceStateMsg
 * &#064;date  2025/6/17
 */
@Getter
@Setter
public class DeviceStateMsg {
    private String timestamp;
    private String state ;
    private Integer brightness;
}
