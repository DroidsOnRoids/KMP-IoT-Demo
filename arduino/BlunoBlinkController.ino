long numBlinks = 0;

void setup() {
    Serial.begin(115200);
    pinMode(LED_BUILTIN, OUTPUT);
    digitalWrite(LED_BUILTIN, LOW);
}

void loop() {
    if (numBlinks > 0) {
        for (long i = 0; i < numBlinks; i++) {
            digitalWrite(LED_BUILTIN, HIGH);
            delay(200);
            digitalWrite(LED_BUILTIN, LOW);
            delay(200);
            if (Serial.available()) {
                numBlinks = Serial.parseInt();
                break;
            }
            if (i == numBlinks - 1) {
                Serial.println("C");
                numBlinks = 0;
            }
        }
    }

    if (Serial.available()) {
        numBlinks = Serial.parseInt();
    }
}
