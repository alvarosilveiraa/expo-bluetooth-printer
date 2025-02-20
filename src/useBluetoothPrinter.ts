import { useEvent } from "expo";
import { useCallback, useEffect, useMemo } from "react";
import { BluetoothPrinter } from "./BluetoothPrinter";

export const useBluetoothPrinter = (deviceName?: string) => {
  const event = useEvent(BluetoothPrinter, "onDevices");
  const devices = useMemo(() => event?.devices || [], [event]);
  const isLoading = useMemo(() => !event, [event]);
  const isEnabled = useMemo(() => BluetoothPrinter.isEnabled(), []);

  useEffect(() => {
    if (!isEnabled) return;
    BluetoothPrinter.listenDevices();
    return () => {
      BluetoothPrinter.unlistenDevices();
    };
  }, [isEnabled]);

  const printText = useCallback(
    async (text: string) => {
      const device = devices.find(
        ({ name }) => !deviceName || deviceName === name
      );
      if (!isEnabled || !device) return;
      await BluetoothPrinter.printText(device.id, text);
    },
    [deviceName, devices, isEnabled]
  );

  return {
    devices,
    isLoading,
    isEnabled,
    printText,
  };
};
