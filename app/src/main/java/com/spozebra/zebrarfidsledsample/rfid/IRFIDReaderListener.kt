package com.spozebra.zebrarfidsledsample.rfid

interface IRFIDReaderListener {
    fun newTagRead(epc : String?)
}