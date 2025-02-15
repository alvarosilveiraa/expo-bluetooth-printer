import * as React from 'react';
export default function ExpoBluetoothPrinterView(props) {
    return (<div>
      <iframe style={{ flex: 1 }} src={props.url} onLoad={() => props.onLoad({ nativeEvent: { url: props.url } })}/>
    </div>);
}
//# sourceMappingURL=ExpoBluetoothPrinterView.web.js.map