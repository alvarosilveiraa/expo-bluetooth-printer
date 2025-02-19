import { useEvent } from "expo";
import { useCallback, useEffect, useMemo } from "react";
import { ExpoBluetoothPrinter } from "./ExpoBluetoothPrinter";

export const useBluetoothPrinter = (deviceName?: string) => {
  const event = useEvent(ExpoBluetoothPrinter, "onDevices");
  const devices = useMemo(() => event?.devices || [], [event]);

  useEffect(() => {
    ExpoBluetoothPrinter.listenDevices();
    return () => {
      ExpoBluetoothPrinter.unlistenDevices();
    };
  }, []);

  const printText = useCallback(
    async (text: string) => {
      const device = devices.find(
        ({ name }) => !deviceName || deviceName === name
      );
      if (!device) return;
      await ExpoBluetoothPrinter.printText(device.id, text);
    },
    [devices]
  );

  return {
    devices,
    printText,
  };
};
