package bluetooth;

public interface Constants {

    // Message types sent from the BluetoothChatService Handler
    int MESSAGE_STATE_CHANGE = 1;
    int MESSAGE_READ = 2;
    int MESSAGE_WRITE = 3;
    int MESSAGE_DEVICE_NAME = 4;
    int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    String DEVICE_NAME = "device_name";
    String TOAST = "toast";

    // GAME LOGIC
    int GAME_SETUP = -1;
    int NEW_SET = -2;
    int PAUSE_GAME = -3;
    int INTRODUCTION = -4;
    int FINISH_GAME = -5;
    int END_OF_MESSAGE = -6;
}
