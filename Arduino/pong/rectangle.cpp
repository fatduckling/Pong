#include "rectangle.h"

/* CONSTRUCTOR: set the width, height and colour variable. */
Rectangle::Rectangle(int width, int height, int colour): m_width(width), m_height(height), m_colour(colour) {
  m_renderer = NULL;
  m_previousX = 0;
  m_previousY = 0;
}

/* Create a new rectangle at position (m_width, m_height); 
  For the old rectangle at position (m_previousX, m_previousY), if any point in this rectangle do not intersect with
  new rectangle, then colour it black.

  The other approach would be to paint the old rectangle black, and draw a new rectangle. This method will not work because
  the rectangle's creation and deletion will cause it to flicker, and hence, is unstable.
  Therefore, only colour the pixels black for those that are not part of the new rectangle now
*/
void Rectangle::update() {
  if (m_renderer != NULL) {
    // draw the new rectangle
    m_renderer->setColor(this->getColour());
    m_renderer->fillRect(this->getX(), this->getY(), this->getX() + this->getWidth() - 1, this->getY() + this->getHeight() - 1);

    // loop through every point in the prevoius rectangle and paint it black it doesn't intersect with the new rectangle
    m_renderer->setColor(VGA_BLACK);
    for (int y = m_previousY; y < m_previousY + m_height; y++) {
      for (int x = m_previousX; x < m_previousX + m_width; x++) {
        if (!intersect(x, y)) {
          m_renderer->drawPixel(x, y);
        }
      }
    }
  }
}

// returns true if "this" Rectangle intersects another Rectangle
boolean Rectangle::collidesWith(Rectangle* anotherObject) {
  return !(this->getX() > anotherObject->getX() + anotherObject->getWidth() || this->getX() + this->getWidth() < anotherObject->getX() ||
    this->getY() > anotherObject->getY() + anotherObject->getHeight() || this->getY() + this->getHeight() < anotherObject->getY());
} 

// moves the rectangle to a new position (x, y)
void Rectangle::setPosition(int x, int y) {
  this->setX(x);
  this->setY(y);
  this->update();
}

// attaches an UTFT renderer
void Rectangle::setRenderer(UTFT* renderer) {
  m_renderer = renderer;
}

/* stores the prevoius X coordinate in a variable and updates the current coordinate */
void Rectangle::setX(int x) {
  m_previousX = m_x;
  m_x = x;
}

/* stores the prevoius Y coordinate in a variable and updates the current coordinate */
void Rectangle::setY(int y) {
  m_previousY = m_y;
  m_y = y;
}

// changes the colour of the rectangle
void Rectangle::setColour(int colour) {
  m_colour = colour;
  update();
}

// returns true if the point (px, py) is inside this rectangle
boolean Rectangle::intersect(int px, int py) {
  return px >= m_x && px < m_x + m_width && py >= m_y && py < m_y + m_height;
}


// gets the center of the rectangle and calculates the distance to another rectangle's center
double Rectangle::distanceCenteredDistance(Rectangle* anotherRectangle) {
  // get "this" Rectangle's center position
  Point* z = this->getCenteredPosition();
  // get "anotherRectangle"'s center position
  Point* y = anotherRectangle->getCenteredPosition();

  // calculate the distance between the two points
  double d = Point::distance(z, y);

  // delete the points (free some memory)
  delete(z);
  delete(y);

  return d;
}

/*
    This method will generate three nodes from the initial position of this rectangle.
    The first node will be the distance between this rectangle and 'anotherRectangle' object, if this rectangle were to go left
    The second node will be the distance between this rectangle if it didn't move
    The third node will be the distance between this rectangle and 'anotherRectangle' object, if this rectangle were to go right;

    Find the smallest distance and return the direction based off that
*/

Directions Rectangle::directionTowards(Rectangle* ball) {
 
  // if this rectangle can't see the ball, return the STAY direction
  if (ball->getY() > getPotVal()) {
    // if this rectangle can't see the ball, return the STAY direction
    this->setColour(VGA_RED); // change colour to red
    return STAY;
  }
  
  this->setColour(VGA_WHITE);

  int x = m_x;
  // caluclate the distance to the ball if the rectangle were not to move
  double stop = distanceCenteredDistance(ball);
  // calculate the distance to the ball if the rectangle were to go left
  double left;
  if (m_x > 0) {
    // move left one place and calculate the distance
    m_x -= 1;
    left = distanceCenteredDistance(ball);
  } else {
    // this rectangle is at the LEFT MOST position on the screen - you can't go any more left
    left = INFINITY; // return a big number
  }
  
  // calcuate the distance to the ball if you were to the right
  double right;
  if (m_x + m_width < SCREEN_WIDTH) {
    // move right one place and calculate the distance
    m_x = x + 1;
    right = distanceCenteredDistance(ball);
  } else {
    // this rectangle is at the RIGHT MOST position on the screen - you can't go any more right
    right = INFINITY;
  }

  // determine the minimum of the three points 
  double tmp;
  Directions go;
  
  if (left < stop) {
    tmp = left;
    go = GO_LEFT;
  } else {
    tmp = stop;
    go = STAY;
  }
  if (right < tmp) {
    go = GO_RIGHT;
  }
  m_x = x;
  return go;
}

// returns the center point of "this" Rectangle
Point* Rectangle::getCenteredPosition() {
  Point* tmp = new Point();
  tmp->x = m_x + (m_width / 2);
  tmp->y = m_y + (m_height / 2);
  return tmp;
}

// getter methods
int Rectangle::getX() {
  return m_x;
}
int Rectangle::getY() {
  return m_y;
}
int Rectangle::getWidth() {
  return m_width;
}
int Rectangle::getHeight() {
  return m_height;
}
int Rectangle::getColour() {
  return m_colour;
}
