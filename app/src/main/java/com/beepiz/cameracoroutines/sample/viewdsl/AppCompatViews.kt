@file:Suppress("NOTHING_TO_INLINE")

package com.beepiz.cameracoroutines.sample.viewdsl

import android.content.Context
import android.support.v7.widget.AppCompatAutoCompleteTextView
import android.support.v7.widget.AppCompatButton
import android.support.v7.widget.AppCompatCheckBox
import android.support.v7.widget.AppCompatCheckedTextView
import android.support.v7.widget.AppCompatEditText
import android.support.v7.widget.AppCompatImageButton
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.AppCompatMultiAutoCompleteTextView
import android.support.v7.widget.AppCompatRadioButton
import android.support.v7.widget.AppCompatRatingBar
import android.support.v7.widget.AppCompatSeekBar
import android.support.v7.widget.AppCompatSpinner
import android.support.v7.widget.AppCompatTextView
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.CheckBox
import android.widget.CheckedTextView
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.MultiAutoCompleteTextView
import android.widget.RadioButton
import android.widget.RatingBar
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView

/** Matches [android.support.v7.app.AppCompatViewInflater.createView] content. */

inline fun textView(ctx: Context): TextView = AppCompatTextView(ctx)
inline fun imageView(ctx: Context): ImageView = AppCompatImageView(ctx)
inline fun button(ctx: Context): Button = AppCompatButton(ctx)
inline fun editText(ctx: Context): EditText = AppCompatEditText(ctx)
inline fun spinner(ctx: Context): Spinner = AppCompatSpinner(ctx)
inline fun imageButton(ctx: Context): ImageButton = AppCompatImageButton(ctx)
inline fun checkBox(ctx: Context): CheckBox = AppCompatCheckBox(ctx)
inline fun radioButton(ctx: Context): RadioButton = AppCompatRadioButton(ctx)
inline fun checkedTextView(ctx: Context): CheckedTextView = AppCompatCheckedTextView(ctx)
inline fun autoCompleteTextView(ctx: Context): AutoCompleteTextView = AppCompatAutoCompleteTextView(ctx)
inline fun multiAutoCompleteTextView(ctx: Context): MultiAutoCompleteTextView = AppCompatMultiAutoCompleteTextView(ctx)
inline fun ratingBar(ctx: Context): RatingBar = AppCompatRatingBar(ctx)
inline fun seekBar(ctx: Context): SeekBar = AppCompatSeekBar(ctx)
