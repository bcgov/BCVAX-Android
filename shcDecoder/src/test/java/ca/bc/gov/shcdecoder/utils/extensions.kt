package ca.bc.gov.shcdecoder.utils

import org.mockito.ArgumentCaptor

fun <T> ArgumentCaptor<T>.safeCapture(): T = this.capture()