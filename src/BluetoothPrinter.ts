import { requireNativeModule, NativeModule } from "expo";

declare class BluetoothPrinterModule extends NativeModule<{
  onDevices: (params: { devices: { id: string; name: string }[] }) => void;
}> {
  checkPermissions(): Promise<void>;
  listenDevices(): Promise<void>;
  unlistenDevices(): Promise<void>;
  printText(deviceID: string, text: string): Promise<void>;
  isEnabled(): boolean;
}

export const BluetoothPrinter = requireNativeModule<BluetoothPrinterModule>(
  "ExpoBluetoothPrinter"
);
