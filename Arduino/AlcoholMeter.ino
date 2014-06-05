
int sensorValue;

void setup()
{
  Serial.begin(9600);      // sets the serial port to 9600
}

void loop()
{
  sensorValue = analogRead(0);       // read analog input pin 0
  Serial.print("s");
  Serial.print(sensorValue, DEC);  // prints the value read
  Serial.print("\r");
  delay(300);                        // wait 100ms for next reading
}
