import React, {Component} from 'react';
import {View, Text, Alert, TextInput, Button} from 'react-native';
import WebView from 'react-native-webview';

export default class Framework extends Component<Props, State> {
    render() {
      return (
        <View>
            <View style={{ width: '100%', height: '100%' }}>
                <WebView appMeta={{
                  renderFrameWorkPath: 'http://localhost:8081/tf-tiniapp.render.js',
                  workerFrameWorkPath: 'http://localhost:8081/tf-tiniapp.render.js',
                }} source={{ uri: 'http://localhost:8081/tf-tiniapp.html' }}/>
          </View>
        </View>
      );
    }
  }