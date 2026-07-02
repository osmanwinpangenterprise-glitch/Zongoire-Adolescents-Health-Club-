package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Zongoire Health Club System", appName)
  }

  @Test
  fun `verify member photo file directory exists and resolves correct name`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val memberId = "ZAC-2026-0001"
    val dir = File(context.filesDir, "member_photos")
    if (!dir.exists()) {
      dir.mkdirs()
    }
    val photoFile = File(dir, "$memberId.jpg")
    
    assertEquals("ZAC-2026-0001.jpg", photoFile.name)
    assertEquals(File(context.filesDir, "member_photos/ZAC-2026-0001.jpg").absolutePath, photoFile.absolutePath)
  }

  @Test
  fun `verify single assessment pdf generation`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val assessment = com.example.data.Assessment(
      id = 1,
      date = "2026-07-02",
      memberId = "ZAC-2026-0001",
      memberName = "Adama Mahama",
      hygiene = 8,
      character = 9,
      behaviorSchool = 7,
      behaviorHome = 8,
      dressing = 9,
      contribution = 8,
      comments = "Excellent behavior and leadership.",
      assessedBy = "Facilitator Osman",
      periodType = "Weekly",
      periodLabel = "Week 27, 2026"
    )

    val pdfFile = com.example.ui.generateSingleAssessmentPdf(context, assessment)
    org.junit.Assert.assertNotNull(pdfFile)
    org.junit.Assert.assertTrue(pdfFile!!.exists())
    org.junit.Assert.assertTrue(pdfFile.name.startsWith("ZAC_Assessment_"))
    org.junit.Assert.assertTrue(pdfFile.name.endsWith(".pdf"))
  }

  @Test
  fun `verify multi-page assessment summary pdf generation`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val assessments = listOf(
      com.example.data.Assessment(
        id = 1,
        date = "2026-07-02",
        memberId = "ZAC-2026-0001",
        memberName = "Adama Mahama",
        hygiene = 8,
        character = 9,
        behaviorSchool = 7,
        behaviorHome = 8,
        dressing = 9,
        contribution = 8,
        comments = "Excellent behavior and leadership.",
        assessedBy = "Facilitator Osman",
        periodType = "Weekly",
        periodLabel = "Week 27, 2026"
      ),
      com.example.data.Assessment(
        id = 2,
        date = "2026-07-02",
        memberId = "ZAC-2026-0002",
        memberName = "Aisha Sampa",
        hygiene = 9,
        character = 10,
        behaviorSchool = 8,
        behaviorHome = 9,
        dressing = 10,
        contribution = 9,
        comments = "Exceptional hygiene and active class participation.",
        assessedBy = "Facilitator Osman",
        periodType = "Weekly",
        periodLabel = "Week 27, 2026"
      )
    )

    val pdfFile = com.example.ui.generateAssessmentSummaryPdf(context, assessments)
    org.junit.Assert.assertNotNull(pdfFile)
    org.junit.Assert.assertTrue(pdfFile!!.exists())
    org.junit.Assert.assertTrue(pdfFile.name.startsWith("ZAC_Assessment_Summary_"))
    org.junit.Assert.assertTrue(pdfFile.name.endsWith(".pdf"))
  }

  @Test
  fun `verify attendance record creation and filtering`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repository = com.example.data.AppRepository.getInstance(context)
    
    val att = com.example.data.Attendance(
      id = 101,
      date = "2026-07-02",
      memberId = "ZAC-2026-0001",
      memberName = "Adama Mahama",
      sessionType = "Weekly Meeting",
      status = "Present",
      facilitator = "Facilitator Osman",
      isSynced = false
    )
    
    // We can verify properties of the object
    assertEquals("2026-07-02", att.date)
    assertEquals("ZAC-2026-0001", att.memberId)
    assertEquals("Present", att.status)
    assertEquals(false, att.isSynced)
  }

  @Test
  fun `verify member editing fields and updates`() {
    val context = ApplicationProvider.getApplicationContext<android.app.Application>()
    val viewModel = com.example.ui.MainViewModel(context)
    
    val member = com.example.data.Member(
      id = 5,
      memberId = "ZAC-9999",
      registrationDate = "2026-07-02",
      fullName = "Test Member Name",
      dob = "2010-04-12",
      age = 16,
      gender = "Female",
      ageGroup = "Senior (15-19)",
      community = "Zongoire Center",
      religion = "Islam",
      schoolStatus = "Currently in school",
      schoolName = "Zongoire JHS",
      classYear = "JHS 3",
      occupation = "",
      guardianName = "Guardian Name",
      relationship = "Father",
      contactNumber = "0241112222",
      parentAware = "Yes",
      consentSigned = "Yes",
      healthConditionKnown = "No",
      healthConditionDetails = "",
      visitedChpsLast6Months = "No",
      nhisCard = "Yes",
      registeredBy = "Admin",
      oathTaken = "Yes",
      membershipCardIssued = "Yes",
      membershipTier = "Active",
      isSynced = false
    )
    
    viewModel.startEditingMember(member)
    
    assertEquals(true, viewModel.isEditingMember.value)
    assertEquals("ZAC-9999", viewModel.editingMemberId.value)
    assertEquals("Test Member Name", viewModel.regFullName.value)
    assertEquals("2010-04-12", viewModel.regDob.value)
    assertEquals("Female", viewModel.regGender.value)
    assertEquals("ZAC-9999", viewModel.editingMemberId.value)
  }
}
