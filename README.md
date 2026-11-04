# Smart Helmet -- Accident Detection and Emergency Alert System

An IoT-based smart helmet prototype designed to improve two-wheeler
rider safety by detecting possible accidents, monitoring alcohol
presence, and communicating alerts to an Android application.

> **Project type:** Academic / hackathon prototype\
> **Controller:** ESP32\
> **Mobile application:** Android (Android Studio, Kotlin/Java and XML
> as applicable to the implemented app)\
> **Communication:** Bluetooth Classic\
> **Status:** Prototype; validate each feature on the actual hardware
> before relying on it.

------------------------------------------------------------------------

## 1. Project overview

The Smart Helmet prototype combines embedded sensors, an ESP32
controller, GPS positioning, and an Android application.

The intended workflow is:

1.  The MPU6050 accelerometer and gyroscope provide motion data to the
    ESP32.
2.  The ESP32 evaluates the sensor readings using the accident-detection
    logic in the firmware.
3.  The alcohol sensor is monitored by the ESP32.
4.  When an alert condition is detected, the ESP32 activates the
    connected buzzer and/or LED as configured in the firmware.
5.  The ESP32 sends alert data to the Android application over
    Bluetooth.
6.  When GPS data is available, the application/system can use the
    location information for emergency communication.
7.  The Android application displays alerts and, where implemented and
    permissions are granted, can send an emergency SMS.

The actual behaviour depends on the firmware, Android app
implementation, sensor wiring, phone permissions, and GPS reception.

## 2. Main features

-   **Accident detection:** Uses MPU6050 accelerometer and gyroscope
    readings with threshold-based logic in the ESP32 firmware.
-   **Alcohol detection:** Reads the connected alcohol sensor and checks
    it against the firmware's configured condition.
-   **Local alert:** Uses an LED and buzzer to indicate configured alert
    conditions.
-   **Bluetooth communication:** Sends status or alert messages from the
    ESP32 to the paired Android phone using Bluetooth Classic.
-   **GPS positioning:** Uses a NEO-6M GPS module to obtain coordinates
    when the module has a valid satellite fix.
-   **Android application:** Receives and presents helmet alerts.
-   **Emergency messaging:** The app may send an emergency SMS for a
    crash alert if this feature is present in the installed app, the
    required permission is granted, and the phone has cellular service.

> GPS reception may be unavailable indoors or before the module obtains
> a satellite fix. An emergency alert should not be assumed to have been
> delivered merely because an event was detected.

## 3. Hardware components

  -----------------------------------------------------------------------
  Component                           Purpose
  ----------------------------------- -----------------------------------
  ESP32 development board             Main controller and Bluetooth
                                      communication

  MPU6050                             Accelerometer and gyroscope data
                                      for motion-based crash detection

  Alcohol sensor                      Alcohol presence/sensor-level
                                      monitoring

  NEO-6M GPS module                   Obtains geographical coordinates
                                      outdoors with satellite reception

  LED                                 Visual status/alert indication

  Buzzer                              Audible status/alert indication

  Power supply                        Supplies the prototype modules
  -----------------------------------------------------------------------

The exact alcohol-sensor model, wiring, GPIO assignments, thresholds,
and power arrangement should be documented from the final physical
prototype and firmware. They are intentionally not assumed in this
README.

## 4. Software and technologies

-   Arduino IDE
-   ESP32 Arduino core
-   Embedded C/C++ for ESP32 firmware
-   Adafruit MPU6050 library (as used in the prototype)
-   Android Studio
-   Android application source code (Kotlin/Java and XML files,
    according to the project)
-   Bluetooth Classic communication
-   GPS data from the NEO-6M module

Check the source files and installed library versions before reproducing
the build.

## 5. Repository structure

The repository is intended to be organized as follows:

``` text
Smart-Helmet-Accident-Detection/
│
├── Android_App/
│   ├── app/
│   ├── gradle/
│   ├── .gitignore
│   ├── build.gradle.kts
│   ├── gradle.properties
│   ├── gradlew
│   ├── gradlew.bat
│   ├── settings.gradle.kts
│   └── README.md
│
├── ESP32_Firmware/
│   └── SmartHelmet.ino
│
├── Images/
│   └── (prototype and application screenshots)
│
└── README.md
```

The names above are the recommended layout. Keep the actual filenames
and folder names in the repository consistent with the files you upload.
Add any additional source files required by your Android project.

## 6. Setting up the ESP32 firmware

