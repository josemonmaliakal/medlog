package com.queryb.medlog.ui.screens

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.queryb.medlog.data.LabResult
import com.queryb.medlog.ui.theme.*
import com.queryb.medlog.ui.viewmodel.LabViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEntryScreen(
    viewModel: LabViewModel,
    onSaved: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    // Form state
    val cal = Calendar.getInstance()
    var date by remember {
        mutableStateOf(
            "${cal.get(Calendar.YEAR)}-" +
                    "${(cal.get(Calendar.MONTH) + 1).toString().padStart(2,'0')}-" +
                    "${cal.get(Calendar.DAY_OF_MONTH).toString().padStart(2,'0')}"
        )
    }
    var glucose       by remember { mutableStateOf("") }
    var cholesterol   by remember { mutableStateOf("") }
    var hdl           by remember { mutableStateOf("") }
    var ldl           by remember { mutableStateOf("") }
    var triglycerides by remember { mutableStateOf("") }
    var hemoglobin    by remember { mutableStateOf("") }
    var pdfUri        by remember { mutableStateOf<Uri?>(null) }
    var errorMsg      by remember { mutableStateOf("") }
    var showSuccess   by remember { mutableStateOf(false) }

    // PDF picker
    val pdfLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                context.contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                pdfUri = uri
            }
        }
    }

    // Date picker dialog
    fun showDatePicker() {
        val c = Calendar.getInstance()
        DatePickerDialog(context, { _, y, m, d ->
            date = "$y-${(m + 1).toString().padStart(2, '0')}-${d.toString().padStart(2, '0')}"
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
    }

    fun save() {
        fun f(s: String) = s.trim().toFloatOrNull() ?: 0f
        val result = LabResult(
            date          = date,
            pdfUri        = pdfUri?.toString() ?: "",
            glucose       = f(glucose),
            cholesterol   = f(cholesterol),
            hdl           = f(hdl),
            ldl           = f(ldl),
            triglycerides = f(triglycerides),
            hemoglobin    = f(hemoglobin)
        )
        val hasValue = listOf(
            result.glucose, result.cholesterol, result.hdl,
            result.ldl, result.triglycerides, result.hemoglobin
        ).any { it > 0 }

        if (!hasValue) {
            errorMsg = "Please enter at least one test value"
            return
        }
        viewModel.insert(result)
        onSaved()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Lab Result", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back",
                            tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MedBlue,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = SurfaceWhite
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Date picker
            SectionLabel("Test Date")
            OutlinedButton(
                onClick = { showDatePicker() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.CalendarToday, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(date)
            }

            // PDF picker
            SectionLabel("Lab Report PDF (optional)")
            OutlinedButton(
                onClick = {
                    pdfLauncher.launch(
                        Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                            addCategory(Intent.CATEGORY_OPENABLE)
                            type = "application/pdf"
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (pdfUri != null) MedGreen else MedBlue
                )
            ) {
                Icon(Icons.Default.UploadFile, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (pdfUri != null) "✅ PDF Selected" else "Choose PDF")
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            SectionLabel("Test Values — leave blank if not tested")

            // Input fields
            TestField("Glucose (mg/dL)", "Normal: 70–99", glucose) { glucose = it }
            TestField("Total Cholesterol (mg/dL)", "Normal: < 200", cholesterol) { cholesterol = it }
            TestField("HDL Cholesterol (mg/dL)", "Normal: > 40", hdl) { hdl = it }
            TestField("LDL Cholesterol (mg/dL)", "Normal: < 100", ldl) { ldl = it }
            TestField("Triglycerides (mg/dL)", "Normal: < 150", triglycerides) { triglycerides = it }
            TestField("Hemoglobin (g/dL)", "Normal: 13.5–17.5", hemoglobin) { hemoglobin = it }

            // Error
            if (errorMsg.isNotEmpty()) {
                Text(errorMsg, color = ErrorRed, fontSize = 13.sp)
            }

            Spacer(Modifier.height(8.dp))

            // Save
            Button(
                onClick = { save() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MedBlue)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Save Result", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        color = TextMuted
    )
}

@Composable
private fun TestField(
    label: String,
    hint: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(hint, fontSize = 12.sp, color = TextMuted) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        shape = RoundedCornerShape(12.dp)
    )
}
