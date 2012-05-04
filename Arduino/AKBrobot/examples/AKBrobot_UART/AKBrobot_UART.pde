/*
  Copyright 2012 JAG-AKIBA

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/
#include <Servo.h>
#include "AKBrobot.h"
#define RECVTIMEOUT 100	//ms

void setup();
void loop();
uint8_t getdata(byte *buf, size_t len);


AKBrobot robo;
//Initialize
void setup()
{
  robo.init();
  Serial.begin(115200);
}

//Main loop
void loop()
{
  byte data[4];
  static byte count = 0;
  
    int len = getdata(data, sizeof(data));
    int val;
    //Write function
    if (len > 0) {
      // assumes only one command per packet
      switch(data[0]){
        case 0: //プリミティブ設定モード
          robo.configPort(data);
          break;
        case 1: //プリミティブ出力
          robo.outPort(data);
          break;
        case 2: //プリミティブ入力
          val = robo.inPort(data);
	  data[2] = (byte)(val >> 8);
	  data[3] = (byte)(val & 0xff);
	  Serial.write(data, 4);
          break;
        case 3: //モータ制御 (data[1]:モータ番号, data[2],data[3]:データ)
          robo.ctrlMotor(data);
          break;
        case 4: //リレー制御 (data[1]:リレー番号, data[2],data[3]:データ)
          robo.ctrlRelay(data);
          break;
        case 5: //サーボ制御 (data[1]:サーボ番号, data[2],data[3]:データ)
          robo.ctrlServo(data);
          break;
        default:
          break;
      }
    }
  delay(5);
}

uint8_t getdata(byte *buf, size_t len)
{
  char c;
  size_t i;
  unsigned long starttime;

  if (Serial.available() < 0) {
	return 0;
  }
  starttime = millis();

  for(i=0; i<len; i++){
    if (Serial.available() > 0) {
      c = Serial.read();
      buf[i] = (byte)c;
    }else{
      if( millis() - starttime > RECVTIMEOUT ) { //Timeout
         return 0;
      }
    }
  }
  return (i - 1);
}
