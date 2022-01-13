let angles = [30, 10, 45, 60, 38, 75, 67, 35];
let colors = [];
let corRandom = [];
let rlabel = 1.2;
let labelSize = 25;

function setup() {
    let myCanvas = createCanvas(600, 400);
    myCanvas.parent("myContainer");
    noStroke();
    background(100);
    console.log(angles[0]);

    set_pie_colors()
    /*
    for (let i = 0; i < angles.length; i++) {
        corRandom.push(color(random(255), random(255), random(255)));

    }*/
}

function draw() {

    pieChart(300, angles);
}

function set_pie_colors() {
    for (let i = 0; i < angles.length; i++) {
        let red = floor(random(10, 255));
        let green = floor(random(10, 255));
        let blue = floor(random(10, 255));
        colors.push([red, green, blue]);
    }
}

function pieChart(diameter, data) {
    let lastAngle = 0;

    for (let i = 0; i < data.length; i++) {

        fill(colors[i]);

        arc(
            width / 2,
            height / 2,
            diameter,
            diameter,
            lastAngle,
            lastAngle + radians(angles[i])
        );
        stroke(1);
        fill(255, 255, 255, 240);
        textSize(labelSize);
        let wText = textWidth(String(angles[i]));
        let hText = textAscent() - textDescent();
        console.log('wText: ', wText);
        console.log('hText: ', hText);
        console.log('textAscent: ', textAscent());
        console.log('textDescent: ', textDescent());
        // console.log(wText);
        text(angles[i],
            width / 2 + cos(lastAngle + radians(angles[i] / 2)) *
            diameter * (rlabel / 2) - wText / 2,
            height / 2 + sin(lastAngle + radians(angles[i] / 2)) *
            diameter * (rlabel / 2) + hText / 2);

        lastAngle += radians(angles[i]);


        //console.log(int( width / 2 + cos(lastAngle + radians(angles[i] / 2)) * diameter * (rlabel / 2) - wText/2));
        //console.log(int( height / 2 + sin(lastAngle + radians(angles[i] / 2)) * diameter * (rlabel / 2) + hText/2));
    }
}