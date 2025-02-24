import { BluetoothPrinterText } from "./BluetoothPrinterText";
import { BluetoothPrinterImage } from "./BluetoothPrinterImage";

export type BluetoothPrinterValue = {
  image?: BluetoothPrinterImage;
  text?: BluetoothPrinterText;
  newLines?: number;
};
