/*
color finalColor = color(3, 123, 74);
color initialColor = color(193, 222, 210);
*/

// Constants
int DELAY_TIME = 300;
int MIN_SIZE = 20;
int MAX_SIZE = 40;

// Variables
int animationIndex = 0;
dot[] dots = new dot[4];


void setup()
{
  // Initial Settings
  size(350, 300);
  background(255);
  noStroke();
  
  // Create objects
  dots[0] = new dot(0, 100, height/2);
  dots[1] = new dot(1, 150, height/2);
  dots[2] = new dot(2, 200, height/2);
  dots[3] = new dot(3, 250, height/2);

  // Start animation
  dots[0].animate = true;
  
}


void draw() {

  background(255);
  
  // Show all objects
  for (int i = 0; i < 4; i++) {
    dots[i].display();
  }
  
}


// Change animation to the next object
void updateAnimationIndex() {

  if (animationIndex < 3) {
    animationIndex++;
    dots[animationIndex].animate = true;
  }
  else {
    animationIndex = 0;
  }
  
}


// Dot object
class dot {
  
  // Variables
  int index;
  float posX, posY;
  float size = 20;
  float sizeInc = 1.5;
  boolean animate = false;


  // Constructor
  dot(int _index, float _posX, float _posY) { 
    index = _index;
    posX = _posX;
    posY = _posY;
  }
  
  
  // Display object in screen
  void display() {
    
    // Animate Object
    if (animate) {
      
      if (size > MAX_SIZE) {
        sizeInc *= -1; 
        updateAnimationIndex();
      }
        
      if (size < MIN_SIZE) {
        sizeInc *= -1;
        animate = false;
        
        if (index == 3 && animationIndex == 0) {
          delay(DELAY_TIME);
          dots[animationIndex].animate = true;
        }
      }
        
      size += sizeInc;
      posY -= sizeInc;
      
    }
    
    // Get Color
    float r = map(size, MAX_SIZE, MIN_SIZE, 3, 193);
    float g = map(size, MAX_SIZE, MIN_SIZE, 123, 222);
    float b = map(size, MAX_SIZE, MIN_SIZE, 74, 210);
    
    color newColor = color(r, g, b);
    
    fill(newColor); 
    
    // Display Circle
    circle(posX, posY, size); 
  
  }

}
