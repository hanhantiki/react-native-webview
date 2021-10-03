import React, {Component} from 'react';
import {View, findNodeHandle} from 'react-native';

import WebView, { CompositeComponent } from 'react-native-webview';

interface Props {}

interface State {
  webViewTag?: number | null;
}

export default class NativeComponent extends Component<Props, State> {
  webViewRef = React.createRef<WebView>();

  state: State = {};

  get webviewTag(): number | null {
    const { current: webview } = this.webViewRef;
    if (webview) {
      return findNodeHandle((webview as any).webViewRef.current);
    }
    return 0;
  }
  

  render() {
    return (
      <View style={{height: 900}}>
        <WebView
          ref={this.webViewRef}
          source={{uri: 'https://tiki.vn'}}
          style={{width: '100%', height: '100%'}}
          onLoad={() => {
            this.setState({ webViewTag: this.webviewTag });
          }}
          // setSupportMultipleWindows={false}
        />
        {this.state.webViewTag && (
          <CompositeComponent webviewTag={this.state.webViewTag}>
            <View style={{ backgroundColor: 'red', top: 0, left: 0, height: 100, width: '100%' }}/>
          </CompositeComponent>
        )}
      </View>
    );
  }
}
