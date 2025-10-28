package com.mine.protocol.demo.ota;

import com.x.iot.protocol.support.exception.MessageDecodeException;
import lombok.extern.slf4j.Slf4j;


/**
 * 字节流协议包解析
 * 1 字节流解析总入口
 * 2 对协议包进行规则校验，有问题返回错误通知命令
 * 3 根据不同的命令进行分发
 * 4 返回结果 code&data
 *
 * @author zoro.kong
 */
@Slf4j
public final class ProtocolTransformer {

    private static final String[] HEX_ARRAY = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};
    private static final String HEADER_VERSION = "AAAA";

    public static UndefinedPayloadMessage decodeHexPayload(String hexPayload) throws MessageDecodeException {
        UndefinedPayloadMessage undefinedMessage = new UndefinedPayloadMessage(hexPayload, "Hex");
        // 1 删除AA后的55
        String content = deFormatTtlv(hexPayload.toUpperCase());
        // 5 包id
        int packetId = Integer.parseInt(content.substring(10, 14), 16);
        undefinedMessage.setPacketId(packetId);
        // 2 协议头和版本号 校验头部
        String headVer = content.substring(0, 4);
        if (!HEADER_VERSION.equals(headVer)) {
            // 协议头校验失败
            throw new MessageDecodeException(61009,"decode message error.");
        }
        undefinedMessage.setHeadVer(headVer);
        // 3 数据域长度
        String len = content.substring(4, 8);
        undefinedMessage.setLen(Integer.parseInt(len, 16));
        // 4 校验和
        String sum = content.substring(8, 10);
        undefinedMessage.setSum(sum);
        // 6 命令字
        String cmd = content.substring(14, 18);
        undefinedMessage.setCmd(cmd);
        // 7 数据域
        String data = content.substring(18);
        undefinedMessage.setMessage(data);
        // 校验校验和
        String checkSum = computeSum(content.substring(10, 14) + cmd + data);
        if (!checkSum.equals(sum)) {
            throw new MessageDecodeException(61009, "decode message error.");
        }
        // 校验数据域长度
        int lenInt = Integer.parseInt(len, 16);
        int dataLen = 5 + data.length() / 2;
        if (lenInt != dataLen) {
            throw new MessageDecodeException(61009, "decode message error.");
        }
        return undefinedMessage;
    }

    /**
     * 十六进制的累加
     */
    private static String computeSum(String hex) {
        if (hex == null || hex.isEmpty()) {
            return "00";
        }
        hex = hex.replaceAll(" ", "");
        int total = 0;
        int len = hex.length();
        if (len % 2 != 0) {
            return "00";
        }
        int num = 0;
        while (num < len) {
            String s = hex.substring(num, num + 2);
            total += Integer.parseInt(s, 16);
            num = num + 2;
        }
        String sum = Integer.toHexString(total);
        while (sum.length() < 2) {
            sum = "0" + sum;
        }
        return sum.substring(sum.length() - 2).toUpperCase();
    }

    private static String deFormatTtlv(String data) {
        String res = "";
        for (int i = 0; i < data.length(); i += 2) {
            String str = data.substring(i, i + 2);
            res = res + str;
            if (res.endsWith("AA55")) {
                res = res.substring(0, res.length() - 4) + "AA";
                if (i + 2 < data.length()) {
                    i = i + 2;
                    res = res + data.substring(i, i + 2);
                }
            }
        }
        return res;
    }



}
