import { BluetoothPrinterText } from "./BluetoothPrinterText";
import { BluetoothPrinterImage } from "./BluetoothPrinterImage";
import { BluetoothPrinterQRCode } from "./BluetoothPrinterQRCode";

export type BluetoothPrinterValue = {
  image?: BluetoothPrinterImage;
  text?: BluetoothPrinterText;
  qrCode?: BluetoothPrinterQRCode;
  newLines?: number;
};
