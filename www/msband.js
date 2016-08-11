var service = "MSBandPlugin";
module.exports = {
    connect: function (successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, service, "connect", []);
    },
    disconnect: function (successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, service, "disconnect", []);
    },
    contact: function (successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, service, "contact", []);
    },
    consent: function (successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, service, "consent", []);
    },
    subscribe: function (successCallback, errorCallback, type) {
        cordova.exec(successCallback, errorCallback, service, "subscribe", [type])
    },
    subscribeWithArg: function (successCallback, errorCallback, type, arg) {
        cordova.exec(successCallback, errorCallback, service, "subscribe", [type, arg])
    },
    unsubscribe: function (successCallback, errorCallback, type) {
        cordova.exec(successCallback, errorCallback, service, "unsubscribe", [type]);
    },
    isConnected: function (successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, service, "isConnected", []);
    },
    getVersionInfo: function (successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, service, "getVersionInfo", []);
    }
};
