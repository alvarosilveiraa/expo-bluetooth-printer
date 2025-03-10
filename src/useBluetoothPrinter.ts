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

  const connect = useCallback(async () => {
    if (!device || isConnecting || isConnected) return;
    try {
      setIsConnecting(true);
      await BluetoothPrinter.connect(device.id);
    } finally {
      setIsConnecting(false);
      setIsConnected(BluetoothPrinter.isConnected());
    }
  }, [device, isConnecting, isConnected]);

  useEffect(() => {
    if (!isMounted || !isEnabled) return;
    connect();
  }, [isMounted, isEnabled, connect]);

  const print = useCallback(
    async (values: BluetoothPrinterValue[], count?: number) => {
      await connect();
      if (!BluetoothPrinter.isConnected())
        throw new Error("The printer is unconnected!");
      return BluetoothPrinter.print(JSON.stringify(values), count);
    },
    [connect]
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
