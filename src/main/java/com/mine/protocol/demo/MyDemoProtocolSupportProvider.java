package com.mine.protocol.demo;

import com.x.iot.protocol.support.CompositeProtocolSupport;
import com.x.iot.protocol.support.DefaultTransport;
import com.x.iot.protocol.support.ProtocolSupport;
import com.x.iot.protocol.support.context.ServiceContext;
import com.x.iot.protocol.support.spi.ProtocolSupportProvider;

/**
 * @author zoro.kong
 * &#064;className  MyDemoProtocolSupportProvider
 * &#064;date  2025/6/14
 */
public class MyDemoProtocolSupportProvider implements ProtocolSupportProvider {
    @Override
    public ProtocolSupport create(ServiceContext context) {
        System.out.println("Create demo protocol support...");
        //使用SPI包提供的CompositeProtocolSupport组装自定义协议组件
        CompositeProtocolSupport support = new CompositeProtocolSupport();
        support.setId("my protocol example");
        support.setName("示例自定义协议");
        support.addAuthenticator(DefaultTransport.MQTT, new MyAuthenticator());
        support.addMessageCodecSupport(DefaultTransport.MQTT, new MyMqttMessageCodec());
        support.addMessageCodecSupport(DefaultTransport.LWM2M, new MyLwM2MMessageCodec());
        return support;
    }
    @Override
    public void close() {
        ProtocolSupportProvider.super.close();
    }
}
