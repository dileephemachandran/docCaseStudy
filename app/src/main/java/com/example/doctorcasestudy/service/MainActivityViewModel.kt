package com.example.doctorcasestudy.service

import android.app.Activity
import android.os.CountDownTimer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.doctorcasestudy.model.DoctorData
import com.example.doctorcasestudy.model.PatientData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.LinkedList


class MainActivityViewModel(activity: Activity) : AndroidViewModel(activity.application) {
    private var docATimerCountDownTimer: CountDownTimer? = null
    private var docBTimerCountDownTimer: CountDownTimer? = null
    private var totalCountDownTimer: CountDownTimer? = null
    private var currentPatientForDocA = MutableLiveData<PatientData>()
    private var currentPatientForDocB = MutableLiveData<PatientData>()
    private var waitingTime = MutableLiveData<Long>()
    private var docAIndex = 0
    private var docBIndex = 1
    private var totalTime = 0L
    private val docAAverageTime = 180000L
    private val docBAverageTime = 240000L
    private var isPatientAdded = false
    private var isCaseStudy1 = true

    private var patientList = LinkedList<PatientData>().apply {
        this.add(PatientData(1, "Patient 1", false))
        this.add(PatientData(2, "Patient 2", false))
        this.add(PatientData(3, "Patient 3", false))
        this.add(PatientData(4, "Patient 4", false))
        this.add(PatientData(5, "Patient 5", false))
    }

    private var patientList2 = LinkedList<PatientData>().apply {
        this.add(PatientData(1, "Patient 1", false))
        this.add(PatientData(2, "Patient 2", false))
        this.add(PatientData(3, "Patient 3", false))
        this.add(PatientData(4, "Patient 4", false))
        this.add(PatientData(5, "Patient 5", false))
        this.add(PatientData(6, "Patient 6", false))
        this.add(PatientData(7, "Patient 7", false))
        this.add(PatientData(8, "Patient 8", false))
        this.add(PatientData(9, "Patient 9", false))
        this.add(PatientData(10, "Patient 10", false))
    }

    fun isCaseStudy1(value: Boolean) {
        isCaseStudy1 = value
        patientList = if (value) patientList else patientList2
        totalCountDownTimer = null
        docATimerCountDownTimer = null
        docBTimerCountDownTimer = null
        totalTime = 0
        docAIndex = 0
        docBIndex = 1
        isPatientAdded = false
    }

    private var doctorList = ArrayList<DoctorData>().apply {
        add(DoctorData(1, "Doctor A", docAAverageTime))
        add(DoctorData(2, "Doctor B", docBAverageTime))
    }

    fun addPatient(name: String) {
        patientList.addLast(PatientData(patientList.size + 1, name, false))
        isPatientAdded = true
    }

    fun startQueue() {
        totalTime = 900000

        CoroutineScope(Dispatchers.Main).launch {
            async { firstDocTimer(doctorList[0]) }
            async { totalCountDownTimer() }
        }
    }

    fun case2StartQueue() {
        totalTime = 960000

        CoroutineScope(Dispatchers.Main).launch {
            async { firstDocTimer(doctorList[0]) }
            async { secondDocTimer(doctorList[1]) }
            async { totalCountDownTimer() }
        }
    }

    fun getCurrentDocAPatient(): LiveData<PatientData> {
        return currentPatientForDocA
    }

    fun getCurrentDocBPatient(): LiveData<PatientData> {
        return currentPatientForDocB
    }

    fun getCurrentTime(): LiveData<Long> {
        return waitingTime
    }

    private fun totalCountDownTimer() {
        if (totalCountDownTimer == null) {
            totalCountDownTimer = object : CountDownTimer(totalTime, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    waitingTime.postValue(millisUntilFinished)
                }

                override fun onFinish() {
                    waitingTime.postValue(0L)
                }

            }
            (totalCountDownTimer as CountDownTimer).start()
        }
    }


    private fun firstDocTimer(doctorData: DoctorData) {
        if (patientList.size <= docAIndex) {
            docATimerCountDownTimer?.cancel()
        } else {
            val countDownInterval = 1000L
            if (!patientList[docAIndex].isConsultationDone) {
                docATimerCountDownTimer =
                    object :
                        CountDownTimer(doctorData.averageConsultationTime, countDownInterval) {
                        override fun onTick(millisUntilFinished: Long) {
                            patientList[docAIndex].isConsultationDone = true
                            currentPatientForDocA.postValue(patientList[docAIndex])
                        }

                        override fun onFinish() {
                            currentPatientForDocA.postValue(patientList[docAIndex])
                            docAIndex += 1
                            cancel()
                            firstDocTimer(doctorData)
                        }
                    }
                (docATimerCountDownTimer as CountDownTimer).start()
            } else {
                docAIndex += 1
                firstDocTimer(doctorData)
            }
        }
    }

    private fun secondDocTimer(doctorData: DoctorData) {
        if (patientList.size <= docBIndex) {
            docBTimerCountDownTimer?.cancel()
        } else {
            val countDownInterval = 1000L
            if (!patientList[docBIndex].isConsultationDone) {
                docBTimerCountDownTimer =
                    object :
                        CountDownTimer(doctorData.averageConsultationTime, countDownInterval) {
                        override fun onTick(millisUntilFinished: Long) {
                            if (docAIndex == docBIndex) {
                                docBIndex++
                            }
                            patientList[docBIndex].isConsultationDone = true
                            currentPatientForDocB.postValue(patientList[docBIndex])
                        }

                        override fun onFinish() {
                            currentPatientForDocB.postValue(patientList[docBIndex])
                            docBIndex += 1
                            cancel()
                            secondDocTimer(doctorData)
                        }
                    }
                (docBTimerCountDownTimer as CountDownTimer).start()
            } else {
                docBIndex += 1
                secondDocTimer(doctorData)
            }
        }
    }
}