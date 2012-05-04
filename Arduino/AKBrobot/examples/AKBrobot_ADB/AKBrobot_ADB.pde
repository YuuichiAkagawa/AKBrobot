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
#include <Adb.h>
#include "AKBrobot.h"

void setup();
void loop();

// Adb connection.
Connection * connection;

boolean flgReply = false;
uint8_t dataReply[4];

AKBrobot robo;
//Initialize
void setup()
{
  robo.init();

  // Initialize the ADB subsystem.  
  ADB::init();

  // Open an ADB stream to the phone's shell. Auto-reconnect
  connection = ADB::addConnection("tcp:4567", true, adbEventHandler);  
}

//Main loop
void loop()
{
  // Poll the ADB subsystem.
  ADB::poll();
  if(flgReply == true ){
    connection->write(4, dataReply);
    flgReply = false;
  }
}


// Event handler for the shell connection. 
void adbEventHandler(Connection * connection, adb_eventType event, uint16_t length, uint8_t * data)
{
  // Data packets contain four bytes
  //  +0 : Command
  //  +1 : Sub command
  //  +2 : Data1
  //  +3 : Data2
  if (event == ADB_CONNECTION_RECEIVE)
  {
    int val;
    switch(data[0]){
        case 0: //プリミティブ設定モード
          robo.configPort(data);
          break;
        case 1: //プリミティブ出力
          robo.outPort(data);
          break;
        case 2: //プリミティブ入力
          val = robo.inPort(data);
//	  data[2] = (byte)(val >> 8);
//	  data[3] = (byte)(val & 0xff);
//          connection->write(4, data);
          dataReply[0] = data[0];
          dataReply[1] = data[1];
          dataReply[2] = (uint8_t)(val >> 8);
          dataReply[3] = (uint8_t)(val & 0xff);
          flgReply = true;

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
}
