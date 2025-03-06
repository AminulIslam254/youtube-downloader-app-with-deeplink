import React, { useEffect, useState } from 'react'
import { View, Text, Button, ActivityIndicator } from 'react-native'
import { NativeModules, TextInput, TouchableOpacity } from 'react-native';
import { Linking } from 'react-native';
import Modal from 'react-native-modal';

const MainHome = () => {

  const { MyMainModule } = NativeModules;

  const [availableVideoFormats, setAvailableVideoFormats] = useState([{ url: '', format: ' ', title: ' ' }]);
  const [inputVideoURL, setinputVideoURL] = useState("");
  const [isVisible, setIsVisible] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [selectedOption, setSelectedOption] = useState({ url: '', format: ' ', title: ' ' });

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
      MyMainModule.getAvailableFormats(inputVideoURL, (availableFormats) => {
        setAvailableVideoFormats(availableFormats);
      });
      setIsLoading(false)
    }
  }

  const handleDownload=(selectedOption)=>{
    console.log(selectedOption)
  }
  

  useEffect(() => {

    const handleDeepLink = (event) => {
      const url = event.url;
      setinputVideoURL(url);
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
          <View style={{ backgroundColor: 'white', padding: 20, borderRadius: 10, }}>
            {isLoading ? (
              <>
                <ActivityIndicator size="large" color="blue" />
                <Text>Loading...</Text>
              </>
            ) : (
              <>
                <View style={{ display: "flex", flexDirection: "column", alignItems: "center" }} >
                  <Text style={{ fontSize: 18, fontWeight: 'bold', marginBottom: 10 }}>{availableVideoFormats[0].title}</Text>

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

                  <View style={{ display: "flex", flexDirection: "row", justifyContent: "space-evenly",width:"100%" }} >
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