Wailord
=======
This project, code named 'Wailord' is an augmented reality based android app. An actual name is still pending.
The goal is to be able to generate a 3D surface representing what the user drew on a map.

More precisions
-------
The application actually generates a mountain view from a picture of level lines drawn by the user. You actually have to take out a real sheet of paper and draw level lines on it. They don't have to be perfect circle, but the shapes have to be closed, or the 3D generation won't work.

How does it work ?
-------
The program consists in several steps, several of them being performed by different external modules and SDKs.

- Take a photo : first of all, the handroid camera module takes a photo of the drawing.
- The Analyse : the program runs an algorithm that will binarize the picture into stick black and white values, and detects contours in it, and then ranks them according to which contains which. A second algorithm will then smooth the points in between. Note : the original level lines detection and ranking was originally achieved thanks to OpenCV, but we then switched to algorithms of our own creation.
- 3D Surface generation : the mountain is generated based on the gigantic values table the analyzed picture offers us, using OpenGL. We added colors depending on the altitude of a point, to make it look like a mountain with snow on top :)
- Tracking : the generated 3D surface is placed on the screen, at the exact position where the drawing was supposed to be. That's augmented reality. To create this effect, we used the quite impressive Vuforia SDK.

TODOES
-------
Several things to do left, that we will or will not do, depending on if we have or don't have time :
- Clean the code. It's so dirty. Unused/deprecated methods everywhere.
- fix memory leaks
- Fix memory related problems : there is, for example, at the moment the picture is taken, a huge stack of nested methods, that + Vuforia running, + generating OpenGL stuff, some Android smartphones get OutOfMemoryError exceptions, because their VMs can't handle it. So we have to work more in async.
- Improve smoothing and display
- Add textures
- Add the possibility to zoom into the generated surface
Seen a bug ? Hey, you'd be really nice to report it to us and we'll add it to this list, and maybe never work on it. But again, maybe we will ;)

External Links
-------
As said, we used other developer's work to build our app, and we thank them since without them, there would be no project at all.
- Thanks to Vuforia for the amazing tracking (and moaar !) SDK : https://developer.vuforia.com/
- Thanks to OpenCV for the Computer Vision that we didn't use in the end. But still : http://opencv.org/
- Thanks to OpenClassrooms, french MOOC website : http://fr.openclassrooms.com/
Check them out ! :)
