import * as React from 'react';

import { ExpoBluetoothPrinterViewProps } from './ExpoBluetoothPrinter.types';

export default function ExpoBluetoothPrinterView(props: ExpoBluetoothPrinterViewProps) {
  return (
    <div>
      <iframe
        style={{ flex: 1 }}
        src={props.url}
        onLoad={() => props.onLoad({ nativeEvent: { url: props.url } })}
      />
    </div>
  );
}
