import { registerWebModule, NativeModule } from 'expo';

import { ExpoBluetoothPrinterModuleEvents } from './ExpoBluetoothPrinter.types';

class ExpoBluetoothPrinterModule extends NativeModule<ExpoBluetoothPrinterModuleEvents> {
  PI = Math.PI;
  async setValueAsync(value: string): Promise<void> {
    this.emit('onChange', { value });
  }
  hello() {
    return 'Hello world! 👋';
  }
}

export default registerWebModule(ExpoBluetoothPrinterModule);
