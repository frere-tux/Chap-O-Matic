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

    static
    {
        // Sample Services.
        attributes.put(GENERIC_ACCESS, "Generic Access Service");
        attributes.put(GENERIC_ATTRIBUTE, "Generic Attribute Service");
        attributes.put(DEVICE_INFORMATION, "Device Information Service");
        attributes.put(CHAP, "Chap Service");
        // Sample Characteristics.
        attributes.put(CHAP_CHAR_1, "Characteristic 1");
        attributes.put(CHAP_CHAR_2, "Characteristic 2");
    }

    public static String lookup(String uuid, String defaultName)
    {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
