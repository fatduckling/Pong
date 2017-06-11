#ifndef POINT_H
#define POINT_H

class Point {

public:  
  // returns the euclidean distance between two points
  static double distance(Point* a, Point* b);
    
  Point(int x, int y);
  Point();
  int x, y;
};


#endif
