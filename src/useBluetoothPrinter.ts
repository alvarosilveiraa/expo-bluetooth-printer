import { useEvent } from "expo";
import { useCallback, useEffect, useMemo, useState } from "react";
import { BluetoothPrinter } from "./BluetoothPrinter";
import { BluetoothPrinterValue } from "./data/BluetoothPrinterValue";

export const useBluetoothPrinter = (deviceName?: string) => {
  const [isMounted, setIsMounted] = useState(false);
  const [isConnected, setIsConnected] = useState(false);
  const event = useEvent(BluetoothPrinter, "onDevices");
  const devices = useMemo(() => event?.devices || [], [event]);
  const device = useMemo(
    () => devices.find(({ name }) => !deviceName || deviceName === name),
    [devices]
  );
  const isLoading = useMemo(() => !event, [event]);
  const isEnabled = useMemo(() => BluetoothPrinter.isEnabled(), []);

  const listenDevices = useCallback(async () => {
    await BluetoothPrinter.checkPermissions();
    BluetoothPrinter.listenDevices();
  }, []);

  useEffect(() => {
    setIsMounted(true);
  }, []);

  useEffect(() => {
    if (!isMounted || !isEnabled) return;
    listenDevices();
    return () => {
      BluetoothPrinter.unlistenDevices();
    };
  }, [isMounted, isEnabled]);

  const connectDevice = useCallback(
    async (id: string) => {
      try {
        await BluetoothPrinter.connectDevice(id);
      } finally {
        setIsConnected(BluetoothPrinter.isConnected());
      }
    },
    [device]
  );

  useEffect(() => {
    if (!isMounted || !device || !isEnabled || isConnected) return;
    connectDevice(device.id);
    return () => {
      BluetoothPrinter.closeDevice();
    };
  }, [deviceName, isMounted, device, isEnabled, isConnected]);

  const print = useCallback(
    (values: BluetoothPrinterValue[]) =>
      BluetoothPrinter.print(JSON.stringify(values)),
    []
  );

  return {
    devices,
    isLoading,
    isEnabled,
    print,
  };
};
