package com.example.project.model;

import androidx.room.Embedded;
import androidx.room.Relation;

public class StudentWithGroup {
    @Embedded
    public Student student;

    @Relation(
            parentColumn = "groupId",
            entityColumn = "id"
    )
    public Group group;
}
