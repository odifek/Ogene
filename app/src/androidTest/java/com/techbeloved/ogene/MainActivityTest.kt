package com.techbeloved.ogene

import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

class MainActivityTest {

    @Before
    fun setUp() {
    }

    @Test
    fun songListIsLoaded() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)

        onView(withId(R.id.recyclerview_song_list)).check(ViewAssertions.matches(isDisplayed()))

    }
}