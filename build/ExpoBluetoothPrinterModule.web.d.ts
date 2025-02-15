import { NativeModule } from 'expo';
import { ExpoBluetoothPrinterModuleEvents } from './ExpoBluetoothPrinter.types';
declare class ExpoBluetoothPrinterModule extends NativeModule<ExpoBluetoothPrinterModuleEvents> {
    PI: number;
    setValueAsync(value: string): Promise<void>;
    hello(): string;
}
declare const _default: typeof ExpoBluetoothPrinterModule;
export default _default;
//# sourceMappingURL=ExpoBluetoothPrinterModule.web.d.ts.map