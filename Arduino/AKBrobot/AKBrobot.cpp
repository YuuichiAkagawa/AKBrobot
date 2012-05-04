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
#include "AKBrobot.h"

AKBrobot::AKBrobot()
{
}

void AKBrobot::init()
{
  //Initialize Motors
  motors[0].in1 = MOTOR1A;
  motors[0].in2 = MOTOR1B;
  motors[0].pwm = MOTOR1P;
  motors[1].in1 = MOTOR2A;
  motors[1].in2 = MOTOR2B;
  motors[1].pwm = MOTOR2P;
  for(int i=0; i<_MAX_MOTORS; i++){
    pinMode(motors[i].in1, OUTPUT);
    pinMode(motors[i].in2, OUTPUT);
    digitalWrite(motors[i].in1, LOW);
    digitalWrite(motors[i].in2, LOW);
  }
    
  //Initialize Relays
  relays[0] = RELAY1;
  relays[1] = RELAY2;
  pinMode(RELAY1, OUTPUT);
  pinMode(RELAY2, OUTPUT);

  //Initialize Servos
  servos[0].attach(SERVO1);
  servos[0].write(90);
  servolist[0] = SERVO1;
}

//Primitive port assignment
// data[1] : 0x00-0x3f : digital port
//           0x40-0x7f : analog port
//           0x80-0xbf : Output digital port(PWM) / Input analog port

//Configure primitive port
// data[3] : 0 : digital out
//           1 : analog out(PWM)
//           2 : digital in
//           3 : analog in
void AKBrobot::configPort(uint8_t *data)
{
  int portno;

//Servo check
	for(uint8_t i=0; i<_MAX_SERVOS; i++){
		if( servolist[i] == (data[1] & 0x3f) ){
			servos[i].detach();
		}
	}

  switch(data[3]){
    case 0 :  //digital out
      if( data[1] < 0x40 ){        // digital port
        portno = data[1];
      }else if( data[1] < 0x80 ){  // analog port
        portno = (data[1] & 0x3f) + A0;
      }
      pinMode(portno, OUTPUT);
      break;
    case 1 :
      //Not need
      break;
    case 2 :
      if( data[1] < 0x40 ){        // digital port
        portno = data[1];
      }else if( data[1] < 0x80 ){  // analog port
        portno = (data[1] & 0x3f) + A0;
      }
      pinMode(portno, INPUT);
      break;
    case 3 :
      //Not need
      break;
    defalut :
      break;
  }
}

//Input from primitive port
int AKBrobot::inPort(uint8_t *data)
{
  int pindata = 0;
  int portno;
  uint8_t outdata[4];
  if( data[1] < 0x40 ){        // digital port
    pindata = digitalRead(data[1]);
  }else if( data[1] < 0x80 ){  // analog port(digital mode)
    portno = (data[1] & 0x3f) + A0;
    pindata = digitalRead(portno);
  }else if( data[1] < 0xc0 ){  // analog port(analog mode)
    portno = (data[1] & 0x3f) + A0;
    pindata = analogRead(portno);
  }else{
  }

  outdata[0] = data[0];
  outdata[1] = data[1];  
  outdata[2] = (uint8_t)((pindata >> 8) & 0xff);
  outdata[3] = (uint8_t)(pindata & 0xff);
  Serial.write(outdata, 4);
  return(pindata);
}

//Output to primitive port
void AKBrobot::outPort(uint8_t *data)
{
  if( data[1] < 0x40 ){        // digital port
    digitalWrite(data[1], data[3] ? HIGH : LOW);
  }else if( data[1] < 0x80 ){  // analog port
    int portno = (data[1] & 0x3f) + A0;
    digitalWrite(portno, data[3] ? HIGH : LOW);
  }else if( data[1] < 0xc0 ){  // digital port(PWM)
    int portno = (data[1] & 0x3f);
    analogWrite(portno, data[3]);
  }else{
  }
}

//Motor control
void AKBrobot::ctrlMotor(uint8_t *data)
{
  if( data[1] < _MAX_MOTORS ){
    if( data[3] == 0) { //STOP
      digitalWrite(motors[data[1]].in1, LOW);
      digitalWrite(motors[data[1]].in2, LOW);
      digitalWrite(motors[data[1]].pwm, 0);
    }else{
      if( data[2] == 0){ //³“]
        digitalWrite(motors[data[1]].in1, HIGH);
        digitalWrite(motors[data[1]].in2, LOW);
      }else{             //‹t“]
        digitalWrite(motors[data[1]].in1, LOW);
        digitalWrite(motors[data[1]].in2, HIGH);
      }
      analogWrite(motors[data[1]].pwm, data[3]);
    }
  }
}

//Relay control
void AKBrobot::ctrlRelay(uint8_t *data)
{
  if( data[1] < _MAX_RELAYS ){
    digitalWrite(relays[data[1]], data[3] ? HIGH : LOW);
  }
}

//ServoMotor control
void AKBrobot::ctrlServo(uint8_t *data)
{
  if( data[1] < _MAX_SERVOS ){
    servos[data[1]].write(map(data[3], 0, 255, 0, 180));
  }
}

