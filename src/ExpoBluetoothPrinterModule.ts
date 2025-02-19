import { NativeModule } from "expo";

export declare class ExpoBluetoothPrinterModule extends NativeModule<{
  onDevices: (params: { devices: { id: string; name: string }[] }) => void;
}> {
  listenDevices(): Promise<void>;
  unlistenDevices(): Promise<void>;
  printText(deviceID: string, text: string): Promise<void>;
}
