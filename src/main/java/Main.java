import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;

import edu.wpi.cscore.CvSink;
import edu.wpi.cscore.CvSource;
import edu.wpi.cscore.MjpegServer;
import edu.wpi.cscore.UsbCamera;
import edu.wpi.cscore.VideoSource;
import edu.wpi.cscore.VideoMode.PixelFormat;
import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.networktables.NetworkTableInstance;

public final class Main {

	public static final int TEAM_NUMBER = 5148;

	public static void main(String... args) {

		// Network Tables
		System.out.println("Setting up NetworkTables client for team " + TEAM_NUMBER);
		NetworkTableInstance ntinst = NetworkTableInstance.getDefault();
		ntinst.startClientTeam(TEAM_NUMBER);
		ntinst.startDSClient();

		// USB Camera
		UsbCamera camera = new UsbCamera("USB Camera", 0);
		CvSink inputStream = new CvSink("cam_sink");
		inputStream.setSource(camera);

		// Camera Server 1
		CameraServer inst = CameraServer.getInstance();
		MjpegServer mjpegServer1 = inst.startAutomaticCapture(camera);
		camera.setConnectionStrategy(VideoSource.ConnectionStrategy.kKeepOpen);

		// Camera Server 2
		CvSource outputStream = new CvSource("Processed", PixelFormat.kMJPEG, 640, 480, 30);
		MjpegServer mjpegServer2 = new MjpegServer("serve_process", 1182);
		mjpegServer2.setSource(outputStream);

		// Loop
		for (;;) {
			Mat frame = new Mat();
			inputStream.grabFrame(frame);

			// do processing here

			outputStream.putFrame(frame);
		}
	}
}
