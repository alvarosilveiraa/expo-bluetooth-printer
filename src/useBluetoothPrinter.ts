import { useEvent } from "expo";
import { useCallback, useEffect, useMemo } from "react";
import { BluetoothPrinter } from "./BluetoothPrinter";

export const useBluetoothPrinter = (deviceName?: string) => {
  const event = useEvent(BluetoothPrinter, "onDevices");
  const devices = useMemo(() => event?.devices || [], [event]);
  const isLoading = useMemo(() => !event, [event]);
  const isEnabled = useMemo(() => BluetoothPrinter.isEnabled(), []);

  const listenDevices = useCallback(async () => {
    await BluetoothPrinter.checkPermissions();
    BluetoothPrinter.listenDevices();
  }, []);

  useEffect(() => {
    if (!isEnabled) return;
    listenDevices();
    return () => {
      BluetoothPrinter.unlistenDevices();
    };
  }, []);

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
