package com.mine.protocol.demo.msg;

import lombok.Getter;
import lombok.Setter;

/**
 * @author zoro.kong
 * &#064;className  DeviceErrorMsg
 * &#064;date  2025/6/17
 */
@Setter
@Getter
public class DeviceErrorMsg {
    private String timestamp;
    private String code;
    private String message;
    private String severity;
}
