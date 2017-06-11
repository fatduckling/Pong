#include "point.h" 
#include "math.h" 
/* A basicimplementation of a point class  */
double Point::distance(Point* a, Point* b){
  // calculate the Euclidean distance between two points
  return sqrt(((b->x - a->x) * (b->x - a->x)) + ((b->y - a->y) * (b->y - a->y))); 
}

Point::Point(){} // empty constructor
Point::Point(int x1, int y1){
  x = x1;
  y = y1;  
}
