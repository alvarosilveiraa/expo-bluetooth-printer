export type BluetoothPrinterContextValue = {
  devices: {
    id: string;
    name: string;
  }[];
  isLoading: boolean;
  isEnabled: boolean;
  print: (byteArrayList: Uint8Array[][], count?: number) => Promise<void>;
  printPdf: (fileUri: string, count?: number) => Promise<void>;
};
