package com.example.p7;

public class Stopwatch {
    private long time = 0;
    private long compareTo = 0;

    public Stopwatch() {
        compareTo = System.currentTimeMillis();
    }
    public void update() {
        if (compareTo != 0) {
            long now = System.currentTimeMillis();
            time += now - compareTo;
            compareTo = now;
        }
    }
    public void pause() {
        update();
        compareTo = 0;
    }
    public void resume() {
        update();
        compareTo = System.currentTimeMillis();
    }
    public String getTime() {
        update();
        int secs = (int) (time / 1000) % 60;
        int mins = (int) (time / 60000) % 60;
        int hrs = (int) (time / 3600000);
        /* I assume no one would actually play the game for 10 hours */
        return String.format("%01d:%02d:%02d", hrs, mins, secs);
    }
    static public String stringFromSeconds(int seconds) {
        int secs = seconds % 60;
        int mins = (seconds / 60) % 60;
        int hrs = (seconds / 3600);
        return String.format("%01d:%02d:%02d", hrs, mins, secs);
    }
    public Integer getIntTime() {
        return (int) (time / 1000);
    }
}
