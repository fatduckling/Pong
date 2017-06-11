#include "utilities.h"

#define INPUT_SIZE 7

int tilt = 0;

/* Returns a number from DIFF_LEVEL to SCREEN_HEIGHT - 1 based on the potentiometer pin */
int getPotVal(){ 
    int val = analogRead(THRESHOLD_POTENTIOMETER_PIN);
    //map(value, fromLow, fromHigh, toLow, toHigh)
    // remap "val" from (0 to 1023) to (DIFF_LEVEL to SCREEN_HEIGHT - 1)
    int mapped = map(val,0,1023,DIFF_LEVEL, SCREEN_HEIGHT - 1);
    return mapped;
}

// reached the \n, therefore, act upon this
void act(const char* inputs, const int size){
  // make sure the input string starts and ends with {} respectively
  if (inputs[0] != '{' || inputs[size - 1] != '}'){
    return; // garbage data
  }
  // convert the string to a float. For example, {123} would return 123.00
  int d = extractTilt(inputs, size);
  // invalid number
  if (!isValidNum(d)){
    return;  
  } 
  // valid number, update the tilt
  tilt = d;
} 

/* extracts the data from the master device; eg {5} will return 5; 
   For any invalid data, returns INFINITY
*/
int extractTilt(const char* inputs, int size){
  int tmp;
  switch (size){ 
    case 3: { // for exampe {5}
      tmp = charToInt(inputs[1]); // extract the number 
      // if this number is valid, return it; otherwise return infinity
      return (isValidNum(tmp)) ? tmp : INFINITY; 
    }  
    case 4: { // for example {-3} or {10}     
      tmp = charToInt(inputs[2]);
      if (inputs[1] == '-'){ // first character is a minus sign 
        /* get the number in the braces and multiply it by -1; if this number 
        is valid, then return it; otherwise, return INFINITY */
        return -1 * ((isValidNum(tmp)) ? tmp : INFINITY);
      }
      else {
        return 10; // only valid two digit number that is positive  
      }
    }
    case 5: return -10; // only valid three digit number
  }   
  return INFINITY; // invalid string
}

// returns true if this number is in range of [-10, 10]
inline boolean isValidNum(int num){
  return (num >= -10 && num <= 10);
}

// returns the integer representation of a character. For example, '3' would return 3
inline int charToInt(const char c){
  return (((int) c) - 48);
}

/* 
  Read incoming byte from the master device (if such exist); if not, return;
  Add this byte to our "inputs" array; If we've reached the '\n', call method "act()"
*/
void updateTilt(){
  // create an array of inputs
  static char* inputs = NULL;
  // index of the character we are trying to insert
  static int index;
  if (inputs == NULL){
    inputs = (char*) malloc(INPUT_SIZE);
    inputs[INPUT_SIZE - 1] = '\0';
    index = 0;
  }  
  
  int c;
  // read the incoming byte (if there are bytes incoming)
  while (c = bluetooth.read(), c >= 0) {
    if (c == '\n') {
      // we read the entire string, so act upon this
      act(inputs, index); 
      // reset
      bluetooth.flush();
      index = 0;
    }
    // buffer full: string, reset
    else if (index == INPUT_SIZE - 1){
      // buffer full 
      bluetooth.flush();
      index = 0;
    }    
    // insert the new character to our input array
    else {
      inputs[index++] = char(c);
    }
  }
} 

/* Continually check whether the button has been pressed; 
   If it has not been pressed, print the string "s" at (x,y), rotated at degree "deg"
*/
void waitForInput(UTFT* renderer, String s, int x, int y, int deg){
  for (unsigned int i = 0; digitalRead(BUTTON_INPUT) != 1; i ++){ 
    renderer->setColor(i);
    renderer->print(s, CENTER, y, deg); 
    if (i == 0xFFFFFF) {  // maximum
      i = 0;
    }
  }
}

// sends the outcome of the game to the master device
void sendScore(int playerScore, int aiScore){
  bluetooth.print(playerScore);
  bluetooth.println(aiScore);
}

// returns the tilt
int getTilt(){
  return tilt;  
}


