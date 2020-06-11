# PRCAndroid2DGameEngine
As the descriptive name indicates, this is a 2D game engine for Android. Its based on OpenGL ES 2.0. PRC refers to Project Rocket, a game on Google Play. This game engine was a part of Project Rocket before I decoupled it and made it a stand alone game engine. The reason why this game engine exists is because I didn't know game engines existed when making the game so I made my own one. This is also the reason why game engine is rather primitive only capable of drawing simple geometric single colored shapes (and collision detection for those shapes) and textures.
### How to use
Clone PRCAndroid2DGameEngine into your project and import it as a module. You can also make it a submodule of your git repos if you want.

Put a PRCSurfaceView in your activity on which all the game graphics will be displayed.

Then you extend ProcessingThread and pass an instance of it into PRCSurfaceView by calling initializeRenderer(processingThread: ProcessingThread).

You'll put your game loop inside your costume ProcessingThread.
