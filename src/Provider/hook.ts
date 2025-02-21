import { useContext } from "react";
import { BluetoothPrinterContext } from "./Context";

export const useBluetoothPrinterContext = () =>
  useContext(BluetoothPrinterContext);
