# PRCAndroid2DGameEngine
As the descriptive name indicates, this is a 2D game engine for Android. Its based on OpenGL ES 2.0. PRC refers to Project Rocket, a game on Google Play. This game engine was a part of Project Rocket before I decoupled it and made it a stand alone game engine. The reason why this game engine exists is because I didn't know game engines existed when making the game so I made my own one. This is also the reason why game engine is rather primitive only capable of drawing simple geometric single colored shapes (and collision detection for those shapes) and textures.
### How to use
Clone PRCAndroid2DGameEngine into your Android Studio (not sure if it'll work for other IDE) project and add it as a module. You can also make it a submodule of your git repos if you want.

Put a PRCSurfaceView in your activity on which all the game graphics will be displayed.

Extend ProcessingThread and override the abstract methods as their names suggest. You can access the property: drawData in your processingThread, you'll need to indirectly modify drawData to draw each frame in your PRCSurfaceView.

The only way you can(should) modify drawData is by passing it into a Shape class's subclass for single colored simple geometric shape, or the Image class for textures.

You'll specify the coordinate system by returning an FloatArray specifying the boundary of the coordinate system in getLeftRightBottomTopBoundaries().

Call \<yourPRCSurfaceView\>.initializeRenderer(\<yourProcessingThread\>) to initialize the SurfaceView.
