import React, { useEffect, useState } from 'react'
import { View, Text, Button, ActivityIndicator, PermissionsAndroid } from 'react-native'
import { NativeModules, TextInput, TouchableOpacity, ScrollView } from 'react-native';
import { Linking } from 'react-native';
import Modal from 'react-native-modal';
import { FetchedVideoDataInterface } from './utils/TypeImplements';

const MainHome = () => {

  const { MyMainModule } = NativeModules;

  const [availableVideoFormats, setAvailableVideoFormats] = useState<FetchedVideoDataInterface[]>([{ url: '', format: ' ', title: ' ' }]);
  const [inputVideoURL, setinputVideoURL] = useState("");
  const [isVisible, setIsVisible] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [renderKey, setRenderKey] = useState(Math.random());
  const [selectedOption, setSelectedOption] = useState<FetchedVideoDataInterface>({ url: '', format: ' ', title: ' ' });

  function isValidYouTubeUrl(url) {
    const pattern = /https?:\/\/(?:www\.)?(?:youtube\.com\/watch\?v=|youtu\.be\/)([a-zA-Z0-9_-]{11})/;
    return pattern.test(url);
  }

  const handleVideoFetch = () => {
    const validInfo = isValidYouTubeUrl(inputVideoURL)
    if (!validInfo) {
      MyMainModule.messageWarning("Input String is not a valid link")
    }
    else {
      setIsVisible(true)
      setIsLoading(true)
      setRenderKey(Math.random());
      MyMainModule.getAvailableFormats(inputVideoURL, (availableFormats) => {
        if (availableFormats === "Error: Unable to extract video formats") {
            MyMainModule.messageWarning(availableFormats)
        }
        else {
          setAvailableVideoFormats(availableFormats);
          console.log(availableFormats, " video formats");
        }


      });
      setIsLoading(false)
    }
  }

  const handleDownload = (selectedOption: FetchedVideoDataInterface) => {
    MyMainModule.downloadFromUrl(selectedOption.url, selectedOption.title, selectedOption.title, ((response) => {
      setIsLoading(false)
      setRenderKey(Math.random())
      setIsVisible(false)
    }))

  }





  useEffect(() => {

    const handleDeepLink = (event) => {
      const url = event.url;
      setinputVideoURL(url);
      handleVideoFetch();
      console.log("received url", url)
    };


    const linkingListener = Linking.addEventListener('url', handleDeepLink);

    Linking.getInitialURL().then((url) => {
      if (url) {
        console.log('App opened with URL:', url);
      }
    });

    return () => {
      linkingListener.remove();
    };
  }, []);

  useEffect(() => {
    (async () => {
      try {
        const granted = await PermissionsAndroid.request(
          PermissionsAndroid.PERMISSIONS.WRITE_EXTERNAL_STORAGE,
          {
            title: 'Youtube App Storage Permission',
            message:
              'You need storage permission to download files ',
            buttonNeutral: 'Ask Me Later',
            buttonNegative: 'Cancel',
            buttonPositive: 'OK',
          },
        );
        if (granted === PermissionsAndroid.RESULTS.GRANTED) {
          console.log('You can use the storage');
        } else {
          console.log('storage permission denied');
          MyMainModule.messageWarning("storage permission denied")
        }
      } catch (err) {
        console.warn(err);
      }
    })()
  }, [])



  return (
    <View style={{ display: "flex", flexDirection: "row", justifyContent: "center", alignItems: "center", height: "100%", width: "100%" }} >
      <View style={{ height: "80%", width: "80%", margin: 20, display: "flex", flexDirection: "column" }}>
        <View style={{ width: "100%", height: 50 }} >
          <TextInput
            style={{ color: "black", borderWidth: 1, }}
            placeholder="Enter Youtube Video Link"
            placeholderTextColor={"black"}
            onChangeText={(e) => { setinputVideoURL(e) }}
          />
        </View>
        <View style={{ width: "100%", height: 50, marginTop: 20, display: "flex", flexDirection: "row", justifyContent: "center" }} >
          <TouchableOpacity onPress={handleVideoFetch} style={{ borderWidth: 1, height: "100%", width: 150, backgroundColor: "#930093", display: "flex", justifyContent: "center", alignItems: "center" }} >
            <Text style={{ color: "white" }} >Fetch</Text>
          </TouchableOpacity>
        </View>

      </View>
      <View>
        <Modal isVisible={isVisible} onBackdropPress={() => setIsVisible(false)}>
          <View key={renderKey} style={{ backgroundColor: 'white', padding: 20, borderRadius: 10, }}>
            {isLoading ? (
              <>
                <ActivityIndicator size="large" color="blue" />
                <Text>Loading...</Text>
              </>
            ) : (
              <>
                <View style={{ display: "flex", flexDirection: "column", alignItems: "center" }} >
                  <Text style={{ fontSize: 18, fontWeight: 'bold', marginBottom: 10 }}>{availableVideoFormats[0].title}</Text>
                  <ScrollView style={{ maxHeight: 300 }} contentContainerStyle={{ alignItems: "center" }}>
                    {availableVideoFormats.map((option, index) => (
                      <TouchableOpacity
                        key={index}
                        style={{
                          flexDirection: 'row',
                          alignItems: 'center',
                          padding: 10,
                          width: 200,
                          backgroundColor: selectedOption === option ? '#ddd' : 'white',
                          borderRadius: 5,
                          marginVertical: 5,
                          borderWidth: 1,
                          borderColor: '#ccc'
                        }}
                        onPress={() => setSelectedOption(option)}
                      >
                        <View style={{
                          width: 20,
                          height: 20,
                          borderRadius: 10,
                          borderWidth: 2,
                          borderColor: selectedOption === option ? 'blue' : '#ccc',
                          justifyContent: 'center',
                          alignItems: 'center',
                          marginRight: 10
                        }}>
                          {selectedOption === option && (
                            <View style={{
                              width: 10,
                              height: 10,
                              borderRadius: 5,
                              backgroundColor: 'blue',
                            }} />
                          )}
                        </View>
                        <Text>{option.format}</Text>
                      </TouchableOpacity>
                    ))}
                  </ScrollView>
                  <View style={{ display: "flex", flexDirection: "row", justifyContent: "space-evenly", width: "100%", marginTop: 10 }} >
                    <Button
                      title="Confirm"
                      onPress={() => {
                        // alert(`You selected: ${selectedOption}`);
                        handleDownload(selectedOption)
                        setIsLoading(true)
                      }}
                      disabled={!selectedOption}
                    />
                    <Button
                      title="Cancel"
                      onPress={() => {
                        setIsVisible(false);
                      }}
                      disabled={!selectedOption}
                    />

                  </View>

                </View>
              </>
            )}
          </View>
        </Modal>
      </View>
    </View>
  )
}

export default MainHome