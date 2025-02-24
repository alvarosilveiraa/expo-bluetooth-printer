import { BluetoothPrinterColumns } from "./BluetoothPrinterColumns";
import { BluetoothPrinterText } from "./BluetoothPrinterText";

export type BluetoothPrinterValue = {
  image?: string;
  text?: BluetoothPrinterText;
  columns?: BluetoothPrinterColumns;
};
