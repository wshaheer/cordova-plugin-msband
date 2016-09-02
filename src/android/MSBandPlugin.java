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
import com.microsoft.band.sensors.BandAccelerometerEvent;
import com.microsoft.band.sensors.BandAccelerometerEventListener;
import com.microsoft.band.sensors.BandAltimeterEvent;
import com.microsoft.band.sensors.BandAltimeterEventListener;
import com.microsoft.band.sensors.BandAmbientLightEvent;
import com.microsoft.band.sensors.BandAmbientLightEventListener;
import com.microsoft.band.sensors.BandBarometerEvent;
import com.microsoft.band.sensors.BandBarometerEventListener;
import com.microsoft.band.sensors.BandCaloriesEvent;
import com.microsoft.band.sensors.BandCaloriesEventListener;
import com.microsoft.band.sensors.BandContactState;
import com.microsoft.band.sensors.BandContactEvent;
import com.microsoft.band.sensors.BandContactEventListener;
import com.microsoft.band.sensors.BandDistanceEvent;
import com.microsoft.band.sensors.BandDistanceEventListener;
import com.microsoft.band.sensors.BandGsrEvent;
import com.microsoft.band.sensors.BandGsrEventListener;
import com.microsoft.band.sensors.BandGyroscopeEvent;
import com.microsoft.band.sensors.BandGyroscopeEventListener;
import com.microsoft.band.sensors.BandHeartRateEvent;
import com.microsoft.band.sensors.BandHeartRateEventListener;
import com.microsoft.band.sensors.BandPedometerEvent;
import com.microsoft.band.sensors.BandPedometerEventListener;
import com.microsoft.band.sensors.BandRRIntervalEvent;
import com.microsoft.band.sensors.BandRRIntervalEventListener;
import com.microsoft.band.sensors.BandSkinTemperatureEvent;
import com.microsoft.band.sensors.BandSkinTemperatureEventListener;
import com.microsoft.band.sensors.BandUVEvent;
import com.microsoft.band.sensors.BandUVEventListener;
import com.microsoft.band.sensors.HeartRateConsentListener;
import com.microsoft.band.sensors.SampleRate;

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
        if (action.equals("connect")) {
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
        } else if (action.equals("getVersionInfo")) {
            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    getVersionInfo(args, callbackContext);
                }
            });
            return true;
        }
        return false;
    }

    protected void connect(JSONArray args, final CallbackContext callbackContext) {
        try {
            BandInfo[] allBands = BandClientManager.getInstance().getPairedBands();
            if (allBands.length < 1) {
                throw new BandException("no band found", BandErrorType.DEVICE_ERROR);
            }
            this.device = allBands[0];
            this.bandClient = BandClientManager.getInstance().create(cordova.getActivity(), this.device);
            if (this.bandClient == null) {
                throw new BandException("bandclient could not be registerd", BandErrorType.DEVICE_ERROR);
            }
        } catch (BandException e) {
            JSONObject obj = new JSONObject();
            addProperty(obj, "error", "no band");
            addProperty(obj, "message", e.getMessage());
            callbackContext.error(obj);
        } catch (Exception e) {
            JSONObject obj = new JSONObject();
            if (this.device == null) {
                addProperty(obj, "error", "notInitialized");
                addProperty(obj, "message", e.getMessage());
                callbackContext.error(obj);
            } else {
                addProperty(obj, "name", this.device.getName());
                addProperty(obj, "address", this.device.getMacAddress());
                callbackContext.success(obj);
            }
        }

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
                addProperty(obj, "name", "notConnected");
                addProperty(obj, "message", "Could not connect to device");
                callbackContext.error(obj);
            }
        } catch (InterruptedException e) {
            // handle exception
            JSONObject obj = new JSONObject();
            addProperty(obj, "error", "interruptedException");
            addProperty(obj, "message", e.getMessage());
            callbackContext.error(obj);
        } catch (BandException e) {
            // handle exception
            JSONObject obj = new JSONObject();
            addProperty(obj, "error", "bandException");
            addProperty(obj, "message", e.getMessage());
            callbackContext.error(obj);
        }
    }

    protected void getVersionInfo(JSONArray args, CallbackContext callbackContext) {
        try {
            if (this.bandClient.getConnectionState() == ConnectionState.CONNECTED) {
                String fwVersion = bandClient.getFirmwareVersion().await();
                String hwVersion = bandClient.getHardwareVersion().await();
                JSONObject obj = new JSONObject();
                addProperty(obj, "firmwareVersion", fwVersion);
                addProperty(obj, "hardwareVersion", hwVersion);
                callbackContext.success(obj);
            } else {
                JSONObject obj = new JSONObject();
                addProperty(obj, "error", "getVersionInfo");
                addProperty(obj, "message", "band is not connected");
                callbackContext.error(obj);
            }
        } catch (InterruptedException e) {
            // handle exception
            JSONObject obj = new JSONObject();
            addProperty(obj, "error", "interruptedException");
            addProperty(obj, "message", e.getMessage());
            callbackContext.error(obj);
        } catch (BandException e) {
            // handle exception
            JSONObject obj = new JSONObject();
            addProperty(obj, "error", "firmwareexception");
            addProperty(obj, "message", e.getMessage());
            callbackContext.error(obj);
        }

    }

    protected void disconnect(JSONArray args, CallbackContext callbackContext) {

        try {
            if (this.bandClient.getConnectionState() == ConnectionState.CONNECTED) {
                this.bandClient.unregisterConnectionCallback();
                this.bandClient.disconnect().await();
                JSONObject obj = new JSONObject();
                addProperty(obj, "name", this.device.getName());
                addProperty(obj, "address", this.device.getMacAddress());
                addProperty(obj, "status", this.bandClient.getConnectionState().name().toString());
                callbackContext.success(obj);
            } else {
                JSONObject obj = new JSONObject();
                addProperty(obj, "error", "disconnect");
                addProperty(obj, "message", "band is not connected, connect to disconnect...");
                callbackContext.error(obj);
            }
        } catch (InterruptedException e) {
            // handle exception
            JSONObject obj = new JSONObject();
            addProperty(obj, "error", "interruptedException");
            addProperty(obj, "message", e.getMessage());
            callbackContext.error(obj);
        } catch (BandException e) {
            // handle exception
            JSONObject obj = new JSONObject();
            addProperty(obj, "error", "bandException");
            addProperty(obj, "message", e.getMessage());
            callbackContext.error(obj);
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
                    JSONObject obj = new JSONObject();
                    addProperty(obj, "error", "consent");
                    addProperty(obj, "message", e.getMessage());
                    callbackContext.error(obj);
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
                            JSONObject obj = new JSONObject();
                            addProperty(obj, "error", "consent");
                            addProperty(obj, "message", e.getMessage());
                            callbackContext.error(obj);
                        }
                    }
                });
            }
        } else {
            // handle exception
            JSONObject obj = new JSONObject();
            addProperty(obj, "error", "consent");
            addProperty(obj, "message", "band is not connected");
            callbackContext.error(obj);
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
                                try {
                                    JSONObject obj = new JSONObject();
                                    addProperty(obj, "heartRate", event.getHeartRate());
                                    addProperty(obj, "quality", event.getQuality());
                                    addProperty(obj, "timeStamp", event.getTimestamp());
                                    PluginResult result = new PluginResult(PluginResult.Status.OK, obj);
                                    result.setKeepCallback(true);
                                    callbackContext.sendPluginResult(result);
                                } catch (Exception e) {
                                    JSONObject obj = new JSONObject();
                                    addProperty(obj, "error", "HEART_RATE");
                                    addProperty(obj, "message", e.getMessage());
                                    callbackContext.error(obj);
                                }
                            }
                        });
                    } else {
                        JSONObject obj = new JSONObject();
                        addProperty(obj, "error", "subscribe");
                        addProperty(obj, "message", "user dit not grant permission for heartrate");
                        callbackContext.error(obj);
                    }
                } else if (args.getString(0).equals("RR_INTERVAL")) {
                    this.bandClient.getSensorManager().registerRRIntervalEventListener(new BandRRIntervalEventListener() {
                        @Override
                        public void onBandRRIntervalChanged(BandRRIntervalEvent event) {
                            try {
                                double interval = event.getInterval();
                                long timeStamp = event.getTimestamp();
                                JSONObject obj = new JSONObject();
                                addProperty(obj, "interval", interval);
                                addProperty(obj, "timeStamp", timeStamp);
                                PluginResult result = new PluginResult(PluginResult.Status.OK, obj);
                                result.setKeepCallback(true);
                                callbackContext.sendPluginResult(result);
                            } catch (Exception e) {
                                JSONObject obj = new JSONObject();
                                addProperty(obj, "error", "RR_INTERVAL");
                                addProperty(obj, "message", e.getMessage());
                                callbackContext.error(obj);
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
                                JSONObject obj = new JSONObject();
                                addProperty(obj, "error", "GSR");
                                addProperty(obj, "message", e.getMessage());
                                callbackContext.error(obj);
                            }
                        }
                    });
                } else if (args.getString(0).equals("SKIN_TEMPERATURE")) {
                    this.bandClient.getSensorManager().registerSkinTemperatureEventListener(new BandSkinTemperatureEventListener() {
                        @Override
                        public void onBandSkinTemperatureChanged(BandSkinTemperatureEvent event) {
                            try {
                                double temperature = event.getTemperature();
                                long timeStamp = event.getTimestamp();
                                JSONObject obj = new JSONObject();
                                addProperty(obj, "temperature", temperature);
                                addProperty(obj, "timeStamp", timeStamp);
                                PluginResult result = new PluginResult(PluginResult.Status.OK, obj);
                                result.setKeepCallback(true);
                                callbackContext.sendPluginResult(result);
                            } catch (Exception e) {
                                JSONObject obj = new JSONObject();
                                addProperty(obj, "error", "SKIN_TEMPERATURE");
                                addProperty(obj, "message", e.getMessage());
                                callbackContext.error(obj);
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
                                addProperty(obj, "error", "BAND_CONTACT");
                                addProperty(obj, "message", e.getMessage());
                                callbackContext.error(obj);
                            }
                        }
                    });
                } else if (args.getString(0).equals("ACCELEROMETER")) {
                    if (args.length() < 2) {
                        throw new IllegalArgumentException("sample interval needed");
                    }
                    int sampleInterval = 0;
                    try {
                        sampleInterval = Integer.parseInt(args.getString(1));
                    } catch (NumberFormatException e) {
                        JSONObject obj = new JSONObject();
                        addProperty(obj, "error", "ACCELEROMETER");
                        addProperty(obj, "message", "sample interval needed (in hz)");
                        callbackContext.error(obj);
                    }
                    SampleRate s;
                    switch (sampleInterval) {
                        case 62:
                            s = SampleRate.MS16;
                            break;
                        case 31:
                            s = SampleRate.MS32;
                            break;
                        case 8:
                            s = SampleRate.MS128;
                            break;
                        default:
                            throw new IllegalArgumentException("Samplerate must be either 8, 31 or 62");
                    }

                    this.bandClient.getSensorManager().registerAccelerometerEventListener(new BandAccelerometerEventListener() {
                        @Override
                        public void onBandAccelerometerChanged(BandAccelerometerEvent event) {
                            try {
                                JSONObject obj = new JSONObject();
                                addProperty(obj, "x", event.getAccelerationX());
                                addProperty(obj, "y", event.getAccelerationY());
                                addProperty(obj, "z", event.getAccelerationZ());
                                addProperty(obj, "timeStamp", event.getTimestamp());
                                PluginResult result = new PluginResult(PluginResult.Status.OK, obj);
                                result.setKeepCallback(true);
                                callbackContext.sendPluginResult(result);
                            } catch (Exception e) {
                                // handle exception
                                JSONObject obj = new JSONObject();
                                addProperty(obj, "error", "ACCELEROMETER");
                                addProperty(obj, "message", e.getMessage());
                                callbackContext.error(obj);
                            }
                        }
                    }, s);
                } else if (args.getString(0).equals("GYROSCOPE")) {
                    if (args.length() < 2) {
                        throw new IllegalArgumentException("sample interval needed");
                    }
                    int sampleInterval = 0;
                    try {
                        sampleInterval = Integer.parseInt(args.getString(1));
                    } catch (NumberFormatException e) {
                        JSONObject obj = new JSONObject();
                        addProperty(obj, "error", "GYROSCOPE");
                        addProperty(obj, "message", "sample interval needed (in hz)");
                        callbackContext.error(obj);
                    }
                    SampleRate s;
                    switch (sampleInterval) {
                        case 62:
                            s = SampleRate.MS16;
                            break;
                        case 31:
                            s = SampleRate.MS32;
                            break;
                        case 8:
                            s = SampleRate.MS128;
                            break;
                        default:
                            throw new IllegalArgumentException("Samplerate must be either 8, 31 or 62");
                    }

                    this.bandClient.getSensorManager().registerGyroscopeEventListener(new BandGyroscopeEventListener() {
                        @Override
                        public void onBandGyroscopeChanged(BandGyroscopeEvent event) {
                            try {
                                JSONObject obj = new JSONObject();
                                addProperty(obj, "accX", event.getAccelerationX());
                                addProperty(obj, "axxY", event.getAccelerationY());
                                addProperty(obj, "accZ", event.getAccelerationZ());
                                addProperty(obj, "velX", event.getAngularVelocityX());
                                addProperty(obj, "velY", event.getAngularVelocityY());
                                addProperty(obj, "velZ", event.getAngularVelocityZ());
                                addProperty(obj, "timeStamp", event.getTimestamp());
                                PluginResult result = new PluginResult(PluginResult.Status.OK, obj);
                                result.setKeepCallback(true);
                                callbackContext.sendPluginResult(result);
                            } catch (Exception e) {
                                // handle exception
                                JSONObject obj = new JSONObject();
                                addProperty(obj, "error", "GYROSCOPE");
                                addProperty(obj, "message", e.getMessage());
                                callbackContext.error(obj);
                            }
                        }
                    }, s);
                } else if (args.getString(0).equals("ALTIMETER")) {
                    this.bandClient.getSensorManager().registerAltimeterEventListener(new BandAltimeterEventListener() {
                        @Override
                        public void onBandAltimeterChanged(BandAltimeterEvent event) {
                            try {
                                JSONObject obj = new JSONObject();
                                addProperty(obj, "flAsToday", event.getFlightsAscendedToday());
                                addProperty(obj, "flAs", event.getFlightsAscended());
                                addProperty(obj, "flDe", event.getFlightsDescended());
                                addProperty(obj, "rate", event.getRate());
                                addProperty(obj, "stepGain", event.getSteppingGain());
                                addProperty(obj, "stepLoss", event.getSteppingLoss());
                                addProperty(obj, "stepsAs", event.getStepsAscended());
                                addProperty(obj, "stepsDe", event.getStepsDescended());
                                addProperty(obj, "totalGain", event.getTotalGain());
                                addProperty(obj, "gainToday", event.getTotalGainToday());
                                addProperty(obj, "totalLoss", event.getTotalLoss());
                                addProperty(obj, "timeStamp", event.getTimestamp());
                                PluginResult result = new PluginResult(PluginResult.Status.OK, obj);
                                result.setKeepCallback(true);
                                callbackContext.sendPluginResult(result);
                            } catch (Exception e) {
                                // handle exception
                                JSONObject obj = new JSONObject();
                                addProperty(obj, "error", "ALTIMETER");
                                addProperty(obj, "message", e.getMessage());
                                callbackContext.error(obj);
                            }
                        }
                    });
                } else if (args.getString(0).equals("DISTANCE")) {
                    this.bandClient.getSensorManager().registerDistanceEventListener(new BandDistanceEventListener() {
                        @Override
                        public void onBandDistanceChanged(BandDistanceEvent event) {
                            try {
                                JSONObject obj = new JSONObject();
                                addProperty(obj, "distanceToday", event.getDistanceToday());
                                addProperty(obj, "totalDistance", event.getTotalDistance());
                                addProperty(obj, "motionType", event.getMotionType().toString());
                                addProperty(obj, "pace", event.getPace());
                                addProperty(obj, "speed", event.getSpeed());
                                addProperty(obj, "timeStamp", event.getTimestamp());
                                PluginResult result = new PluginResult(PluginResult.Status.OK, obj);
                                result.setKeepCallback(true);
                                callbackContext.sendPluginResult(result);
                            } catch (Exception e) {
                                // handle exception
                                JSONObject obj = new JSONObject();
                                addProperty(obj, "error", "DISTANCE");
                                addProperty(obj, "message", e.getMessage());
                                callbackContext.error(obj);
                            }
                        }
                    });
                } else if (args.getString(0).equals("PEDOMETER")) {
                    this.bandClient.getSensorManager().registerPedometerEventListener(new BandPedometerEventListener() {
                        @Override
                        public void onBandPedometerChanged(BandPedometerEvent event) {
                            try {
                                JSONObject obj = new JSONObject();
                                addProperty(obj, "stepsToday", event.getStepsToday());
                                addProperty(obj, "totalSteps", event.getTotalSteps());
                                addProperty(obj, "timeStamp", event.getTimestamp());
                                PluginResult result = new PluginResult(PluginResult.Status.OK, obj);
                                result.setKeepCallback(true);
                                callbackContext.sendPluginResult(result);
                            } catch (Exception e) {
                                // handle exception
                                JSONObject obj = new JSONObject();
                                addProperty(obj, "error", "PEDOMETER");
                                addProperty(obj, "message", e.getMessage());
                                callbackContext.error(obj);
                            }
                        }
                    });
                } else if (args.getString(0).equals("CALORIES")) {
                    this.bandClient.getSensorManager().registerCaloriesEventListener(new BandCaloriesEventListener() {
                        @Override
                        public void onBandCaloriesChanged(BandCaloriesEvent event) {
                            try {
                                JSONObject obj = new JSONObject();
                                addProperty(obj, "totalCalories", event.getCalories());
                                addProperty(obj, "caloriesToday", event.getCaloriesToday());
                                addProperty(obj, "timeStamp", event.getTimestamp());
                                PluginResult result = new PluginResult(PluginResult.Status.OK, obj);
                                result.setKeepCallback(true);
                                callbackContext.sendPluginResult(result);
                            } catch (Exception e) {
                                // handle exception
                                JSONObject obj = new JSONObject();
                                addProperty(obj, "error", "CALORIES");
                                addProperty(obj, "message", e.getMessage());
                                callbackContext.error(obj);
                            }
                        }
                    });
                } else if (args.getString(0).equals("BAROMETER")) {
                    this.bandClient.getSensorManager().registerBarometerEventListener(new BandBarometerEventListener() {
                        @Override
                        public void onBandBarometerChanged(BandBarometerEvent event) {
                            try {
                                JSONObject obj = new JSONObject();
                                addProperty(obj, "airPressure", event.getAirPressure());
                                addProperty(obj, "temperature", event.getTemperature());
                                addProperty(obj, "timeStamp", event.getTimestamp());
                                PluginResult result = new PluginResult(PluginResult.Status.OK, obj);
                                result.setKeepCallback(true);
                                callbackContext.sendPluginResult(result);
                            } catch (Exception e) {
                                // handle exception
                                JSONObject obj = new JSONObject();
                                addProperty(obj, "error", "BAROMETER");
                                addProperty(obj, "message", e.getMessage());
                                callbackContext.error(obj);
                            }
                        }
                    });
                } else if (args.getString(0).equals("UV")) {
                    this.bandClient.getSensorManager().registerUVEventListener(new BandUVEventListener() {
                        @Override
                        public void onBandUVChanged(BandUVEvent event) {
                            try {
                                JSONObject obj = new JSONObject();
                                addProperty(obj, "uvExposureToday", event.getUVExposureToday());
                                addProperty(obj, "uvIndexLevel", event.getUVIndexLevel());
                                addProperty(obj, "timeStamp", event.getTimestamp());
                                PluginResult result = new PluginResult(PluginResult.Status.OK, obj);
                                result.setKeepCallback(true);
                                callbackContext.sendPluginResult(result);
                            } catch (Exception e) {
                                // handle exception
                                JSONObject obj = new JSONObject();
                                addProperty(obj, "error", "UV");
                                addProperty(obj, "message", e.getMessage());
                                callbackContext.error(obj);
                            }
                        }
                    });
                } else if (args.getString(0).equals("AMBIENTLIGHT")) {
                    this.bandClient.getSensorManager().registerAmbientLightEventListener(new BandAmbientLightEventListener() {
                        @Override
                        public void onBandAmbientLightChanged(BandAmbientLightEvent event) {
                            try {
                                JSONObject obj = new JSONObject();
                                addProperty(obj, "brightness", event.getBrightness());
                                addProperty(obj, "timeStamp", event.getTimestamp());
                                PluginResult result = new PluginResult(PluginResult.Status.OK, obj);
                                result.setKeepCallback(true);
                                callbackContext.sendPluginResult(result);
                            } catch (Exception e) {
                                // handle exception
                                JSONObject obj = new JSONObject();
                                addProperty(obj, "error", "AMBIENTLIGHT");
                                addProperty(obj, "message", e.getMessage());
                                callbackContext.error(obj);
                            }
                        }
                    });
                }
            } catch (InvalidBandVersionException e) {
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
            } catch (IllegalArgumentException e) {
                JSONObject obj = new JSONObject();
                addProperty(obj, "error", "Subscribe - Invalid Argument");
                addProperty(obj, "message", e.getMessage());
                callbackContext.error(obj);
            }
        } else {
            // handle exception
            JSONObject obj = new JSONObject();
            addProperty(obj, "error", "subscribe");
            addProperty(obj, "message", "band is not connected");
            callbackContext.error(obj);
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
                } else if (args.getString(0).equals("ACCELEROMETER")) {
                    this.bandClient.getSensorManager().unregisterAccelerometerEventListeners();
                    JSONObject obj = new JSONObject();
                    addProperty(obj, "sensor", "Accelerometer");
                    addProperty(obj, "status", "unsubscribed");
                    callbackContext.success(obj);
                } else if (args.getString(0).equals("GYROSCOPE")) {
                    this.bandClient.getSensorManager().unregisterGyroscopeEventListeners();
                    JSONObject obj = new JSONObject();
                    addProperty(obj, "sensor", "Gyroscope");
                    addProperty(obj, "status", "unsubscribed");
                    callbackContext.success(obj);
                } else if (args.getString(0).equals("ALTIMETER")) {
                    this.bandClient.getSensorManager().unregisterAltimeterEventListeners();
                    JSONObject obj = new JSONObject();
                    addProperty(obj, "sensor", "Altimeter");
                    addProperty(obj, "status", "unsubscribed");
                    callbackContext.success(obj);
                } else if (args.getString(0).equals("DISTANCE")) {
                    this.bandClient.getSensorManager().unregisterDistanceEventListeners();
                    JSONObject obj = new JSONObject();
                    addProperty(obj, "sensor", "Distance");
                    addProperty(obj, "status", "unsubscribed");
                    callbackContext.success(obj);
                } else if (args.getString(0).equals("PEDOMETER")) {
                    this.bandClient.getSensorManager().unregisterPedometerEventListeners();
                    JSONObject obj = new JSONObject();
                    addProperty(obj, "sensor", "Pedometer");
                    addProperty(obj, "status", "unsubscribed");
                    callbackContext.success(obj);
                } else if (args.getString(0).equals("CALORIES")) {
                    this.bandClient.getSensorManager().unregisterCaloriesEventListeners();
                    JSONObject obj = new JSONObject();
                    addProperty(obj, "sensor", "Calories");
                    addProperty(obj, "status", "unsubscribed");
                    callbackContext.success(obj);
                } else if (args.getString(0).equals("BAROMETER")) {
                    this.bandClient.getSensorManager().unregisterBarometerEventListeners();
                    JSONObject obj = new JSONObject();
                    addProperty(obj, "sensor", "Barometer");
                    addProperty(obj, "status", "unsubscribed");
                    callbackContext.success(obj);
                } else if (args.getString(0).equals("UV")) {
                    this.bandClient.getSensorManager().unregisterUVEventListeners();
                    JSONObject obj = new JSONObject();
                    addProperty(obj, "sensor", "UV");
                    addProperty(obj, "status", "unsubscribed");
                    callbackContext.success(obj);
                } else if (args.getString(0).equals("AMBIENTLIGHT")) {
                    this.bandClient.getSensorManager().unregisterAmbientLightEventListeners();
                    JSONObject obj = new JSONObject();
                    addProperty(obj, "sensor", "AmbientLight");
                    addProperty(obj, "status", "unsubscribed");
                    callbackContext.success(obj);
                } else if (args.getString(0).equals("ALL")) {
                    this.bandClient.getSensorManager().unregisterAllListeners();
                    JSONObject obj = new JSONObject();
                    addProperty(obj, "sensor", "all");
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
