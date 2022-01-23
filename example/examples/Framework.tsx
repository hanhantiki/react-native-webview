import React, {Component} from 'react';
import {View, Text, Alert, TextInput, Button} from 'react-native';
import WebView from 'react-native-webview';

const renderFrameWorkPath = 'http://localhost:8082/tf-tiniapp.render.js';
const workerFrameWorkPath = 'http://localhost:8082/tf-tiniapp.worker.js';

export default class Framework extends Component<Props, State> {
  render() {
    return (
      <View>
        <View style={{width: '100%', height: '100%'}}>
          <WebView
            appMeta={{
              renderFrameWorkPath,
              workerFrameWorkPath,
            }}
            source={{
              uri: 'http://localhost:8080/index.prod.html?__enableCache=YES',
            }}
          />
        </View>
      </View>
    );
  }
}
