import { useEvent } from "expo";
import { useEffect } from "react";
import { ExpoBluetoothPrinter } from "./ExpoBluetoothPrinter";

export const useBluetoothPrinter = () => {
  const payload = useEvent(ExpoBluetoothPrinter, "onDevices");

  useEffect(() => {
    ExpoBluetoothPrinter.listenDevices();

    return () => {
      ExpoBluetoothPrinter.unlistenDevices();
    };
  }, []);

  return {
    devices: payload?.devices || [],
    printText: ExpoBluetoothPrinter.printText,
  };
};
