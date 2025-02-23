import { BluetoothPrinterValue } from "../data/BluetoothPrinterValue";

export type BluetoothPrinterContextValue = {
  devices: {
    id: string;
    name: string;
  }[];
  isLoading: boolean;
  isEnabled: boolean;
  print: (values: BluetoothPrinterValue[], count?: number) => Promise<void>;
};
