import java.util.ArrayList;
import java.util.List;

import edu.wpi.cscore.CvSink;
import edu.wpi.cscore.CvSource;
import edu.wpi.cscore.UsbCamera;
import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.networktables.NetworkTableInstance;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

public final class Main {

    public static final int TEAM_NUMBER = 5148;
    public static final String TARGET_TYPE = "Ball";

    public static void main(String... args) {

        // Network Tables
        System.out.println("Connecting to Network Tables...");
        NetworkTableInstance ntinst = NetworkTableInstance.getDefault();
        ntinst.startClientTeam(TEAM_NUMBER);
        ntinst.startDSClient();

        // NT Entries
        NetworkTable table = ntinst.getTable(TARGET_TYPE + " Auto");
        NetworkTableEntry filterEntry = table.getEntry("Cam Filter");
        NetworkTableEntry xEntry = table.getEntry("X Value");
        NetworkTableEntry yEntry = table.getEntry("Y Value");
        NetworkTableEntry areaEntry = table.getEntry("Area");
        NetworkTableEntry maxAreaEntry = table.getEntry("Max Area");
        NetworkTableEntry minAreaEntry = table.getEntry("Min Area");
        NetworkTableEntry rHighEntry = table.getEntry("R High");
        NetworkTableEntry gHighEntry = table.getEntry("G High");
        NetworkTableEntry bHighEntry = table.getEntry("B High");
        NetworkTableEntry rLowEntry = table.getEntry("R Low");
        NetworkTableEntry gLowEntry = table.getEntry("G Low");
        NetworkTableEntry bLowEntry = table.getEntry("B Low");

        filterEntry.setBoolean(true);
        xEntry.setDouble(0);
        yEntry.setDouble(0);
        areaEntry.setDouble(0);
        maxAreaEntry.setDouble(20000);
        minAreaEntry.setDouble(300);

        // Red Ball
        /*
         * rHighEntry.setDouble(195);
         * gHighEntry.setDouble(80);
         * bHighEntry.setDouble(255);
         * rLowEntry.setDouble(40);
         * gLowEntry.setDouble(40);
         * bLowEntry.setDouble(160);
         */

        // Reflective Tape
        rHighEntry.setDouble(255);
        gHighEntry.setDouble(255);
        bHighEntry.setDouble(220);
        rLowEntry.setDouble(0);
        gLowEntry.setDouble(230);
        bLowEntry.setDouble(0);

        // Driver Station
        DriverStation fms = DriverStation.getInstance();

        // USB Camera
        System.out.println("Connecting to " + TARGET_TYPE + " Camera...");
        UsbCamera camera = new UsbCamera("USB Camera", 0);
        camera.setResolution(640, 480);
        camera.setExposureManual(30);
        CvSink inputStream = new CvSink("cam_sink");
        inputStream.setSource(camera);

        // Camera Server 1
        CameraServer inst = CameraServer.getInstance();
        CvSource source = inst.putVideo(TARGET_TYPE + "Cam", 640, 480);

        // Loop
        for (;;) {
            Mat frame = new Mat();
            inputStream.grabFrame(frame);
            if (frame.empty())
                continue;

            boolean isFiltered = filterEntry.getBoolean(true);

            if (isFiltered) {

                // Network Tables
                double minArea = minAreaEntry.getDouble(300);
                double maxArea = maxAreaEntry.getDouble(20000);

                double rHigh = rHighEntry.getDouble(255);
                double gHigh = gHighEntry.getDouble(255);
                double bHigh = bHighEntry.getDouble(255);
                double rLow = rLowEntry.getDouble(0);
                double gLow = gLowEntry.getDouble(0);
                double bLow = bLowEntry.getDouble(0);

                // Threshold
                Core.inRange(
                        frame,
                        new Scalar(rLow, gLow, bLow),
                        new Scalar(rHigh, gHigh, bHigh),
                        frame);

                // Finding Contours
                List<MatOfPoint> contours = new ArrayList<>();
                Imgproc.findContours(
                        frame,
                        contours,
                        new Mat(),
                        Imgproc.RETR_LIST,
                        Imgproc.CHAIN_APPROX_SIMPLE);

                if (contours.size() > 0) {

                    // Getting the Thicc-est Contour
                    double areaBig = 0;
                    MatOfPoint contourBig = contours.get(0);
                    for (int i = 0; i < contours.size(); i++) {
                        double area = Imgproc.contourArea(contours.get(i));
                        if (areaBig < area && area > minArea && area < maxArea) {
                            areaBig = area;
                            contourBig = contours.get(i);
                        }
                    }

                    // Draw the Thicc-est Contour
                    areaEntry.setDouble(areaBig);
                    List<MatOfPoint> newContours = new ArrayList<>();
                    newContours.add(contourBig);
                    Imgproc.drawContours(
                            frame,
                            newContours,
                            -1,
                            new Scalar(138, 43, 226),
                            3);
                    // find rectangle
                    Rect rectangle = Imgproc.boundingRect(contourBig);
                    Imgproc.rectangle(frame, rectangle.tl(), rectangle.br(), new Scalar(255, 0, 0), 1);

                    // find center of rectangle
                    double x = rectangle.x + (rectangle.width / 2);
                    double y = rectangle.y + (rectangle.height / 2);

                    // place dot in center of rectangle
                    Imgproc.circle(
                            frame,
                            new Point(x, y),
                            1,
                            new Scalar(10, 10, 10),
                            3);

                    // does good thing
                    xEntry.setDouble(x);
                    yEntry.setDouble(y);
                }
            }

            // Export Frame
            source.putFrame(frame);

        }
    }
}
// yes