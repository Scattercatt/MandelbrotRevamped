# Mandelbrot Revamped
This is a complex plane fractal image generator designed to use 100% of your CPU's power, with the capability to generate highly detailed, ultra-high resolution fractal images quickly.

## UI and controls
Navigation of fractals relies on the use of the small finder window at the top left. The small resolution of this window is used to make navigation always quick even on highly intensive render settings (such as 4x detail and high iteration count). **Control this by using the arrow keys**. 

The panel on the right has a lot of important controls as well as render stats.

Once you're in a spot you want to observe, you can click the **Render Preview** button (or with Alt-E) on the bottom panel. This render a larger, more detailed image into the main window. Unlike the finder window, this window only renders when this action is performed.

To render a julia fractal, first render a preview into the top main window. Once completed, **Click** anywhere on the top window to automatically render the corresponding julia set of the point you clicked. Sometimes, you will want to change a palette, iterations, iterations-per-loop, or any other propety of the fractal without having to click on the exact spot again. To do this, click the **Julia Preview** button. 

Remember that the main fractal and julia fractal are seperate "cameras". You can change which one you are controlling with the "Control main" and "Control julia" radio buttons. 

If you want your images to have more detail, the radio buttons **1x 4x 16x** adjust this. 1x is the basic calculation method; just take every point in the window and iterate accordingly. 4x and 16x use subdividing and averaging, rendering a smoother, higher quality image. However, this comes at the cost of computation time. As the numbers imply, 4x is *4 times* and 16x is ***16 times*** as many pixels calulated, so it gets pretty slow pretty quick. 

## Render Stats

###### Iterations calculated (IC) 
The current amount of iterations calculated in the render. 

###### Pixels calculated (PC)
How many pixels have been completed. 

These are seperate variables because they do not always correlate directly. 

###### Average time per iteration (ATPI) 
The time in nanoseconds each iteration takes to calculate on average. The lower the variable is, the better. 

###### Iterations per second (IPS)
The amount of iterations on average calculated per second.

###### Predicted render iterations (PRI)
The amount of iteration the program predicts a full render will have to calculate.

###### Estimated render time (ERT)
The amount of time the program predicts a full render will take given the output render size.

## Speed 
High core count CPUs work best for this program, as it takes advantage of as many cores as it possibly can. With all threads running, calculating the Mandelbrot Set usually can go through 150M iterations per second.

## Render window
The render window displays all the information about a render whilst it is rendering.

###### The block of colors in the middle
Every time a fractal is rendered, it marks every column of the render as a "job" to be calculated. 
This grid of pixels represents complete, incomplete, active, and unassigned jobs for the render.
Gray = Unassigned job
Red = Incomplete job
Green = Complete job
White = Active job

This block is (In my opinion) a fun visualization of the threads of your CPU working on the individual pieces of the render.

## My info

Gavin Green
gavinr11213@gmail.com

https://github.com/Scattercatt/MandelbrotRevamped
 