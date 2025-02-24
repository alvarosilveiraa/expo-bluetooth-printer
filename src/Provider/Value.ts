import { BluetoothPrinterValue } from "../data/BluetoothPrinterValue";

type Device = {
  id: string;
  name: string;
};

export type BluetoothPrinterContextValue = {
  device?: Device;
  isLoading: boolean;
  isEnabled: boolean;
  isConnecting: boolean;
  isConnected: boolean;
  print: (values: BluetoothPrinterValue[], count?: number) => Promise<void>;
};
