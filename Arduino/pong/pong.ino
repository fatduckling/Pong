#include "constants.h"
#include "rectangle.h"
#include "utilities.h"

/* Create the objects to display in the screen */
Rectangle* player1;
Rectangle* player2;
Rectangle* ball;

// sore the current game state in this variable
GameStates currentGameState;

int playerScore = 0, // score for player 1
    compScore = 0;   // score for player 2


UTFT* renderer;
void setup() {
  // initialise random number generator
  randomSeed(analogRead(0));
  // set the baud rate for Serial2
  bluetooth.begin(9600);
  
  pinMode(BUTTON_INPUT,INPUT);

  // create the renderer
  renderer = new UTFT(ITDB28, 38, 39, 40, 41);

  //This will reset color to white with black background. Selected font will be reset to none.
  renderer->InitLCD();
  //Clear the screen. The background-color will be set to black.
  renderer->clrScr();

  // create the players (player 1)
  player1 = new Rectangle(PLAYER_WIDTH, PLAYER_HEIGHT); // human player
  player1->setRenderer(renderer);
  // place player1 in the bottom-centre of the screen
  player1->setPosition(((SCREEN_WIDTH - player1->getWidth()) / 2), (SCREEN_HEIGHT - player1->getHeight()));

  // create the computer player
  player2 = new Rectangle(PLAYER_WIDTH, PLAYER_HEIGHT); // computer
  player2->setRenderer(renderer);
  // place player 2 at the top-center of the screen
  player2->setPosition(((SCREEN_WIDTH - player1->getWidth()) / 2), 0);

  // create the ball
  ball = new Rectangle(BALL_RADIUS, BALL_RADIUS);
  ball->setRenderer(renderer);
  // place ball in the middle of the screen
  ball->setPosition(((SCREEN_WIDTH - ball->getWidth()) / 2), ((SCREEN_HEIGHT - ball->getHeight()) / 2)); 

  // current state of the game is IDLE
  currentGameState = IDLE;
}
 
void idleState() {
  /*
     Idle state: Just display a menu and do nothing until the start button is pressed;
     If the start button is pressed, go to the play state
  */

  // adjust and print font
  renderer->setFont(BigFont);
  renderer->setColor(VGA_GREEN);
  renderer->print("Welcome to", CENTER, 30);
  renderer->setColor(VGA_RED);
  renderer->print("PONG", CENTER, 50);

  // block until there is input from the button
  waitForInput(renderer, "Press start to play", CENTER, 180); 

  // reset scores
  playerScore = 0;
  compScore = 0;

  // go to play state
  renderer->clrScr();
  currentGameState = PLAY;
}

// this is used to control the displacement of the ball
int speedX = 1, speedY = 1; 
void playState() {
  /*
     This is the main state of the game where the user is playing pong against the AI;
     Once the score limit is reached, change the currentState to the gameOver state.
  */
  updateTilt();

  // check if the ball hits the left or right of the screen's borders
  if (ball->getX() <= 0 || ball->getX() + ball->getWidth() >= SCREEN_WIDTH) {
    speedX *= -1;
  } 
  
  //check if the ball hit the top of the screen
  if (ball->getY() <= 0) { 
    //player 1 has won this round; change state to score    
    playerScore++;
    renderer->clrScr();
    currentGameState = SCORE;
    return;
  
  //check if the ball hits the bottom of the screen
  } else if (ball->getY() + ball->getHeight() >= SCREEN_HEIGHT) {  
    compScore++; // increase AI score    
    renderer->clrScr();
    currentGameState = SCORE;
    return;
  } 

  // determine which way the computer should go to win
  switch (player2->directionTowards(ball)){
    case GO_LEFT: {
      player2->setPosition(player2->getX() - 1, player2->getY());
      break;
    } 
    case GO_RIGHT: {
      player2->setPosition(player2->getX() + 1, player2->getY());     
      break; 
    } 
    case STAY: { break; } // don't do anything  
  }

  // get the value from the master device
  int tilt = getTilt();
  if (player1->getX() + tilt > 0 && player1->getX() + player1->getWidth() + tilt < SCREEN_WIDTH) {
    // move player 1 based on this value
    player1->setPosition(player1->getX() + tilt, player1->getY());
  }

  // check if ball collides with players
  if (player1->collidesWith(ball) || player2->collidesWith(ball)){
    speedY *= -1;
  } 
  
  // move the ball
  ball->setPosition(ball->getX() + speedX, ball->getY() + speedY); 
}

void scoreState(){
  /* Displays the score;
     Compare the scores and check to see which one has won.
     Display the scores.
  */  

  // reset the position of the ball and the players, to their initial state
  ball->setPosition(((SCREEN_WIDTH - ball->getWidth()) / 2), ((SCREEN_HEIGHT - ball->getHeight()) / 2));
  player2->setPosition(((SCREEN_WIDTH - player1->getWidth()) / 2), 0);
  player1->setPosition(((SCREEN_WIDTH - player1->getWidth()) / 2), (SCREEN_HEIGHT - player1->getHeight()));

  // check if the score limit has been reached
  if(playerScore == SCORE_LIMIT || compScore == SCORE_LIMIT){      
    renderer->clrScr();
    currentGameState = GAMEOVER; 
    return;   
  }
  
  renderer->setColor(VGA_WHITE);
  renderer->setFont(SmallFont);
  
  String score  = "Score: " + (String) playerScore + ":" + (String) compScore;
  renderer->print(score, CENTER, 80);

  // wait for the start button to be pressed  
  waitForInput(renderer, "Press start to continue...", CENTER, 90);  
  
  renderer->clrScr();
  currentGameState = PLAY;  
} 

void gameOverState() {
  /* Either human or computer has won!
   * Display who has won;
   * Send the scores to the bluetooth master device
  */
  
   renderer->setFont(BigFont);
   
   renderer->setColor(VGA_WHITE);
   renderer->print("GAME OVER!",CENTER,50);

   // see whether computer or human has won
   renderer->setFont(SmallFont);
   if (playerScore == SCORE_LIMIT){
      renderer->print("YOU WIN", CENTER, 70);
   }
   else {
      renderer->print("YOU LOSE", CENTER, 70);
   }
   String score = (String) playerScore + ":" + (String) compScore;
   renderer->print(score, CENTER, 80);

   sendScore(playerScore, compScore);
   
   // wait for the start button to be pressed
   waitForInput(renderer, "Press start to reset...", CENTER, 90);   
     
   renderer->clrScr();
   currentGameState = IDLE; 
}
 
void loop() { 

  switch (currentGameState) {
  case IDLE: {
      idleState();
      break;
    }
  case PLAY: {
      playState();
      break;
    }
  case SCORE: {
      scoreState();
      break;
    }
  case GAMEOVER: {
      gameOverState();
      break;
    }
  }
  // delay the game between MINIMUM_GAME_DELAY and MAXIMUM_GAME_DELAY milliseconds based off the value of the GAME_FPS_POTENTIOMETER
  delay(MINIMUM_GAME_DELAY + (int) (analogRead(GAME_FPS_POTENTIOMETER) * ((MAXIMUM_GAME_DELAY - MINIMUM_GAME_DELAY) / 1023.0)));
}
