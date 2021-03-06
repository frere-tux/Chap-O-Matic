package com.ton_in.chapomatic;

import java.util.HashMap;

public class SampleGattAttributes
{
    private static HashMap<String, String> attributes = new HashMap();
    public static String GENERIC_ACCESS = "00001800-0000-1000-8000-00805f9b34fb";
    public static String GENERIC_ATTRIBUTE = "00001801-0000-1000-8000-00805f9b34fb";
    public static String DEVICE_INFORMATION = "0000180a-0000-1000-8000-00805f9b34fb";
    public static String CHAP = "9bc63380-0000-1000-8000-00805f9b34fb";
    public static String CHAP_CHAR_1 = "00003381-0000-1000-8000-00805f9b34fb";
    public static String CHAP_CHAR_2 = "00003382-0000-1000-8000-00805f9b34fb";
    public static String UART = "6e400001-b5a3-f393-e0a9-e50e24dcca9e";
    public static String UART_TXD = "6e400002-b5a3-f393-e0a9-e50e24dcca9e";
    public static String UART_RXD = "6e400003-b5a3-f393-e0a9-e50e24dcca9e";

    static
    {
        // Sample Services.
        attributes.put(GENERIC_ACCESS, "Generic Access Service");
        attributes.put(GENERIC_ATTRIBUTE, "Generic Attribute Service");
        attributes.put(DEVICE_INFORMATION, "Device Information Service");
        attributes.put(UART, "UART Service");
        attributes.put(CHAP, "Chap Service");
        // Sample Characteristics.
        attributes.put(UART_TXD, "TDX Characteristic");
        attributes.put(UART_RXD, "RDX Characteristic");
        attributes.put(CHAP_CHAR_1, "Characteristic 1");
        attributes.put(CHAP_CHAR_2, "Characteristic 2");
    }

    public static String lookup(String uuid, String defaultName)
    {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
