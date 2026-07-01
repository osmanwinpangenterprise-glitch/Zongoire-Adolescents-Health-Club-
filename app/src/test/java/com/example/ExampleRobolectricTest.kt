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
}
