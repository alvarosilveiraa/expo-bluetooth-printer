import { requireNativeModule, NativeModule } from "expo";

declare class BluetoothPrinterModule extends NativeModule<{
  onDevices: (params: { devices: { id: string; name: string }[] }) => void;
}> {
  checkPermissions(): Promise<void>;
  listenDevices(): Promise<void>;
  unlistenDevices(): Promise<void>;
  connectDevice(id: string): Promise<void>;
  closeDevice(): Promise<void>;
  print(byteArrayList: Uint8Array[][], count?: number): Promise<void>;
  printPdf(fileUri: string, count?: number): Promise<void>;
  isEnabled(): boolean;
}

export const BluetoothPrinter = requireNativeModule<BluetoothPrinterModule>(
  "ExpoBluetoothPrinter"
);
