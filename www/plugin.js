var exec = require('cordova/exec');

var PLUGIN_NAME = 'GoogleVisionPlugin';

var GoogleVisionPlugin = {
  detectText: function(pattern, cb, errCb) {
    return exec(cb, errCb, PLUGIN_NAME, 'detect', [pattern]);
  }
};


module.exports = GoogleVisionPlugin;
