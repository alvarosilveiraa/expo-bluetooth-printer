import { useBluetoothPrinter } from "../useBluetoothPrinter";
import { BluetoothPrinterContext } from "./Context";
import { BluetoothPrinterProviderProps } from "./Props";

export const BluetoothPrinterProvider = ({
  deviceName,
  children,
}: BluetoothPrinterProviderProps) => {
  const value = useBluetoothPrinter(deviceName);
  return (
    <BluetoothPrinterContext.Provider value={value}>
      {children}
    </BluetoothPrinterContext.Provider>
  );
};
