import { requireNativeView } from 'expo';
import * as React from 'react';

import { ExpoBluetoothPrinterViewProps } from './ExpoBluetoothPrinter.types';

const NativeView: React.ComponentType<ExpoBluetoothPrinterViewProps> =
  requireNativeView('ExpoBluetoothPrinter');

export default function ExpoBluetoothPrinterView(props: ExpoBluetoothPrinterViewProps) {
  return <NativeView {...props} />;
}
