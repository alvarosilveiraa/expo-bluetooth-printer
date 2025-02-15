// Reexport the native module. On web, it will be resolved to ExpoBluetoothPrinterModule.web.ts
// and on native platforms to ExpoBluetoothPrinterModule.ts
export { default } from './ExpoBluetoothPrinterModule';
export { default as ExpoBluetoothPrinterView } from './ExpoBluetoothPrinterView';
export * from  './ExpoBluetoothPrinter.types';
