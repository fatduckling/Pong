#ifndef RECTANGLE_H
#define RECTANGLE_H

#ifndef UTFT_h
// make to not include this library more than once
	#include <memorysaver.h>
	#include <UTFT.h>
#endif

#include "utilities.h" 
#include "constants.h"
#include "point.h"

enum Directions { 
  GO_LEFT, STAY, GO_RIGHT
};

class Rectangle {
  
  public:
    /* create a new rectangle of width "width" and height "height"
    OPTIONAL: specify its colour */
    Rectangle(int width, int height, int colour = 0xFFFFFF);
    
    // will re-draw the rectangle to its new position
    void update(); 
    
    // returns true if "this" rectangle intersects another rectangle
    boolean collidesWith(Rectangle * anotherObject);

    // returns true if "this" rectangle contains the point (px, py)
    boolean intersect(int px, int py);

    // moves the rectangle to a new position
    void setPosition(int x, int y);

    // attaches an UTFT renderer to enable drawing
    void setRenderer(UTFT* renderer);

    // only change X position; You'll also need to call update() to apply changes
    void setX(int x);

    // only change Y position; You'll also need to call update() to apply changes
    void setY(int y);

    // change colour of rectangle
    void setColour(int colour);

    /*
      If this Rectangle is blinded by the potentiometer value, it will return STAY;
      If this rectangle's X position is the same as the ball's X position, will also return STAY;
      Otherwise, this will return GO_LEFT or GO_RIGHT depending on the position of the this and the ball
    */ 
    Directions directionTowards(Rectangle * ball);

    /* Returns the center position of the rectangle */
    Point * getCenteredPosition();

    // returns the distance between "this" Rectangle and another Rectagle from its center points
    double distanceCenteredDistance(Rectangle * anotherRectangle);

    // getter methods
    int getX();
    int getY();
    int getWidth();
    int getHeight();
    int getColour();
    
  private:
    int m_x, // xPosition 
    m_y, // yPosition
    m_width, // height
    m_height, // width
    m_colour, // colour
    m_previousX, m_previousY; // previous x and y values (before update is called();

    UTFT * m_renderer; // renderer
};

#endif
