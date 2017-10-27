Cordova GoogleVision Plugin 
======
[![Build Status](https://travis-ci.org/erikeuserr/cordova-googlevision-plugin.svg?branch=master)](https://travis-ci.org/erikeuserr/cordova-googlevision-plugin)


### Installing



```
cordova plugin add cordova-googlevision-plugin
```
 
### Usage



```javascript

        GoogleVisionPlugin.detectText("<RegexPattern>", function(out){
          // Log the detected text here.
            console.log(out);
        });
```
 

