package tools.ofirbar.bluetoothscan;

public class Constants {

    public static final String FITTO_MAC_ADDRESS = "EE:F0:EA:17:69:B4";
    public static final String Nordic_UART_Service = "6e400001-b5a3-f393-e0a9-e50e24dcca9e";
    public static final String RX_Characteristic = "6e400002-b5a3-f393-e0a9-e50e24dcca9e";
    public static final String TX_Characteristic = "6e400003-b5a3-f393-e0a9-e50e24dcca9e";
    public static final String TX_Descriptor = "00002902-0000-1000-8000-00805F9B34FB";


    // Parser Layout used to detect BLE devices conforming to iBeacon format
    // Note: This will find any device that conforms to iBeacon format, even if it is not developed by Apple
    // Add 4c00 to detect apple-only devices
    // public static final String IBEACON_PARSER_LAYOUT_APPLE_ONLY = "m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24";
    public static final String IBEACON_PARSER_LAYOUT = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24";


}
