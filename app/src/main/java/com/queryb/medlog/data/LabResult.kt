package com.queryb.medlog.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "lab_results")
data class LabResult(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: String = "",
    val date: String,
    // NEW — time column, also needs matching defaultValue
    @ColumnInfo(defaultValue = "")
    val time: String = "",
    // @ColumnInfo(defaultValue) tells Room to expect '' as SQL default
    // which matches what ALTER TABLE ... DEFAULT '' stored
    @ColumnInfo(defaultValue = "")
    val readingType: String = "",

    val pdfUri: String = "",
    val glucose: Float = 0f,
    val cholesterol: Float = 0f,
    val hdl: Float = 0f,
    val ldl: Float = 0f,
    val triglycerides: Float = 0f,
    val hemoglobin: Float = 0f
)

