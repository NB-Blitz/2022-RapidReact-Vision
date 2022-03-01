import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import edu.wpi.cscore.CvSink;
import edu.wpi.cscore.CvSource;
import edu.wpi.cscore.UsbCamera;
import edu.wpi.first.cameraserver.CameraServer;

public class CamManager {
    public static final int EXPOSURE = 50;
    public static final int WIDTH = 640;
    public static final int HEIGHT = 480;

    public NTManager nt = NTManager.getInstance();
    public UsbCamera camera;
    public CvSink inputStream;
    public CvSource outputStream;

    public CamManager() {
        System.out.println("Connecting to Camera...");

        // USB Camera
        camera = new UsbCamera("USB Camera", 0);
        camera.setResolution(WIDTH, HEIGHT);
        //camera.setExposureManual(EXPOSURE);

        // Input / Output Streams
        inputStream = new CvSink("cam_sink");
        inputStream.setSource(camera);
        CameraServer inst = CameraServer.getInstance();
        outputStream = inst.putVideo(NTManager.TARGET_TYPE + " Cam", 640, 480);

        // Network Tables
        nt.rHigh.setDouble(150);
        nt.gHigh.setDouble(255);
        nt.bHigh.setDouble(200);
        nt.rLow.setDouble(50);
        nt.gLow.setDouble(200);
        nt.bLow.setDouble(70);
        nt.minArea.setDouble(0);
        nt.maxArea.setDouble(WIDTH * HEIGHT);
        nt.filtered.setBoolean(false);

        // Loop
        while (true) {
            processFrame();
            if (!camera.isConnected())
                break;
        }

        // Close
        inputStream.close();
        outputStream.close();
        camera.close();
    }

    public void processFrame() {

        // Grab Frame
        Mat coloredFrame = new Mat();
        Mat filteredFrame = new Mat();
        inputStream.grabFrame(coloredFrame);
        if (coloredFrame.empty())
            return;

        // Network Tables
        double rHigh = nt.rHigh.getDouble(255);
        double gHigh = nt.gHigh.getDouble(255);
        double bHigh = nt.bHigh.getDouble(255);
        double rLow = nt.rLow.getDouble(0);
        double gLow = nt.gLow.getDouble(0);
        double bLow = nt.bLow.getDouble(0);
        double minArea = nt.minArea.getDouble(0);
        double maxArea = nt.maxArea.getDouble(WIDTH * HEIGHT);
        boolean isFiltered = nt.filtered.getBoolean(false);

        // Threshold
        Core.inRange(
            coloredFrame,
            new Scalar(rLow, gLow, bLow),
            new Scalar(rHigh, gHigh, bHigh),
            filteredFrame
        );

        Imgproc.erode(filteredFrame, filteredFrame, new Mat(), new Point(-1, -1), 2);

        // Finding Contours
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(
            filteredFrame,
            contours,
            new Mat(),
            Imgproc.RETR_LIST,
            Imgproc.CHAIN_APPROX_SIMPLE
        );

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
            nt.area.setDouble(areaBig);
            List<MatOfPoint> newContours = new ArrayList<>();
            newContours.add(contourBig);
            Imgproc.drawContours(
                coloredFrame,
                newContours,
                -1,
                new Scalar(138, 43, 226),
                3
            );

            // Find rectangle
            Rect rectangle = Imgproc.boundingRect(contourBig);
            Imgproc.rectangle(coloredFrame, rectangle.tl(), rectangle.br(), new Scalar(255, 0, 0), 1);

            // find center of rectangle
            double x = rectangle.x + (rectangle.width / 2);
            double y = rectangle.y + (rectangle.height / 2);

            // place dot in center of rectangle
            Imgproc.circle(
                coloredFrame,
                new Point(x, y),
                1,
                new Scalar(10, 10, 10),
                3
            );

            // does good thing
            nt.xPos.setDouble(2 * (x / WIDTH) - 1);
            nt.yPos.setDouble(2 * (y / HEIGHT) - 1);
        }

        // Export Frame
        outputStream.putFrame(isFiltered ? filteredFrame : coloredFrame);

        coloredFrame.release();
        filteredFrame.release();
    }
}
