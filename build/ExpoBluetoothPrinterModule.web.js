import { registerWebModule, NativeModule } from 'expo';
class ExpoBluetoothPrinterModule extends NativeModule {
    PI = Math.PI;
    async setValueAsync(value) {
        this.emit('onChange', { value });
    }
    hello() {
        return 'Hello world! 👋';
    }
}
export default registerWebModule(ExpoBluetoothPrinterModule);
//# sourceMappingURL=ExpoBluetoothPrinterModule.web.js.map