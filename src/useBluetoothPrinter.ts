import { useEvent } from "expo";
import { useCallback, useEffect, useMemo, useState } from "react";
import { BluetoothPrinter } from "./BluetoothPrinter";
import { BluetoothPrinterValue } from "./data/BluetoothPrinterValue";

export const useBluetoothPrinter = (deviceName?: string) => {
  const [isMounted, setIsMounted] = useState(false);
  const [isConnecting, setIsConnecting] = useState(false);
  const [isConnected, setIsConnected] = useState(false);
  const event = useEvent(BluetoothPrinter, "onDevices");
  const device = useMemo(
    () => event?.devices.find(({ name }) => !deviceName || deviceName === name),
    [event]
  );
  const isLoading = useMemo(() => !event, [event]);
  const isEnabled = useMemo(() => BluetoothPrinter.isEnabled(), []);

  useEffect(() => {
    setIsMounted(true);
  }, []);

  const listenDevices = useCallback(async () => {
    await BluetoothPrinter.checkPermissions();
    BluetoothPrinter.listenDevices();
  }, []);

  useEffect(() => {
    if (!isMounted || !isEnabled) return;
    listenDevices();
    return () => {
      BluetoothPrinter.unlistenDevices();
    };
  }, [isMounted, isEnabled]);

  const connect = useCallback(async (id: string) => {
    try {
      setIsConnecting(true);
      await BluetoothPrinter.connect(id);
    } finally {
      setIsConnecting(false);
      setIsConnected(BluetoothPrinter.isConnected());
    }
  }, []);

  useEffect(() => {
    if (!isMounted || !device || !isEnabled || isConnecting || isConnected)
      return;
    connect(device.id);
  }, [deviceName, isMounted, device, isEnabled, isConnecting, isConnected]);

  const print = useCallback(
    (values: BluetoothPrinterValue[], count?: number) =>
      BluetoothPrinter.print(JSON.stringify(values), count),
    []
  );

  return {
    device,
    isLoading,
    isEnabled,
    isConnecting,
    isConnected,
    print,
  };
};
