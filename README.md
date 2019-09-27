# FTC Team #14473 Future - Image processing Framework

To simplify the confusing and often time-consuming code needed for Computer Vision, this library was made.   
It follows very simple rules:

- To be included into the framework, code must be reusable across all forseeable future seasons (it will work as long as there isn't any big change to FTC software)  

- The person using the framework will be able to manage what they need control over and NOTHING ELSE. This means that all setup and android interfaces are handled automagickally.

## Super Quick Start for Rookie Teams
I personally won't recommend doing this for the long run, but if you just need something to **work** then follow these steps:

1. Download this library as a .zip file
2. Unzip the library
3. Open Android Studio and go to File>new>existing project>**where you unzipped the library**
4. Download to your RC
5. Run <<Three systems>> opmode
6. Positioning data will be printed via telemetry, if you want to plug the data elsewhere, find the Telemetry calls in the file `Douduo` and match the labels from the DS telemetry log
	
## Proper Start for the Minority who care

1. Copy all the code from the `Original` folder into your teamcode. We might refactor into a module, but think this way is easier
2. Install opencv for android version 3.4.3 into your project
3. Fix dependencies by clicking the suggested links in your build window
4. If if dosent work, go to the `build.gradle` and `build.releases.grade` folders in the Android File View. This is where IntelliJ writes down for real what all the dependencies for each module are. All the import UI is just an illusion. Go fix __that__. The details are beyond the scope of this readme
5. Study this code:

		@TeleOp(name = "The Three <<Holy Systems>>", group = "Primordial Artifact")
		public class DuoDou extends LinearOpMode {
	    public void runOpMode() {

		telemetry.setAutoClear(true);

		ImageDetector detector = new ImageDetector(this, false);
		StoneDetector stone = new StoneDetector(detector, this, true);
		OpencvDetector foundation = new OpencvDetector(detector, this);
		IMU imu = new IMU(this);

		LocalizationManager m =new LocalizationManager();
		m.addLocalizer(detector);
		m.addLocalizer(imu);

		m.start();
		stone.start();
		detector.start();
		foundation.start();
		imu.start();

		while (!isStopRequested()) {
		    detector.printposition(detector.getPosition());

		    foundation.print(foundation.getObjects());

		    stone.print(stone.getObjects());

		    telemetry.update();

		    detector.printposition(m.getPosition());
		}

		// Disable Tracking when we are done;
		detector.stop();
		stone.stop();
		foundation.stop();
		m.stop();
		imu.stop();
	    }

	    public void delay(Long time) {
		long start = System.currentTimeMillis();

		while (System.currentTimeMillis() - start < time) {
		    //wait
		}
	    }
	}
	
## Using the Library

All classes that the user uses follow a simple guideline: a constructor with clear parameter requirements, a `start()` method to begin computing, a `stop()` method to stop computing, and one method with `get` at the beginning of its name. This returns whatever the class is used for.

For example:

	ImageDetector detector = new ImageDetector(this, false);
	detector.start();

Is used to get the Vuforia localizer running. It requires an opmode instance and a boolean for whether the RC should display its view.

To request data, simply do:

	detector.getPosition();

And, for this class, will return an orientation() object. You can figure this out.

Each class has an additional method that formats and sends its data output to Telemetry. ImageDetector has printPosition(orientation o) that will format and print the aforementioned orientation object

So you can do:
		
	orientation o = detector.getPosition();
	detector.printPosition(o);
	
Or: 

	detector.printPosition(detector.getPosition());
	
Everything else is the same, but the Stone detector will return `recognition` objects and the Cv detector will return `Point` objects. These are all well documented and each class has the method that will format and interpret it for you.

A note: the IMU class, as a non-absolute Localizer, will always return the difference in position since __the last time you called its getter method__. That means that if the robot spins more than 360 degrees between that time, you will have an unreliable rotational reading.

//TODO LocalizationManager
	


