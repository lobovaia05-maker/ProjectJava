package com.example.project.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.example.project.model.Group;
import java.util.List;

@Dao
public interface GroupDao {
    @Insert
    long insert(Group group);

    @Update
    void update(Group group);

    @Delete
    void delete(Group group);

    @Query("SELECT * FROM groups ORDER BY number ASC")
    List<Group> getAllGroupsAsc();

    @Query("SELECT * FROM groups WHERE id = :id")
    Group getGroupById(int id);

    @Query("SELECT COUNT(*) FROM students WHERE groupId = :groupId")
    int getStudentCount(int groupId);

    @Query("SELECT COUNT(*) FROM groups WHERE number = :number AND id != :excludeId")
    int checkDuplicate(String number, int excludeId);
}