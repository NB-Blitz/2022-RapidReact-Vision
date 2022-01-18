import java.util.ArrayList;
import java.util.List;

import edu.wpi.cscore.CvSink;
import edu.wpi.cscore.CvSource;
import edu.wpi.cscore.MjpegServer;
import edu.wpi.cscore.UsbCamera;
import edu.wpi.cscore.VideoSource;
import edu.wpi.cscore.VideoMode.PixelFormat;
import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;

import org.opencv.core.*; 
import org.opencv.imgproc.Imgproc;

public final class Main {

	public static final int TEAM_NUMBER = 5148;

	public static void main(String... args) {

		// Network Tables
		System.out.println("Setting up NetworkTables client for team " + TEAM_NUMBER);
		NetworkTableInstance ntinst = NetworkTableInstance.getDefault();
		ntinst.startClientTeam(TEAM_NUMBER);
		ntinst.startDSClient();
		NetworkTable table = ntinst.getTable("Autonomous");
		NetworkTableEntry rHighEntry = table.getEntry("R High");
		NetworkTableEntry gHighEntry = table.getEntry("G High");
		NetworkTableEntry bHighEntry = table.getEntry("B High");
		NetworkTableEntry rLowEntry = table.getEntry("R Low");
		NetworkTableEntry gLowEntry = table.getEntry("G Low");
		NetworkTableEntry bLowEntry = table.getEntry("B Low");
		rHighEntry.setDouble(255);
		gHighEntry.setDouble(255);
		bHighEntry.setDouble(255);
		rLowEntry.setDouble(0);
		gLowEntry.setDouble(0);
		bLowEntry.setDouble(0);


		// USB Camera
		UsbCamera camera = new UsbCamera("USB Camera", 0);
		CvSink inputStream = new CvSink("cam_sink");
		inputStream.setSource(camera);

		// Camera Server 1
		CameraServer inst = CameraServer.getInstance();
		CvSource source = inst.putVideo("Camera1", 640, 480);

		// Loop
		for (;;) {
			
			double rHigh = rHighEntry.getDouble(230);
			double gHigh = gHighEntry.getDouble(80);
			double bHigh = bHighEntry.getDouble(120);
			double rLow = rLowEntry.getDouble(70);
			double gLow = gLowEntry.getDouble(50);
			double bLow = bLowEntry.getDouble(10);

			Mat frame = new Mat(0,0,0);
			inputStream.grabFrame(frame);
			if (frame.empty())
				continue;
			Core.inRange(
				frame,
				new Scalar(rLow,gLow,bLow),
				new Scalar(rHigh,bHigh,gHigh),
				frame
			);
			List<MatOfPoint>contours = 
			new ArrayList<>();
		
			/*
			Imgproc.findContours(
				frame,
				contours,
				new Mat(),
				Imgproc.RETR_LIST,
				Imgproc.CHAIN_APPROX_SIMPLE
			);
			Imgproc.drawContours(
				frame,
				contours,
				-1,
				new Scalar(255,255,255),
				3
			);*/
			source.putFrame(frame);
			
		}
	}
}
//yes
