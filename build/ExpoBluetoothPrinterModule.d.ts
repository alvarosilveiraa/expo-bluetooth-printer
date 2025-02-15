import { NativeModule } from 'expo';
import { ExpoBluetoothPrinterModuleEvents } from './ExpoBluetoothPrinter.types';
declare class ExpoBluetoothPrinterModule extends NativeModule<ExpoBluetoothPrinterModuleEvents> {
    PI: number;
    hello(): string;
    setValueAsync(value: string): Promise<void>;
}
declare const _default: ExpoBluetoothPrinterModule;
export default _default;
//# sourceMappingURL=ExpoBluetoothPrinterModule.d.ts.map