#include <Wire.h>
#include "BluetoothSerial.h"

// =======================
// 🔵 BLUETOOTH
// =======================
BluetoothSerial SerialBT;

// =======================
// 📡 GPS (USE WORKING PINS)
// =======================
HardwareSerial gpsSerial(2);
String gpsData = "";
String latitude = "0.0";
String longitude = "0.0";
bool gpsValid = false;

// =======================
// 📟 MPU6050
// =======================
#define MPU_ADDR 0x68

// =======================
// 🍺 MQ3 SENSOR
// =======================
#define MQ3_PIN 34
#define ALCOHOL_THRESHOLD 800

// =======================
// 🔔 OUTPUT
// =======================
#define BUZZER 25
#define LED 26

#define DIFF_THRESHOLD 20000

long lastTotal = 0;
unsigned long lastCrashTime = 0;
int alcoholCount = 0;

// =======================
// 🧠 EXTRACT GPS (SIMPLE)
// =======================
void extractGPS(String data) {

  if (data.indexOf("$GPRMC") >= 0) {

    int commas[12];
    int index = 0;

    for (int i = 0; i < data.length(); i++) {
      if (data[i] == ',') {
        commas[index++] = i;
      }
    }

    // Check valid fix (A)
    if (data.substring(commas[1]+1, commas[2]) == "A") {

      latitude = data.substring(commas[2]+1, commas[3]);
      longitude = data.substring(commas[4]+1, commas[5]);

      gpsValid = true;
    }
  }
}

// =======================
// SETUP
// =======================
void setup() {
  Serial.begin(115200);
  SerialBT.begin("SmartHelmet");

  // 🔥 USE WORKING PINS (your test success)
  gpsSerial.begin(9600, SERIAL_8N1, 16, 17);

  Wire.begin();
  Wire.setClock(100000);

  pinMode(BUZZER, OUTPUT);
  pinMode(LED, OUTPUT);

  // Wake MPU6050
  Wire.beginTransmission(MPU_ADDR);
  Wire.write(0x6B);
  Wire.write(0);
  Wire.endTransmission(true);

  Serial.println("SYSTEM READY");
}

// =======================
// LOOP
// =======================
void loop() {

  // =======================
  // 📡 GPS READ
  // =======================
  while (gpsSerial.available()) {
    char c = gpsSerial.read();
    gpsData += c;

    if (c == '\n') {
      extractGPS(gpsData);
      gpsData = "";
    }
  }

  // =======================
  // 🍺 ALCOHOL DETECTION
  // =======================
  int alcoholValue = analogRead(MQ3_PIN);

  Serial.print("Alcohol: ");
  Serial.println(alcoholValue);

  if (alcoholValue > ALCOHOL_THRESHOLD) {
    alcoholCount++;
  } else {
    alcoholCount = 0;
  }

  if (alcoholCount >= 3) {

    Serial.println("ALCOHOL DETECTED!");

    digitalWrite(LED, HIGH);
    digitalWrite(BUZZER, HIGH);

    SerialBT.print("ALCOHOL ALERT\n");

    if (gpsValid) {
      SerialBT.print("Location: ");
      SerialBT.print(latitude);
      SerialBT.print(", ");
      SerialBT.println(longitude);
    } else {
      SerialBT.println("No GPS Fix");
    }

    delay(2000);

    digitalWrite(LED, LOW);
    digitalWrite(BUZZER, LOW);
    alcoholCount = 0;
  }

  // =======================
  // 🚨 CRASH DETECTION
  // =======================
  Wire.beginTransmission(MPU_ADDR);
  Wire.write(0x3B);
  Wire.endTransmission(false);
  Wire.requestFrom(MPU_ADDR, 6, true);

  if (Wire.available() == 6) {

    int16_t AccX = (Wire.read() << 8) | Wire.read();
    int16_t AccY = (Wire.read() << 8) | Wire.read();
    int16_t AccZ = (Wire.read() << 8) | Wire.read();

    long total = abs(AccX) + abs(AccY) + abs(AccZ);
    long diff = abs(total - lastTotal);

    Serial.print("Total: ");
    Serial.print(total);
    Serial.print(" | Diff: ");
    Serial.println(diff);

    if (diff >= DIFF_THRESHOLD && millis() - lastCrashTime > 5000) {

      Serial.println("CRASH DETECTED!");

      digitalWrite(LED, HIGH);

      for (int i = 0; i < 3; i++) {
        digitalWrite(BUZZER, HIGH);
        delay(200);
        digitalWrite(BUZZER, LOW);
        delay(200);
      }

      SerialBT.print("CRASH ALERT\n");

      if (gpsValid) {
        SerialBT.print("Location: ");
        SerialBT.print(latitude);
        SerialBT.print(", ");
        SerialBT.println(longitude);
      } else {
        SerialBT.println("No GPS Fix");
      }

      lastCrashTime = millis();

      delay(2000);
      digitalWrite(LED, LOW);
    }

    lastTotal = total;
  }

  delay(300);
}
