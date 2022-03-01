import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;

public class NTManager {
    public final static String TARGET_TYPE = "Ball";
    public static NTManager instance;

    // Vision
    public NetworkTableInstance ntinst = NetworkTableInstance.getDefault();
    public NetworkTable visionTab = ntinst.getTable(TARGET_TYPE + " Cam");
    public NetworkTableEntry xPos = visionTab.getEntry("X Position");
    public NetworkTableEntry yPos = visionTab.getEntry("Y Position");
    public NetworkTableEntry area = visionTab.getEntry("Area");
    public NetworkTableEntry minArea = visionTab.getEntry("Min Area");
    public NetworkTableEntry maxArea = visionTab.getEntry("Max Area");
    public NetworkTableEntry filtered = visionTab.getEntry("Show Filtered");

    public NetworkTableEntry rHigh = visionTab.getEntry("R High");
    public NetworkTableEntry gHigh = visionTab.getEntry("G High");
    public NetworkTableEntry bHigh = visionTab.getEntry("B High");
    public NetworkTableEntry rLow = visionTab.getEntry("R Low");
    public NetworkTableEntry gLow = visionTab.getEntry("G Low");
    public NetworkTableEntry bLow = visionTab.getEntry("B Low");
    

    public NTManager() {
        System.out.println("Connecting to Network Tables...");
        ntinst.startClientTeam(5148);
        //ntinst.startClient("10.0.0.136");
        ntinst.startDSClient();
    }

    public static NTManager getInstance() {
        if (instance == null)
            instance = new NTManager();
        return instance;
    }
}
