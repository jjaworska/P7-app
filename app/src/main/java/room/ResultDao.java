package room;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ResultDao {

    @Insert
    void insertResult(Result... results);

    @Query("SELECT * FROM result LIMIT 1")
    List<Result> getAnyResult();

    @Query("SELECT * FROM result WHERE (game_mode = :b) AND own_sets > 0 ORDER BY own_sets DESC, time LIMIT 7")
    List<Result> bestGames(boolean b);

    @Query("SELECT SUM(own_sets) FROM result")
    Integer getSetsCollected();

    @Query("SELECT SUM(time) / 60 FROM result")
    Integer getTimeSpentPlaying();

    /*  used when debugging  */
    @Query("DELETE FROM result")
    void clearResults();
}
