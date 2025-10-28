package com.mine.protocol.demo.ota;

import com.x.iot.protocol.support.exception.MessageDecodeException;
import com.x.iot.protocol.support.message.TagTypeLenValue;
import lombok.Getter;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class TTLVTransformer {
    public static List<TagTypeLenValue> hexToTTLV(String hex) throws MessageDecodeException {
        List<TagTypeLenValue> entities = new ArrayList<>();
        String idAndType = hexToBinary(hex.substring(0, 4));
        Integer id = Integer.parseInt(idAndType.substring(0, 13), 2);
        Type type = Type.fromValue(idAndType.substring(13));
        if (type == null) {
            throw new MessageDecodeException(61009, "decode message error.");
        }
        TagTypeLenValue entity = new TagTypeLenValue();
        entity.setId(id);
        int nextIndex;
        String length;
        switch (type) {
            case BOOLEAN_FALSE: {
                entity.setValue(false);
                nextIndex = 4;
                entities.add(entity);
                break;
            }
            case BOOLEAN_TRUE: {
                entity.setValue(true);
                nextIndex = 4;
                entities.add(entity);
                break;
            }
            case NUM: {
                length = hexToBinary(hex.substring(4, 6));
                String flag = (length).startsWith("0") ? "" : "-";
                //衰减
                int attenuation = Integer.parseInt(length.substring(1, 5), 2);
                int byteNum = (Integer.parseInt(length.substring(5), 2) + 1) * 2;
                nextIndex = 4 + 2 + byteNum;
                String value = byteNum == 0 ? "0" : hexStringToBigData(hex.substring(6, 6 + byteNum));
                String valueRes;
                if (attenuation == 0) {
                    valueRes = value;
                } else if (value.length() < attenuation) {
                    valueRes = "0.";
                    attenuation = attenuation - value.length() + 1;
                    while (attenuation > 1) {
                        valueRes = valueRes + "0";
                        attenuation--;
                    }
                    valueRes = valueRes + value;
                } else {
                    String per = value.substring(0, value.length() - attenuation);
                    valueRes = !per.isEmpty() ? per : "0";
                    valueRes = valueRes + "." + value.substring(value.length() - attenuation);
                }
                valueRes = flag + valueRes;
                entity.setValue(valueRes);
                entities.add(entity);
                break;
            }
            case BINARY: {
                int lengthNum = Integer.parseInt(hex.substring(4, 8), 16) * 2;
                // 读取value
                String binaryValue = hex.substring(8, 8 + lengthNum);
                entity.setValue(lengthNum == 0 ? "" : hexToUTF8String(binaryValue));
                entity.setType("text");
                nextIndex = 8 + lengthNum;
                entities.add(entity);
                break;
            }
            case RAW: {
                int lengthNumRaw = Integer.parseInt(hex.substring(4, 8), 16) * 2;
                // 读取value
                String binaryValueRaw = hex.substring(8, 8 + lengthNumRaw);
                entity.setValue(binaryValueRaw.toUpperCase());
                entity.setType("raw");
                nextIndex = 8 + lengthNumRaw;
                entities.add(entity);
                break;
            }
            case STRUCT: {
                int num = Integer.parseInt(hex.substring(4, 8), 16);
                if (num == 0) {
                    entity.setValue(null);
                    entity.setValue(new ArrayList<>());
                    entities.add(entity);
                    nextIndex = 8;
                    break;
                }
                List<TagTypeLenValue> moduleTypeEntities = hexToTTLV(hex.substring(8));
                List<TagTypeLenValue> structValue = new ArrayList<>();
                for (int i = 0; i < moduleTypeEntities.size(); i++) {
                    if (i < num) {
                        structValue.add(moduleTypeEntities.get(i));
                    } else if (i == num) {
                        entity.setValue(structValue);
                        entities.add(entity);
                        entities.add(moduleTypeEntities.get(i));
                    } else {
                        entities.add(moduleTypeEntities.get(i));
                    }
                }
                if (num == moduleTypeEntities.size()) {
                    entity.setValue(structValue);
                    entities.add(entity);
                }
                return entities;
            }
            default:
                throw new MessageDecodeException(61009, "decode message error.");
        }
        if (hex.length() > nextIndex) {
            List<TagTypeLenValue> moduleTypeEntities = hexToTTLV(hex.substring(nextIndex));
            entities.addAll(moduleTypeEntities);
        }
        return entities;
    }

    private static String hexToUTF8String(String s) {
        if (s == null || s.isEmpty()) {
            return null;
        }
        s = s.toUpperCase();
        int total = s.length() / 2;
        int pos = 0;
        byte[] buffer = new byte[total];
        for (int i = 0; i < total; i++) {
            int start = i * 2;
            buffer[i] = (byte) Integer.parseInt(s.substring(start, start + 2), 16);
            pos++;
        }
        return new String(buffer, 0, pos, StandardCharsets.UTF_8);
    }

    private static String hexToBinary(String hex) {
        StringBuilder binary = new StringBuilder();
        for (int i = 0; i < hex.length(); i++) {
            String tmp = "0000" + Integer.toBinaryString(Integer.parseInt(hex.substring(i, i + 1), 16));
            binary.append(tmp.substring(tmp.length() - 4));
        }
        return binary.toString();
    }

    private static String hexStringToBigData(String hexString) {
        BigInteger hexBig = new BigInteger("16");
        BigInteger result = new BigInteger("0");
        for (int i = 0; i < hexString.length(); i++) {
            String endNum = hexString.substring(i, i + 1);
            result = result.multiply(hexBig).add(new BigInteger(endNum, 16));
        }
        return result.toString();
    }

    @Getter
    private enum Type {
        BOOLEAN_FALSE("false", "000", 0),
        BOOLEAN_TRUE("true", "001", 0),
        NUM("num", "010", 8),
        BINARY("binary", "011", 16),
        STRUCT("struct", "100", 16),
        RAW("raw", "101", 16);

        private final String type;
        private final String typeBinary;
        private final Integer length;

        Type(String type, String typeBinary, Integer length) {
            this.type = type;
            this.typeBinary = typeBinary;
            this.length = length;
        }

        public static Type fromValue(String binary) {
            for (Type value : Type.values()) {
                if (value.getTypeBinary().equals(binary)) {
                    return value;
                }
            }
            return null;
        }
    }


}



