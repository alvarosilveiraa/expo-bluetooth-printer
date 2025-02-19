import { requireNativeModule, useEvent } from "expo";
import { ExpoBluetoothPrinterModule } from "./ExpoBluetoothPrinterModule";

const ExpoBluetoothPrinter = requireNativeModule<ExpoBluetoothPrinterModule>(
  "ExpoBluetoothPrinter"
);

export const { loadDevices, printText } = ExpoBluetoothPrinter;

export const useDevices = () => {
  const payload = useEvent(ExpoBluetoothPrinter, "onDevices");
  return payload?.devices || [];
};
