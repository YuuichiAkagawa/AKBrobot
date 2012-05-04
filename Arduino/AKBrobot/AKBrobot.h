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
#if !defined(_AKBROBOT_H_)
#define _AKBROBOT_H_

#include "Servo.h"

#if defined(ARDUINO) && ARDUINO >=100
#include <Arduino.h>
#else
#include <WProgram.h>
#endif


//Core Module pin assign
#define _MAX_MOTORS 2
#define _MAX_RELAYS 2
#define _MAX_SERVOS 1

#define RELAY1 2
#define RELAY2 4
#define MOTOR1A A0
#define MOTOR1B A1
#define MOTOR1P 3
#define MOTOR2A A2
#define MOTOR2B A3
#define MOTOR2P 5
#define SERVO1  6

//Static classes
//Motors
class Motors {
public:
  int in1;
  int in2;
  int pwm;
  Motors(){};
  Motors(int in1, int in2, int pwm) : in1(in1), in2(in2), pwm(pwm){};
};


class AKBrobot
{
private:
	Motors motors[_MAX_MOTORS];
	int relays[_MAX_RELAYS];
	Servo servos[_MAX_SERVOS];
	uint8_t servolist[_MAX_SERVOS];

public:
	AKBrobot();
	void init();
	void configPort(uint8_t *data);
	int  inPort(uint8_t *data);
	void outPort(uint8_t *data);
	void ctrlMotor(uint8_t *data);
	void ctrlRelay(uint8_t *data);
	void ctrlServo(uint8_t *data);
};

#endif //_AKBROBO_H_
