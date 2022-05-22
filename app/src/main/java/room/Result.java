package room;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity
public class Result {

    @PrimaryKey
    @ColumnInfo(name = "date")
    public Date date;

    @ColumnInfo(name = "game_mode")
    public Boolean gameMode;

    @ColumnInfo(name="sets")
    public Integer setsInTotal;

    @ColumnInfo(name="own_sets")
    public Integer setsByYourself;

    @ColumnInfo(name="time")
    public Integer timeInSeconds;

    public Result(Date date, Boolean gameMode, Integer setsInTotal, Integer setsByYourself, Integer timeInSeconds) {
        this.date = date;
        this.gameMode = gameMode;
        this.setsInTotal = setsInTotal;
        this.setsByYourself = setsByYourself;
        this.timeInSeconds = timeInSeconds;
    }

    @Override
    public String toString() {
        return "Result{" +
                "date=" + date +
                ", gameMode=" + gameMode +
                ", setsInTotal=" + setsInTotal +
                ", setsByYourself=" + setsByYourself +
                ", timeInSeconds=" + timeInSeconds +
                '}';
    }
}
