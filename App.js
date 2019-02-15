/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 * @flow
 * @lint-ignore-every XPLATJSCOPYRIGHT1
 */

import React, {Component} from 'react';
import {Platform, StyleSheet, Text, View, Button, NativeModules} from 'react-native';


import ToastExample from './ToastExample';
import PlayerExample from './PlayerExample';

ToastExample.show('Awesome', ToastExample.LONG)

// PlayerExample.startPlayer()

export default class App extends Component {
  
  render() {
    return (
      <View style={styles.container}>
        <Button
          onPress={() => NativeModules.PlayerExample.startPlayer()}
          title='Start example activity'
        />
      </View>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#F5FCFF',
  },
  welcome: {
    fontSize: 20,
    textAlign: 'center',
    margin: 10,
  },
  instructions: {
    textAlign: 'center',
    color: '#333333',
    marginBottom: 5,
  },
});
