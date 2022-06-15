# Mandelbrot Revamped
This is a complex plane fractal image generator designed to use 100% of your CPU's power, with the capability to generate highly detailed, ultra-high resolution fractal images quickly.

## WARNING!
CPU cooling is vital when using this program. Heat damage to your CPU is possible if your computer lacks decent CPU cooling. Use at your own risk and monitor those temps! 

## UI and controls
Navigation of fractals relies on the use of the small finder window at the top left. The small resolution of this window is used to make navigation always quick even on highly intensive render settings (such as 4x detail and high iteration count). **Control this by using the arrow keys**. 

Buttons on the right can be used to change up different things.

Once you're in a spot you want to observe, you can click the **Render Preview** button (or with Alt-E) on the bottom panel. This render a larger, more detailed image into the main window. Unlike the finder window, this window only renders when this action is performed.

To render a julia fractal, first render a preview into the top main window. Once completed, **Click** anywhere on the top window to automatically render the corresponding julia set of the point you clicked. Sometimes, you will want to change a palette, iterations, color division mark, or any other propety of the fractal without having to click on the exact spot again. To do this, click the **Julia Preview** button. 

Remember that the main fractal and julia fractal are seperate "cameras". You can change which one you are controlling with the "Control main" and "Control julia" radio buttons. 

If you want your images to have more detail, the radio buttons **1x 4x 16x** adjust this. 1x is the basic calculation method; just take every point in the window and iterate accordingly. 4x and 16x use subdividing and averaging, rendering a sharper, higher quality image. However, this comes at the cost of computation time. As the numbers imply, 4x is *4 times* and 16x is ***16 times*** as many pixels calulated, so it gets pretty slow pretty quick. 


## Speed 
High core count CPUs work best for this program, as it takes advantage of as many cores as it possibly can. On a single thread, it can calculate ~59 iterations per microsecond (On the Mandelbrot Set). The program measures this stat as average time per iteration (ATPI). This is measured in nanoseconds, and hangs around 16-17 for the Mandelbrot set. Other more complex fractals such as the burning ship, given a sample size of 1.1 trillion, hangs around 31 ATPI. This of course is per thread. For example, one thread calculates at the aformentioned speed of 16.5 ATPI. However, on a Ryzen 3900XT (The CPU I test this program on), 24 of these Threads are running during render, so it is 24x the speed; 1416 iterations per microsecond, or 0.687 ATPI. 

## Render window
The render window displays a heap of information relating to the current ongoing render. It shows render progress, elapsed time, and a couple other variables. 

###### Iterations calculated (IC) 
The current amount of iterations calculated in the render. 

###### Pixels calculated (PC)
How many pixels have been completed. 

These are seperate variables because they do not always correlate directly. 

###### Average time per iteration (ATPI) 
The time in nanoseconds each iteration takes to calculate on average. The lower the variable is, the better. 

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
