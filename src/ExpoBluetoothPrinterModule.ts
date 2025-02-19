import { NativeModule } from "expo";

export declare class ExpoBluetoothPrinterModule extends NativeModule {
  PI: number;
  hello(): string;
  setValueAsync(value: string): Promise<void>;
}
