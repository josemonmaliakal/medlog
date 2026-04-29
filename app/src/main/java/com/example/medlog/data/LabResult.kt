package com.queryb.medlog.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lab_results")
data class LabResult(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: String = "",
    val date: String,           // Format: "2024-01-15"
    val pdfUri: String = "",    // URI of the PDF file
    val glucose: Float = 0f,        // mg/dL
    val cholesterol: Float = 0f,    // mg/dL
    val hdl: Float = 0f,            // mg/dL
    val ldl: Float = 0f,            // mg/dL
    val triglycerides: Float = 0f,  // mg/dL
    val hemoglobin: Float = 0f      // g/dL
)