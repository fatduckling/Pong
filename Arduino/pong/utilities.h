#ifndef UTILITIES_H
#define UTILITIES_H

#include "constants.h"

// return the accelerometer value from the master device
int getTilt();

// updates the accelerometer value from the master device 
void updateTilt();

// returns the integer representation of a character. For example, '3' would return 3
inline int charToInt(const char c);

/* extracts the data from the master device; eg {5} will return 5; 
   For any invalid data, returns INFINITY
*/
int extractTilt(const char* inputs, int size);

// returns true if the number is in range [-10, 10]
inline boolean isValidNum(int num);

// block execution until button has been pressed
void waitForInput(UTFT* renderer, String s, int x, int y, int deg = 0);

// send the outcome of the game to the master device
void sendScore(int score1,int score2);

// returns the current tilt value from the master device
int getPotVal();

#endif //UTILITIES_H
