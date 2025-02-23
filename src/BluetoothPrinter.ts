import { requireNativeModule, NativeModule } from "expo";
import { BluetoothPrinterValue } from "./data/BluetoothPrinterValue";

declare class BluetoothPrinterModule extends NativeModule<{
  onDevices: (params: { devices: { id: string; name: string }[] }) => void;
}> {
  checkPermissions(): Promise<void>;
  listenDevices(): Promise<void>;
  unlistenDevices(): Promise<void>;
  connectDevice(id: string): Promise<void>;
  closeDevice(): Promise<void>;
  print(values: BluetoothPrinterValue[], count?: number): Promise<void>;
  isEnabled(): boolean;
}

export const BluetoothPrinter = requireNativeModule<BluetoothPrinterModule>(
  "ExpoBluetoothPrinter"
);
