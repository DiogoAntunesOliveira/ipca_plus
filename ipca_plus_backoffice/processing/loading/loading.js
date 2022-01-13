/*
color finalColor = color(3, 123, 74);
color initialColor = color(193, 222, 210);
*/

// Constants
let DELAY_TIME = 300;
let MIN_SIZE = 20;
let MAX_SIZE = 40;

let size = 20;
let sizeInc = 1.5;
let animate = false;
let posX = 0;
let posY = 0;

// Dot object
class dot {

  // Constructor
  constructor(_index, _posX, _posY) { 
    this.index = _index;
    this.posX = _posX;
    this.posY = _posY;
    this.size = 20;
    this.sizeInc = 1.5;
    this.animate = false;
  }
  
  
  // Display object in screen
  display() {
    
    // Animate Object
    if (this.animate) {
      
      if (this.size > MAX_SIZE) {
        this.sizeInc *= -1; 
        updateAnimationIndex();
      }
        
      if (this.size < MIN_SIZE) {
        this.sizeInc *= -1;
        this.animate = false;
        
        if (this.index == 3 && animationIndex == 0) {

          setTimeout(function(){
              dots[animationIndex].animate = true;
        }, 300);
      
        }
      }
        
      this.size += this.sizeInc;
      this.posY -= this.sizeInc;
      
    }
    
    // Get Color
    let r = map(this.size, MAX_SIZE, MIN_SIZE, 3, 193);
    let g = map(this.size, MAX_SIZE, MIN_SIZE, 123, 222);
    let b = map(this.size, MAX_SIZE, MIN_SIZE, 74, 210);
    
    let newColor = color(r, g, b);
    
    fill(newColor); 

    // Display Circle
    circle(this.posX, this.posY, this.size); 
  
  }

}


// Variables
let animationIndex = 0;
let dots = [new dot(0, 100, 150), new dot(1, 150, 150), new dot(2, 200, 150), new dot(3, 250, 150)]


function setup()
{
  // Initial Settings
  let myCanvas = createCanvas(350, 300);
  myCanvas.parent('myContainer');
  

  background(255);
  noStroke();

  // Start animation
  dots[0].animate = true;
  
}


function draw() {

  background(255);
  
  // Show all objects
  for (let i = 0; i < 4; i++) {
    dots[i].display();
  }
  
}


// Change animation to the next object
function updateAnimationIndex() {

  if (animationIndex < 3) {
    animationIndex++;
    dots[animationIndex].animate = true;
  }
  else {
    animationIndex = 0;
  }
  
}

