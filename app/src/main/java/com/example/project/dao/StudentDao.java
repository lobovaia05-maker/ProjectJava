package com.example.project.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;
import com.example.project.model.Student;
import com.example.project.model.StudentWithGroup;
import java.util.List;

@Dao
public interface StudentDao {
    @Insert void insert(Student student);
    @Update void update(Student student);
    @Delete void delete(Student student);

    // 8 видов сортировки
    @Transaction @Query("SELECT * FROM students ORDER BY lastName ASC")
    List<StudentWithGroup> getAllLastNameAsc();
    @Transaction @Query("SELECT * FROM students ORDER BY lastName DESC")
    List<StudentWithGroup> getAllLastNameDesc();
    @Transaction @Query("SELECT * FROM students ORDER BY groupId ASC")
    List<StudentWithGroup> getAllGroupAscLastNameAsc();
    @Transaction @Query("SELECT * FROM students ORDER BY groupId DESC")
    List<StudentWithGroup> getAllGroupDescLastNameDesc();

    @Transaction @Query("SELECT * FROM students WHERE lastName LIKE :text || '%' ORDER BY lastName ASC")
    List<StudentWithGroup> searchByLastName(String text);

    @Query("SELECT COUNT(*) FROM students WHERE lastName = :last AND firstName = :first AND COALESCE(middleName, '') = COALESCE(:middle, '') AND id != :excludeId")
    int checkDuplicate(String last, String first, String middle, int excludeId);
}