import { requireNativeView } from 'expo';
import * as React from 'react';
const NativeView = requireNativeView('ExpoBluetoothPrinter');
export default function ExpoBluetoothPrinterView(props) {
    return <NativeView {...props}/>;
}
//# sourceMappingURL=ExpoBluetoothPrinterView.js.map