package com.sensauratech.msband;

import com.microsoft.band.*;
import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandConnectionCallback;
import com.microsoft.band.BandErrorType;
import com.microsoft.band.BandException;
import com.microsoft.band.BandInfo;
import com.microsoft.band.BandIOException;
import com.microsoft.band.BandPendingResult;
import com.microsoft.band.BandResultCallback;
import com.microsoft.band.ConnectionState;
import com.microsoft.band.InvalidBandVersionException;
import com.microsoft.band.UserConsent;
import com.microsoft.band.sensors.BandContactState;
import com.microsoft.band.sensors.BandContactEvent;
import com.microsoft.band.sensors.BandContactEventListener;
import com.microsoft.band.sensors.BandGsrEvent;
import com.microsoft.band.sensors.BandGsrEventListener;
import com.microsoft.band.sensors.BandHeartRateEvent;
import com.microsoft.band.sensors.BandHeartRateEventListener;
import com.microsoft.band.sensors.BandRRIntervalEvent;
import com.microsoft.band.sensors.BandRRIntervalEventListener;
import com.microsoft.band.sensors.BandSkinTemperatureEvent;
import com.microsoft.band.sensors.BandSkinTemperatureEventListener;
import com.microsoft.band.sensors.HeartRateConsentListener;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

@SuppressWarnings("unchecked")

public class MSBandPlugin extends CordovaPlugin {
  private BandClient bandClient;
  private BandInfo device;

