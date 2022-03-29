var exec = require('cordova/exec');

var PLUGIN_NAME = 'GoogleVisionPlugin';

var GoogleVisionPlugin = {
  detectText: function(pattern, detectOne, takePhoto, cb, errCb) {
    return exec(cb, errCb, PLUGIN_NAME, 'detect', [pattern, detectOne, takePhoto]);
  }
};

module.exports = GoogleVisionPlugin;
