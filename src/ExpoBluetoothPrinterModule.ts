import { NativeModule, requireNativeModule } from 'expo';

import { ExpoBluetoothPrinterModuleEvents } from './ExpoBluetoothPrinter.types';

declare class ExpoBluetoothPrinterModule extends NativeModule<ExpoBluetoothPrinterModuleEvents> {
  PI: number;
  hello(): string;
  setValueAsync(value: string): Promise<void>;
}

// This call loads the native module object from the JSI.
export default requireNativeModule<ExpoBluetoothPrinterModule>('ExpoBluetoothPrinter');
