import { NativeModule } from "expo";

export declare class ExpoBluetoothPrinterModule extends NativeModule {
  getDevices(): Promise<{ id: string; name: string }[]>;
  printText(deviceID: string, text: string): Promise<void>;
}
