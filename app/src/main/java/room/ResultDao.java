package room;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ResultDao {

    @Query("SELECT * FROM result")
    List<Result> getAllResults();

    @Insert
    void insertResult(Result... results);

    @Query("SELECT * FROM result WHERE (game_mode = 1) AND own_sets > 0 ORDER BY (time / own_sets) LIMIT 5")
    List<Result> bestP7Games();

    @Query("SELECT SUM(own_sets) FROM result")
    Integer getSetsCollected();

    @Query("SELECT SUM(time) / 60 FROM result")
    Integer getTimeSpentPlaying();

    @Query("SELECT * FROM result WHERE (game_mode = 0) AND own_sets > 0 ORDER BY (time / own_sets) LIMIT 5")
    List<Result> bestP6Games();

    @Delete
    void delete(Result result);

    @Query("DELETE FROM result")
    void clearResults();
}