  public boolean execute(String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException {
    if (action.equals("initialize")) {
      cordova.getThreadPool().execute(new Runnable() {
        public void run() {
          initialize(args, callbackContext);
        }
      });

      return true;
    } else if (action.equals("connect")) {
      cordova.getThreadPool().execute(new Runnable() {
        public void run() {
          connect(args, callbackContext);
        }
      });

      return true;
    } else if (action.equals("disconnect")) {
      cordova.getThreadPool().execute(new Runnable() {
        public void run() {
          disconnect(args, callbackContext);
        }
      });

      return true;
    } else if (action.equals("contact")) {
      cordova.getThreadPool().execute(new Runnable() {
        public void run() {
          contact(args, callbackContext);
        }
      });

      return true;
    } else if (action.equals("consent")) {
      cordova.getThreadPool().execute(new Runnable() {
        public void run() {
          consent(args, callbackContext);
        }
      });

      return true;
    } else if (action.equals("subscribe")) {
      cordova.getThreadPool().execute(new Runnable() {
        public void run() {
          subscribe(args, callbackContext);
        }
      });

      return true;
    } else if (action.equals("unsubscribe")) {
      cordova.getThreadPool().execute(new Runnable() {
        public void run() {
          unsubscribe(args, callbackContext);
        }
      });

      return true;
    } else if (action.equals("isConnected")) {
      cordova.getThreadPool().execute(new Runnable() {
        public void run() {
          isConnected(args, callbackContext);
        }
      });

      return true;
    }

    return false;
  }

  protected void initialize(JSONArray args, CallbackContext callbackContext) {
    this.device = BandClientManager.getInstance().getPairedBands()[0];
    this.bandClient = BandClientManager.getInstance().create(cordova.getActivity(), this.device);
    JSONObject obj = new JSONObject();
    if (this.device == null) {
      addProperty(obj, "error", "notInitialized");
      addProperty(obj, "message", "No paired devices found");
      callbackContext.error(obj);
    } else {
      addProperty(obj, "name", this.device.getName());
      addProperty(obj, "address", this.device.getMacAddress());
      callbackContext.success(obj);
    }
  }

  protected void connect(JSONArray args, final CallbackContext callbackContext) {
    this.bandClient.registerConnectionCallback(new BandConnectionCallback() {
      @Override
      public void onStateChanged(ConnectionState state) {
        JSONObject obj = new JSONObject();
        addProperty(obj, "name", MSBandPlugin.this.device.getName());
        addProperty(obj, "address", MSBandPlugin.this.device.getMacAddress());
        addProperty(obj, "status", state.name().toString());
        PluginResult result = new PluginResult(PluginResult.Status.OK, obj);
        result.setKeepCallback(true);
        callbackContext.sendPluginResult(result);
      }
    });
    try {
      this.bandClient.connect().await();
      if (this.bandClient.getConnectionState() != ConnectionState.CONNECTED) {
        JSONObject obj = new JSONObject();
        addProperty(obj, "error", "notConnected");
        addProperty(obj, "message", "Could not connect to device");
        callbackContext.error(obj);
      }
    } catch(InterruptedException e) {
      // handle exception
      JSONObject obj = new JSONObject();
      addProperty(obj, "error", "interruptedException");
      addProperty(obj, "message", e.getMessage());
      callbackContext.error(obj);
    } catch(BandException e) {
      // handle exception
      JSONObject obj = new JSONObject();
      addProperty(obj, "error", "bandException");
      addProperty(obj, "message", e.getMessage());
      callbackContext.error(obj);
    }
  }

  protected void disconnect(JSONArray args, CallbackContext callbackContext) {
    this.bandClient.unregisterConnectionCallback();
    try {
      this.bandClient.disconnect().await();

      JSONObject obj = new JSONObject();
      addProperty(obj, "name", this.device.getName());
      addProperty(obj, "address", this.device.getMacAddress());
      addProperty(obj, "status", this.bandClient.getConnectionState().name().toString());
      callbackContext.success(obj);
    } catch(InterruptedException e) {
      // handle exception
      JSONObject obj = new JSONObject();
      addProperty(obj, "error", "interruptedException");
      addProperty(obj, "message", e.getMessage());
      callbackContext.error(obj);
    } catch(BandException e) {
      // handle exception
      JSONObject obj = new JSONObject();
      addProperty(obj, "error", "bandException");
      addProperty(obj, "message", e.getMessage());
      callbackContext.error(obj);
    }
  }

  protected void contact(JSONArray args, final CallbackContext callbackContext) {
    if (this.bandClient.getConnectionState() == ConnectionState.CONNECTED) {
      try {
        this.bandClient.getSensorManager().registerContactEventListener(new BandContactEventListener() {
          @Override
          public void onBandContactChanged(BandContactEvent event) {
            try {
              JSONObject obj = new JSONObject();
              addProperty(obj, "status", event.getContactState().name().toString());
              PluginResult result = new PluginResult(PluginResult.Status.OK, obj);
              result.setKeepCallback(true);
              callbackContext.sendPluginResult(result);
            } catch (Exception e) {
              // handle exception
              JSONObject obj = new JSONObject();
              addProperty(obj, "error", "unknownException");
              addProperty(obj, "message", e.getMessage());
              callbackContext.error(obj);
            }
          }
        });
      } catch (BandIOException e) {
        // handle exception
        JSONObject obj = new JSONObject();
        addProperty(obj, "error", "bandIOException");
        addProperty(obj, "message", e.getMessage());
        callbackContext.error(obj);
      }
    }
  }

  protected void consent(JSONArray args, final CallbackContext callbackContext) {
    if (this.bandClient.getConnectionState() == ConnectionState.CONNECTED) {
      if (this.bandClient.getSensorManager().getCurrentHeartRateConsent() == UserConsent.GRANTED) {
        try {
          JSONObject obj = new JSONObject();
          addProperty(obj, "isGranted", true);
          PluginResult result = new PluginResult(PluginResult.Status.OK, obj);
          callbackContext.sendPluginResult(result);
        } catch (Exception e) {
          // handle exception
        }
      } else {
        this.bandClient.getSensorManager().requestHeartRateConsent(cordova.getActivity(), new HeartRateConsentListener() {
          @Override
          public void userAccepted(boolean response) {
            try {
              JSONObject obj = new JSONObject();
              addProperty(obj, "isGranted", response);
              PluginResult result = new PluginResult(PluginResult.Status.OK, obj);
              callbackContext.sendPluginResult(result);
            } catch (Exception e) {
              // handle exception
            }
          }
        });
      }
    }
  }

  protected void subscribe(final JSONArray args, final CallbackContext callbackContext) {
    if (this.bandClient.getConnectionState() == ConnectionState.CONNECTED) {
      try {
        if (args.getString(0).equals("HEART_RATE")) {
          if (this.bandClient.getSensorManager().getCurrentHeartRateConsent() == UserConsent.GRANTED) {
            this.bandClient.getSensorManager().registerHeartRateEventListener(new BandHeartRateEventListener() {
              @Override
              public void onBandHeartRateChanged(BandHeartRateEvent event) {
                int heartRate = event.getHeartRate();
                long timeStamp = event.getTimestamp();
                try {
                  JSONObject obj = new JSONObject();
                  addProperty(obj, "heartRate", heartRate);
                  addProperty(obj, "timeStamp", timeStamp);
                  PluginResult result = new PluginResult(PluginResult.Status.OK, obj);
                  result.setKeepCallback(true);
                  callbackContext.sendPluginResult(result);
                } catch (Exception e) {
                  // handle exception
                }
              }
            });
          }
        } else if (args.getString(0).equals("RR_INTERVAL")) {
          this.bandClient.getSensorManager().registerRRIntervalEventListener(new BandRRIntervalEventListener() {
            @Override
            public void onBandRRIntervalChanged(BandRRIntervalEvent event) {
              double interval = event.getInterval();
              long timeStamp = event.getTimestamp();
              try {
                JSONObject obj = new JSONObject();
                addProperty(obj, "interval", interval);
                addProperty(obj, "timeStamp", timeStamp);
                PluginResult result = new PluginResult(PluginResult.Status.OK, obj);
                result.setKeepCallback(true);
                callbackContext.sendPluginResult(result);
              } catch (Exception e) {
                // handle exception
              }
            }
          });
        } else if (args.getString(0).equals("GSR")) {
          this.bandClient.getSensorManager().registerGsrEventListener(new BandGsrEventListener() {
            @Override
            public void onBandGsrChanged(BandGsrEvent event) {
              int resistance = event.getResistance();
              long timeStamp = event.getTimestamp();
              try {
                JSONObject obj = new JSONObject();
                addProperty(obj, "resistance", resistance);
                addProperty(obj, "timeStamp", timeStamp);
                PluginResult result = new PluginResult(PluginResult.Status.OK, obj);
                result.setKeepCallback(true);
                callbackContext.sendPluginResult(result);
              } catch (Exception e) {
                // handle exception
              }
            }
          });
        } else if (args.getString(0).equals("SKIN_TEMPERATURE")) {
          this.bandClient.getSensorManager().registerSkinTemperatureEventListener(new BandSkinTemperatureEventListener() {
            @Override
            public void onBandSkinTemperatureChanged(BandSkinTemperatureEvent event) {
              double temperature = event.getTemperature();
              long timeStamp = event.getTimestamp();
              try {
                JSONObject obj = new JSONObject();
                addProperty(obj, "temperature", temperature);
                addProperty(obj, "timeStamp", timeStamp);
                PluginResult result = new PluginResult(PluginResult.Status.OK, obj);
                result.setKeepCallback(true);
                callbackContext.sendPluginResult(result);
              } catch (Exception e) {
                // handle exception
              }
            }
          });
        } else if (args.getString(0).equals("BAND_CONTACT")) {
          this.bandClient.getSensorManager().registerContactEventListener(new BandContactEventListener() {
            @Override
            public void onBandContactChanged(BandContactEvent event) {
              try {
                JSONObject obj = new JSONObject();
                addProperty(obj, "status", event.getContactState().name().toString());
                PluginResult result = new PluginResult(PluginResult.Status.OK, obj);
                result.setKeepCallback(true);
                callbackContext.sendPluginResult(result);
              } catch (Exception e) {
                // handle exception
                JSONObject obj = new JSONObject();
                addProperty(obj, "error", "unknownException");
                addProperty(obj, "message", e.getMessage());
                callbackContext.error(obj);
              }
            }
          });
        }
      } catch (InvalidBandVersionException e) {
        // handle exception
        JSONObject obj = new JSONObject();
        addProperty(obj, "error", "invalidBandVersionException");
        addProperty(obj, "message", e.getMessage());
        callbackContext.error(obj);
      } catch (BandIOException e) {
        // handle exception
        JSONObject obj = new JSONObject();
        addProperty(obj, "error", "bandIOException");
        addProperty(obj, "message", e.getMessage());
        callbackContext.error(obj);
      } catch (BandException e) {
        // handle exception
        JSONObject obj = new JSONObject();
        addProperty(obj, "error", "bandException");
        addProperty(obj, "message", e.getMessage());
        callbackContext.error(obj);
      } catch (JSONException e) {
        // handle exception
        JSONObject obj = new JSONObject();
        addProperty(obj, "error", "jsonException");
        addProperty(obj, "message", e.getMessage());
        callbackContext.error(obj);
      }
    }
  }

  protected void unsubscribe(JSONArray args, CallbackContext callbackContext) {
    if (this.bandClient.getConnectionState() == ConnectionState.CONNECTED) {
      try {
        if (args.getString(0).equals("HEART_RATE")) {
          this.bandClient.getSensorManager().unregisterHeartRateEventListeners();
          JSONObject obj = new JSONObject();
          addProperty(obj, "sensor", "HeartRateSensor");
          addProperty(obj, "status", "unsubscribed");
          callbackContext.success(obj);
        } else if (args.getString(0).equals("RR_INTERVAL")) {
          this.bandClient.getSensorManager().unregisterRRIntervalEventListeners();
          JSONObject obj = new JSONObject();
          addProperty(obj, "sensor", "RRIntervalSensor");
          addProperty(obj, "status", "unsubscribed");
          callbackContext.success(obj);
        } else if (args.getString(0).equals("GSR")) {
          this.bandClient.getSensorManager().unregisterGsrEventListeners();
          JSONObject obj = new JSONObject();
          addProperty(obj, "sensor", "GSRSensor");
          addProperty(obj, "status", "unsubscribed");
          callbackContext.success(obj);
        } else if (args.getString(0).equals("SKIN_TEMPERATURE")) {
          this.bandClient.getSensorManager().unregisterSkinTemperatureEventListeners();
          JSONObject obj = new JSONObject();
          addProperty(obj, "sensor", "SkinTemperatureSensor");
          addProperty(obj, "status", "unsubscribed");
          callbackContext.success(obj);
        } else if (args.getString(0).equals("BAND_CONTACT")) {
          this.bandClient.getSensorManager().unregisterContactEventListeners();
          JSONObject obj = new JSONObject();
          addProperty(obj, "sensor", "BandContactSensor");
          addProperty(obj, "status", "unsubscribed");
          callbackContext.success(obj);
        }
      } catch (BandException e) {
        // handle exception
        JSONObject obj = new JSONObject();
        addProperty(obj, "error", "bandException");
        addProperty(obj, "message", e.getMessage());
        callbackContext.error(obj);
      } catch (JSONException e) {
        // handle exception
        JSONObject obj = new JSONObject();
        addProperty(obj, "error", "jsonException");
        addProperty(obj, "message", e.getMessage());
        callbackContext.error(obj);
      }
    }
  }

  protected void isConnected(JSONArray args, CallbackContext callbackContext) {
    JSONObject obj = new JSONObject();
    addProperty(obj, "isConnected", this.bandClient.isConnected());
    callbackContext.success(obj);
  }

  private void addProperty(JSONObject obj, String key, Object value) {
    try {
      if (value == null) {
        obj.put(key, JSONObject.NULL);
      } else {
        obj.put(key, value);
      }
    } catch (JSONException e) {
      // handle exception
    }
  }

}
