var exec = require('cordova/exec');

var PLUGIN_NAME = 'GoogleVisionPlugin';

var GoogleVisionPlugin = {
  detectText: function(cb) {
    exec(cb, null, PLUGIN_NAME, 'detect', []);
  }
};

module.exports = GoogleVisionPlugin;
