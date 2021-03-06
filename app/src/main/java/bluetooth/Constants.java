package bluetooth;

public interface Constants {

    // Message types sent from the BluetoothService Handler
    int MESSAGE_STATE_CHANGE = 1;
    int MESSAGE_READ = 2;
    int MESSAGE_WRITE = 3;
    int MESSAGE_DEVICE_NAME = 4;
    int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothService Handler
    String DEVICE_NAME = "device_name";
    String TOAST = "toast";

    // GAME LOGIC
    int GAME_SETUP = -1;
    int NEW_SET = -2;
    int INTRODUCTION = -3;
    int END_OF_MESSAGE = -4;
    int PAUSE_GAME = -5;
    int STOP_GAME = -6;
    int REMATCH = -7;
}
