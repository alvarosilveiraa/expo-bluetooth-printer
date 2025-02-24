import { requireNativeModule, NativeModule } from "expo";

declare class BluetoothPrinterModule extends NativeModule<{
  onDevices: (params: { devices: { id: string; name: string }[] }) => void;
}> {
  checkPermissions(): Promise<void>;
  listenDevices(): Promise<void>;
  unlistenDevices(): Promise<void>;
  connect(id: string): Promise<void>;
  print(valuesString: string, count?: number): Promise<void>;
  isEnabled(): boolean;
  isConnected(): boolean;
}

export const BluetoothPrinter = requireNativeModule<BluetoothPrinterModule>(
  "ExpoBluetoothPrinter"
);
