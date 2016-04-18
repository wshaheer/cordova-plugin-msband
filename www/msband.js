var service = "MSBandPlugin";
module.exports = {
  initialize: function(successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, service, "initialize", []);
  },
  connect: function(successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, service, "connect", []);
  },
  disconnect: function(successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, service, "disconnect", []);
  },
  getContactState: function(successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, service, "getContactState", []);
  },
  getConsent: function(successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, service, "getConsent", []);
  },
  subscribe: function(successCallback, errorCallback, type) {
    cordova.exec(successCallback, errorCallback, service, "subscribe", [type])
  },
  unsubscribe: function(successCallback, errorCallback, type) {
    cordova.exec(successCallback, errorCallback, service, "unsubscribe", [type]);
  },
  isConnected: function(successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, service, "isConnected", []);
  }
};
