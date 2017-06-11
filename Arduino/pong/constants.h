#ifndef CONSTANTS_H
// ensure you don't load this file more than once
#define CONSTANTS_H

#ifndef UTFT_h
  #include <memorysaver.h>
  #include <UTFT.h>
#endif

#define INFINITY 99999.0

#define bluetooth Serial2

// player properties
#define PLAYER_WIDTH 50
#define PLAYER_HEIGHT 10
#define BALL_RADIUS 6 // make this an even number

// game properties
#define SCORE_LIMIT 5 // make this an odd number 
#define DIFF_LEVEL 70 // minimum difficulty of the game
#define MINIMUM_GAME_DELAY 1
#define MAXIMUM_GAME_DELAY 10

// pins
#define BUTTON_INPUT 6
#define GAME_FPS_POTENTIOMETER 9
#define THRESHOLD_POTENTIOMETER_PIN 8 

// screen properties
#define SCREEN_WIDTH 320 // you can also do renderer->getDisplayXSize();
#define SCREEN_HEIGHT 240

// game states
enum GameStates { IDLE, PLAY, SCORE, GAMEOVER };

// fonts for UTFT renderer
extern uint8_t SevenSegNumFont[];
extern uint8_t BigFont[];
extern uint8_t SmallFont[];

#endif //CONSTANTS_H
