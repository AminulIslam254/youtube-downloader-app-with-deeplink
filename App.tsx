import * as React from 'react';
import { View, Text } from 'react-native';
import { NavigationContainer } from '@react-navigation/native';
import { createStackNavigator } from '@react-navigation/stack';
import { StatusBar } from 'react-native';

import MainHome from './components/MainHome';



const Stack = createStackNavigator();

StatusBar.setHidden(true);


const StackNavigatorContainer = () => (
  <Stack.Navigator
    screenOptions={{
      headerShown: false
    }}>
    <Stack.Screen name="Home" component={MainHome} />
    {/* Add other Stack.Screen components as needed */}
  </Stack.Navigator>
);


function App() {
  return (

    <>
    <NavigationContainer >
      <StackNavigatorContainer />
    </NavigationContainer>
   
  </>

  );
}

export default App;