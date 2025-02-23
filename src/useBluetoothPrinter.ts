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

  useEffect(() => {
    if (!isEnabled || !devices.length) return;
    const device = devices.find(
      ({ name }) => !deviceName || deviceName === name
    );
    if (device) {
      BluetoothPrinter.connectDevice(device.id);
      return () => {
        BluetoothPrinter.closeDevice();
      };
    }
  }, [deviceName, devices]);

  return {
    devices,
    isLoading,
    isEnabled,
    print: BluetoothPrinter.print,
  };
};
