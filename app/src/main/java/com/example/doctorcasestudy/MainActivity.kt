package com.example.doctorcasestudy

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.doctorcasestudy.model.AppViewModelFactory
import com.example.doctorcasestudy.model.PatientData
import com.example.doctorcasestudy.service.MainActivityViewModel
import com.example.doctorcasestudy.ui.theme.DoctorCaseStudyTheme
import com.example.doctorcasestudy.ui.theme.Gray
import com.example.doctorcasestudy.ui.theme.LightBlue
import com.example.doctorcasestudy.ui.theme.LightGray
import com.example.doctorcasestudy.ui.theme.White

class MainActivity : ComponentActivity() {

    private var inputValue: TextFieldState? = null
    private lateinit var submittedPatient: MutableState<Boolean>
    private lateinit var isCaseStudy1: MutableState<Boolean>
    private var docAWaitingTime: MutableState<PatientData?>? = null
    private var docBWaitingTime: MutableState<PatientData?>? = null
    private var currentTime: MutableState<Long>? = null

    private val viewModel: MainActivityViewModel by lazy {
        AppViewModelFactory(this@MainActivity).create(MainActivityViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            inputValue = remember { TextFieldState() }
            docAWaitingTime = remember {
                mutableStateOf(null)
            }
            docBWaitingTime = remember {
                mutableStateOf(null)
            }
            currentTime = remember {
                mutableStateOf(0L)
            }

            submittedPatient = remember {
                mutableStateOf(false)
            }

            isCaseStudy1 = remember {
                mutableStateOf(true)
            }

            DoctorCaseStudyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainCompose()
                }
            }
        }

        viewModel.getCurrentDocAPatient().observe(this) {
            docAWaitingTime?.value = it
        }

        viewModel.getCurrentTime().observe(this) {
            currentTime?.value = it
        }

        viewModel.getCurrentDocBPatient().observe(this) {
            docBWaitingTime?.value = it
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainCompose() {
        Column(
            modifier = Modifier
                .fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {

            val DocATitle = "Doctor A is consulting ${docAWaitingTime?.value?.patientName}"
            val DocBTitle = "Doctor B is consulting ${docBWaitingTime?.value?.patientName}"
            val remainingTime = "Waiting Time: ${timeConversion(currentTime?.value)}"

            Text(
                text = if (docAWaitingTime?.value != null) DocATitle else "Doctor A yet to start the .consulting",
                modifier = Modifier.padding(10.dp)
            )
            if (!isCaseStudy1.value) {
                Text(
                    text = if (docBWaitingTime?.value != null) DocBTitle else "Doctor B yet to start the .consulting",
                    modifier = Modifier.padding(10.dp)
                )
            }
            Text(
                text = if (submittedPatient.value) remainingTime else "Waiting Time: --",
                modifier = Modifier.padding(10.dp)
            )
            TextField(
                value = inputValue?.textInput ?: "",
                onValueChange = { inputValue?.textInput = it },
                placeholder = { Text("Enter patient name", color = Gray) },
                colors = TextFieldDefaults.textFieldColors(
                    textColor = Color.Black,
                    containerColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .drawWithContent {
                        drawContent()
                        val strokeWidth = 1.dp.value * density
                        val y = size.height - strokeWidth / 2
                        drawLine(
                            LightBlue,
                            Offset((6.dp).toPx(), y),
                            Offset(size.width - 6.dp.toPx(), y)
                        )
                    },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )

            TextButton(
                onClick = {
                    submittedPatient.value = true
                    Toast.makeText(this@MainActivity, "Added patient...", Toast.LENGTH_LONG)
                        .show()
                    viewModel.addPatient(inputValue?.textInput.toString())
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = LightBlue
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .height(50.dp),
                shape = RectangleShape,
            ) {
                Text("Submit", color = White)
            }

            Spinner(
                list = arrayListOf("Case Study 1", "Case Study 2"),
                preselected = "Case Study 1"
            ) {
                isCaseStudy1.value = it == "Case Study 1"
                viewModel.isCaseStudy1(it == "Case Study 1")
            }

            if (isCaseStudy1.value) {
                TextButton(
                    onClick = {
                        Toast.makeText(
                            this@MainActivity,
                            "Doctor started consulting...",
                            Toast.LENGTH_LONG
                        )
                            .show()
                        viewModel.startQueue()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LightBlue
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .height(50.dp),
                    shape = RectangleShape,
                ) {
                    Text("Start Consultation", color = White)
                }
            } else {

                TextButton(
                    onClick = {
                        Toast.makeText(
                            this@MainActivity,
                            "Doctor A and B started consulting...",
                            Toast.LENGTH_LONG
                        )
                            .show()
                        viewModel.case2StartQueue()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LightBlue
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .height(50.dp),
                    shape = RectangleShape,
                ) {
                    Text("Doctor A and B Start Consultation", color = White)
                }

                /*TextButton(
                    onClick = {
                        Toast.makeText(
                            this@MainActivity,
                            "Doctor B started consulting...",
                            Toast.LENGTH_LONG
                        )
                            .show()
                        viewModel.startDocBQueue()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LightBlue
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .height(50.dp),
                    shape = RectangleShape,
                ) {
                    Text("Doctor B Start", color = White)
                }*/
            }
        }
    }

    private fun timeConversion(time: Long?): String {
        if (time != null && time != 0L) {
            val minutes = time / 1000 / 60
            val seconds = time / 1000 % 60
            return "$minutes:$seconds"
        }
        return "--"
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Spinner(
        list: ArrayList<String>,
        preselected: String,
        onSelectionChanged: (selection: String) -> Unit
    ) {

        var selected by remember { mutableStateOf(preselected) }
        var expanded by remember { mutableStateOf(false) }

        Box {
            Column() {
                OutlinedTextField(
                    value = (selected),
                    onValueChange = { },
                    label = { Text(text = "Case study type", color = Color.Green) },
                    modifier = Modifier
                        .width(200.dp)
                        .padding(16.dp)
                        .testTag("OutlineTextDropDown"),
                    trailingIcon = { Icon(Icons.Outlined.ArrowDropDown, null) },
                    readOnly = true,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        unfocusedBorderColor = Color.Blue,
                        textColor = Color.Black,
                        containerColor = White
                    )
                )
                DropdownMenu(
                    modifier = Modifier.width(200.dp),
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    list.forEach { entry ->
                        DropdownMenuItem(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                selected = entry
                                expanded = false
                                onSelectionChanged(entry)
                            },
                            text = {
                                Text(
                                    text = (entry),
                                    color = Color.Green,
                                    modifier = Modifier
                                        .wrapContentWidth()
                                        .align(Alignment.Start)
                                )
                            }
                        )
                    }
                }
            }
            Spacer(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Transparent)
                    .padding(10.dp)
                    .clickable(
                        onClick = { expanded = !expanded }
                    )
            )
        }
    }

}