export type BluetoothPrinterContextValue = {
  devices: {
    id: string;
    name: string;
  }[];
  isLoading: boolean;
  isEnabled: boolean;
  print: (byteArrayList: Uint8Array[][]) => Promise<void>;
  printPdf: (fileUri: string) => Promise<void>;
};
