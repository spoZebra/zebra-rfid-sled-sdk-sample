package com.spozebra.zebrarfidsledsample

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.spozebra.zebrarfidsledsample.barcode.BarcodeScannerInterface
import com.spozebra.zebrarfidsledsample.barcode.IBarcodeScannedListener
import com.spozebra.zebrarfidsledsample.barcode.TerminalScanDWInterface
import com.spozebra.zebrarfidsledsample.rfid.IRFIDReaderListener
import com.spozebra.zebrarfidsledsample.rfid.RFIDReaderInterface


class MainActivity : AppCompatActivity(), IBarcodeScannedListener, IRFIDReaderListener {

    private val TAG: String = MainActivity::class.java.simpleName
    private val BLUETOOTH_PERMISSION_REQUEST_CODE = 100
    private val ACCESS_FINE_LOCATION_REQUEST_CODE = 99

    private lateinit var progressBar: ProgressBar
    private lateinit var listViewRFID: ListView
    private lateinit var listViewBarcodes: ListView
    private lateinit var radioBtnGroup: RadioGroup

    private var scanConnectionMode : ScanConnectionEnum = ScanConnectionEnum.SledScan
    private var isDWRegistered : Boolean = false
    private var barcodeList : MutableList<String> = mutableListOf()
    private var tagsList : MutableList<String> = mutableListOf()


    private val dataWedgeReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent) {
            val action = intent.action
            if (action == "com.spozebra.zebrarfidsledsample.ACTION") {
                val decodedData: String? = intent.getStringExtra("com.symbol.datawedge.data_string")
                this@MainActivity.newBarcodeScanned(decodedData)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        progressBar = findViewById(R.id.progressBar)
        listViewRFID = findViewById(R.id.listViewRFID)
        listViewBarcodes = findViewById(R.id.listViewBarcodes)
        radioBtnGroup = findViewById(R.id.radioGroup)

        val tagsLIstAdapter = ArrayAdapter(this,android.R.layout.simple_list_item_1,tagsList)
        listViewRFID.adapter = tagsLIstAdapter

        val barcodeListAdapter = ArrayAdapter(this,android.R.layout.simple_list_item_1,barcodeList)
        listViewBarcodes.adapter = barcodeListAdapter

        radioBtnGroup.setOnCheckedChangeListener { _, checkedId ->
            when(checkedId){
                R.id.radiobtn_sled -> scanConnectionMode = ScanConnectionEnum.SledScan
                R.id.radiobtn_terminal -> scanConnectionMode = ScanConnectionEnum.TerminalScan
            }

            dispose()
            configureDevice()
        }


        //Scanner Initializations
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), ACCESS_FINE_LOCATION_REQUEST_CODE);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                )
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ),
                    BLUETOOTH_PERMISSION_REQUEST_CODE
                )
            } else {
                configureDevice()
            }
        } else {
            configureDevice()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        if (requestCode == BLUETOOTH_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                configureDevice()
            } else {
                Toast.makeText(this, "Bluetooth Permissions not granted", Toast.LENGTH_SHORT).show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun configureDevice() {
        progressBar.visibility = ProgressBar.VISIBLE
        Thread {
            var connectScannerResult = false

            // CONFIGURE SCANNER
            // If terminal scan was selected, we must use DataWedge instead of the SDK
            if (scanConnectionMode == ScanConnectionEnum.TerminalScan)
            {
                var dwConf = TerminalScanDWInterface(applicationContext);
                dwConf.configure()
                connectScannerResult = true

                // Register DW receiver
                registerReceivers()
            }
            else {
                // Configure BT Scanner
                if (scannerInterface == null)
                    scannerInterface = BarcodeScannerInterface(this)

                connectScannerResult = scannerInterface!!.connect(applicationContext)
            }

            // Configure RFID
            if (rfidInterface == null)
                rfidInterface = RFIDReaderInterface(this)

            var connectRFIDResult = rfidInterface!!.connect(applicationContext, scanConnectionMode)

            runOnUiThread {
                progressBar.visibility = ProgressBar.GONE
                Toast.makeText(
                    applicationContext,
                    if (connectRFIDResult && connectScannerResult) "Reader & Scanner are connected!" else "Connection ERROR!",
                    Toast.LENGTH_LONG
                ).show()
            }
        }.start()
    }

    // Create filter for the broadcast intent
    private fun registerReceivers() {
        val filter = IntentFilter()
        filter.addAction("com.symbol.datawedge.api.NOTIFICATION_ACTION") // for notification result
        filter.addAction("com.symbol.datawedge.api.RESULT_ACTION") // for error code result
        filter.addCategory(Intent.CATEGORY_DEFAULT) // needed to get version info

        // register to received broadcasts via DataWedge scanning
        filter.addAction("$packageName.ACTION")
        filter.addAction("$packageName.service.ACTION")
        registerReceiver(dataWedgeReceiver, filter)
        isDWRegistered = true
    }

    override fun newBarcodeScanned(barcode: String?) {
        runOnUiThread {
            barcodeList.add(0, barcode!!)
            listViewBarcodes.invalidateViews()
        }
    }

    override fun newTagRead(epc: String?) {
        runOnUiThread {
            tagsList.add(0, epc!!)
            listViewRFID.invalidateViews()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        dispose()
    }

    private fun dispose(){
        try {
            if (isDWRegistered)
                unregisterReceiver(dataWedgeReceiver)

            isDWRegistered = false

            if (rfidInterface != null) {
                rfidInterface!!.onDestroy()
            }
            if (scannerInterface != null) {
                scannerInterface!!.onDestroy()
            }
        }
        catch (ex : Exception){}
    }

    companion object {
        private var rfidInterface: RFIDReaderInterface? = null
        private var scannerInterface: BarcodeScannerInterface? = null
    }

}
