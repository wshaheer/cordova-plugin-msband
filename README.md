
**Only ANDROID works atm (required for a project), i forked wshaheer's repo and removed the initiate method as i thought it was obsolete. **

 1. Connect to Device
 2. Ask for consent if you want heartrate info
 3. Subscribe to an event

ex: Connect to device

    msband.connect(function (res) {
            if (res.status === "CONNECTED") {
                msband.consent(function (msg) {
                }, function (err) {
                });
                msband.getVersionInfo(function (msg) {
                    //hwversion in msg.hardwareVersion
                    //firmwareVersion msg.firmwareVersion;
                }, function (err) {
                });
            }
        }, function (err) {
            console.log(err);
        });

ex: Subscribe to event

    msband.subscribe(function (msg) {
            console.log(msg);
        }, function (err) {
            console.log(err);
        }, 'HEART_RATE');



