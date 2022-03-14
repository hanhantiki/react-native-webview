import React, {Component} from 'react';
import {
  View,
  Text,
  Alert,
  TextInput,
  Button,
  NativeModules,
} from 'react-native';
import WebView from 'react-native-webview';

export default class TiniEngineWebView extends Component<Props, State> {
  render() {
    NativeModules.RNCWebView.preloadResources(['https://tiki.vn']);

    return (
      <View>
        <View style={{width: '100%', height: '100%'}}>
          <WebView
            appMeta={{
              runtimeVariables: {
                startTime: Date.now(),
              },
            }}
            source={{
              uri: 'https://tiki.vn',
            }}
          />
        </View>
      </View>
    );
  }
}
