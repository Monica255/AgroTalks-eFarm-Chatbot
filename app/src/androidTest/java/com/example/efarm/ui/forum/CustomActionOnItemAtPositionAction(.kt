package com.example.efarm.ui.forum

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers
import org.hamcrest.Matcher

class CustomActionOnItemAtPositionAction(
    private val position: Int,
    private val action: ViewAction
) : ViewAction {

    override fun getConstraints(): Matcher<View> {
        return ViewMatchers.isAssignableFrom(RecyclerView::class.java)
    }

    override fun getDescription(): String {
        return "Performing action on item at position $position"
    }

    override fun perform(uiController: UiController, view: View) {
        val recyclerView = view as RecyclerView
        val viewHolder = recyclerView.findViewHolderForAdapterPosition(position)
        if (viewHolder != null) {
            action.perform(uiController, viewHolder.itemView)
        } else {
            throw IllegalStateException("No view holder found at position: $position")
        }
    }
}