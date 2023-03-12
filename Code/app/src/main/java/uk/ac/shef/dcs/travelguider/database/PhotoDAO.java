package uk.ac.shef.dcs.travelguider.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Ignore;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface PhotoDAO {

    @Query("SELECT id as id, path as path, latitude as latitude," +
                        "longitude as longitude, date as date, title as title FROM photobean")
    public List<PhotoBean> queryAll();

    @Ignore
    @Query("SELECT id as id, path as path, latitude as latitude," +
            "longitude as longitude, date as date, title as title FROM photobean WHERE id= :id")
    VisitBean getRecordBeanByName(int id);

    @Insert
    void insertData(PhotoBean... photo);

    @Delete()
    void DeleteData(PhotoBean... photo);
}
