package com.mine.protocol.demo.ota;


import com.x.iot.protocol.support.message.TransportMessage;
import lombok.Getter;
import lombok.Setter;

/**
 * TTLV消息
 *
 * @author zoro.kong
 * @since 1.0.0
 */
@Getter
@Setter
@SuppressWarnings("SpellCheckingInspection")
public abstract class XTTLVMessage<T> implements TransportMessage<T> {
    private String headVer;
    private int len;
    private String sum;
    private String cmd;
    private int packetId;
}
