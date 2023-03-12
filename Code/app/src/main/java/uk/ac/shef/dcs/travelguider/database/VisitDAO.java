package uk.ac.shef.dcs.travelguider.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface VisitDAO {

    @Query("SELECT duration as duration, id as id, title as title, date as date, path as path FROM visitbean")
    public List<VisitBean> queryAll();

    @Insert
    void insertData(VisitBean... visit);

    @Delete()
    void DeleteData(VisitBean...  visit);
}
