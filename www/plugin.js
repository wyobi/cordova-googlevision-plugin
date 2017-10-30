var exec = require('cordova/exec');

var PLUGIN_NAME = 'GoogleVisionPlugin';

var GoogleVisionPlugin = {
  detectText: function(pattern, cb) {
    exec(cb, null, PLUGIN_NAME, 'detect', [pattern]);
  }
};


module.exports = GoogleVisionPlugin;
