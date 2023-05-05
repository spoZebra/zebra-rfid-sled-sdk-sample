package com.spozebra.zebrarfidsledsample.barcode

interface IBarcodeScannedListener {
    fun newBarcodeScanned(barcode : String?)
}