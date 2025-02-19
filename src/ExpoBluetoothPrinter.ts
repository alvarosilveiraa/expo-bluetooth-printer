import { requireNativeModule } from "expo";
import { ExpoBluetoothPrinterModule } from "./ExpoBluetoothPrinterModule";

export const ExpoBluetoothPrinter =
  requireNativeModule<ExpoBluetoothPrinterModule>("ExpoBluetoothPrinter");
