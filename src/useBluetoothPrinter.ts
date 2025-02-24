import { useEvent } from "expo";
import { useCallback, useEffect, useMemo, useState } from "react";
import { BluetoothPrinter } from "./BluetoothPrinter";
import { BluetoothPrinterValue } from "./data/BluetoothPrinterValue";

export const useBluetoothPrinter = (deviceName?: string) => {
  const [isMounted, setIsMounted] = useState(false);
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

  useEffect(() => {
    if (!isMounted || !device || !isEnabled || BluetoothPrinter.isConnected())
      return;
    BluetoothPrinter.connect(device.id);
  }, [deviceName, isMounted, device, isEnabled]);

  const print = useCallback(
    (values: BluetoothPrinterValue[], count?: number) =>
      BluetoothPrinter.print(JSON.stringify(values), count),
    []
  );

  return {
    devices,
    isLoading,
    isEnabled,
    print,
  };
};
