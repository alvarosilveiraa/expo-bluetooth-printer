import { createContext } from "react";
import { BluetoothPrinterContextValue } from "./Value";

export const BluetoothPrinterContext = createContext(
  {} as BluetoothPrinterContextValue
);