1.  Install the Arduino IDE.
2.  Install the ESP32 board support package using the Arduino IDE Boards
    Manager.
3.  Install the libraries required by the firmware. The project history
    indicates use of the Adafruit MPU6050 library; install its
    dependencies as requested by the library manager.
4.  Open `ESP32_Firmware/SmartHelmet.ino`.
5.  Select the correct ESP32 board and COM port.
6.  Check the GPIO assignments against your actual wiring.
7.  Check sensor thresholds and Bluetooth device name against the
    Android application.
8.  Upload the firmware and open Serial Monitor at the baud rate
    configured in the sketch.
9.  Test each module separately before testing the combined prototype.

Do not assume the pin assignments or threshold values from another
example sketch. Use the values in the final, tested firmware.

## 7. Setting up the Android application

1.  Install Android Studio.
2.  Open Android Studio and select **Open**.
3.  Select the `Android_App` folder (the folder containing the
    project-level Gradle settings file).
4.  Allow Gradle sync to finish.
5.  Resolve any missing SDK or dependency prompts using versions
    compatible with the project.
6.  Connect an Android phone with USB debugging enabled, or use a
    supported test device.
7.  Build and run the application.
8.  Grant only the permissions required by the app and Android version.

### Bluetooth pairing

-   Enable Bluetooth on the Android phone.
-   Pair the phone with the ESP32 using the Bluetooth name configured in
    the firmware.
-   Confirm that the device name and the message format expected by the
    app match the firmware.
-   Test the connection before testing alert behaviour.

### Emergency SMS and permissions

If the Android app includes automatic SMS functionality, review its
source code and manifest to confirm the required permission and
behaviour. Configure the intended emergency contact privately on the
device or through a suitable configuration method.

**Do not commit personal phone numbers, private credentials, signing
keys, or other secrets to a public repository.** Test SMS functionality
only with a consenting test number. Android version, SIM availability,
carrier service, runtime permissions, and device restrictions can affect
SMS delivery.

## 8. Testing checklist

Test the prototype in a controlled and safe environment. Do not create a
real crash or expose anyone to alcohol to test the system.

-   [ ] ESP32 powers on reliably.
-   [ ] MPU6050 is detected and produces plausible readings.
-   [ ] Accident-detection logic responds to safe, controlled movement
    tests.
-   [ ] Alcohol sensor readings are available and its threshold is
    calibrated for the sensor and environment.
-   [ ] LED and buzzer respond to the intended conditions.
-   [ ] ESP32 Bluetooth device is visible and can be paired.
-   [ ] Android app connects to the correct ESP32 device.
-   [ ] Firmware alert messages match the format expected by the app.
-   [ ] Android app displays the expected alert.
-   [ ] NEO-6M produces valid coordinates outdoors after obtaining a
    satellite fix.
-   [ ] Emergency SMS behaviour is tested with consent and without
    relying on it as the only emergency channel.
-   [ ] Background operation is tested on the target phone with the
    required permissions and battery settings.

Record test results, phone model, Android version, firmware version, and
any limitations in the project documentation.

## 9. Limitations and safety

-   This is an academic prototype, not a certified motorcycle safety
    device.
-   Threshold-based accident detection can produce false positives and
    false negatives.
-   Alcohol-sensor readings depend on sensor type, calibration, warm-up,
    placement, and environmental conditions. They are not a certified
    measurement of blood alcohol concentration.
-   GPS may not obtain a fix indoors, in tunnels, or in areas with poor
    satellite visibility.
-   Bluetooth range and reliability depend on the phone, environment,
    antenna, and power conditions.
-   Android background execution, notification, Bluetooth, and SMS rules
    vary by Android version and device manufacturer.
-   SMS delivery is not guaranteed and may be delayed or fail.
-   Do not use this prototype as a replacement for a certified helmet,
    safe riding practices, or official emergency services.

## 10. Future improvements

Possible future work includes:

-   More robust crash classification and false-alarm cancellation.
-   Sensor calibration and documented test datasets.
-   Improved power management and enclosure design.
-   Reliable GPS-fix validation and location handling.
-   Secure, well-defined communication messages between ESP32 and
    Android.
-   Cloud-based monitoring or an authorized emergency-service
    integration, subject to privacy, reliability, and legal
    requirements.

## 11. Contributors

Add the names and contributions of the project team here.

## 12. License

No license is specified yet. If you want others to reuse or modify this
project, choose and add an appropriate open-source license after
confirming that all included code and dependencies can be distributed
under it.
