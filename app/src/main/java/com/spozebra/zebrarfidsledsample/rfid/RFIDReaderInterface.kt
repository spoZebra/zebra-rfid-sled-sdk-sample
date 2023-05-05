package com.spozebra.zebrarfidsledsample.rfid

import android.content.Context
import android.util.Log
import com.spozebra.zebrarfidsledsample.MainActivity
import com.spozebra.zebrarfidsledsample.ScanConnectionEnum
import com.zebra.rfid.api3.*
import java.util.*

class RFIDReaderInterface(var listener: IRFIDReaderListener) : RfidEventsListener {

    private val TAG: String = RFIDReaderInterface::class.java.simpleName

    private lateinit var readers: Readers
    private var availableRFIDReaderList: ArrayList<ReaderDevice>? = null
    private var readerDevice: ReaderDevice? = null
    lateinit var reader: RFIDReader

    fun connect(context: Context, scanConnectionMode : ScanConnectionEnum): Boolean {
        // Init
        readers = Readers(context, ENUM_TRANSPORT.ALL)

        try {
            if (readers != null) {
                availableRFIDReaderList = readers.GetAvailableRFIDReaderList()
                if (availableRFIDReaderList != null && availableRFIDReaderList!!.size != 0) {
                    // get first reader from list
                    readerDevice = availableRFIDReaderList!![0]
                    reader = readerDevice!!.rfidReader
                    if (!reader!!.isConnected) {
                        Log.d(TAG, "RFID Reader Connecting...")
                        reader!!.connect()
                        configureReader(scanConnectionMode)
                        Log.d(TAG, "RFID Reader Connected!")
                        return true
                    }
                }
            }
        } catch (e: InvalidUsageException) {
            e.printStackTrace()
        } catch (e: OperationFailureException) {
            e.printStackTrace()
        } catch (e: OperationFailureException) {
            e.printStackTrace()
        } catch (e: InvalidUsageException) {
            e.printStackTrace()
        }
        Log.d(TAG, "RFID Reader connection error!")
        return false
    }

    private fun configureReader(scanConnectionMode : ScanConnectionEnum) {
        if (reader.isConnected) {
            val triggerInfo = TriggerInfo()
            triggerInfo.StartTrigger.triggerType = START_TRIGGER_TYPE.START_TRIGGER_TYPE_IMMEDIATE
            triggerInfo.StopTrigger.triggerType = STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_IMMEDIATE
            try {
                // receive events from reader
                reader.Events.addEventsListener(this)
                // HH event
                reader.Events.setHandheldEvent(true)
                // tag event with tag data
                reader.Events.setTagReadEvent(true)
                // application will collect tag using getReadTags API
                reader.Events.setAttachTagDataWithReadEvent(false)

                // set start and stop triggers
                reader.Config.startTrigger = triggerInfo.StartTrigger
                reader.Config.stopTrigger = triggerInfo.StopTrigger

                // Terminal scan, use trigger for scanning!
                if(scanConnectionMode == ScanConnectionEnum.TerminalScan)
                    reader.Config.setKeylayoutType(ENUM_KEYLAYOUT_TYPE.UPPER_TRIGGER_FOR_SCAN)
                else
                    reader.Config.setKeylayoutType(ENUM_KEYLAYOUT_TYPE.UPPER_TRIGGER_FOR_SLED_SCAN)



            } catch (e: InvalidUsageException) {
                e.printStackTrace()
            } catch (e: OperationFailureException) {
                e.printStackTrace()
            }
        }
    }

    // Status Event Notification
    override fun eventStatusNotify(rfidStatusEvents: RfidStatusEvents) {
        Log.d(TAG, "Status Notification: " + rfidStatusEvents.StatusEventData.statusEventType)
        if (rfidStatusEvents.StatusEventData.statusEventType === STATUS_EVENT_TYPE.HANDHELD_TRIGGER_EVENT) {
            if (rfidStatusEvents.StatusEventData.HandheldTriggerEventData.handheldEvent === HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_PRESSED) {
                try {
                    reader.Actions.Inventory.perform();
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else if (rfidStatusEvents.StatusEventData.HandheldTriggerEventData.handheldEvent === HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_RELEASED) {
                try {
                    reader.Actions.Inventory.stop()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    // Read Event Notification
    override fun eventReadNotify(e: RfidReadEvents) {
        // Recommended to use new method getReadTagsEx for better performance in case of large tag population
        val myTags: Array<TagData> = reader.Actions.getReadTags(100)
        if (myTags != null) {
            for (tag in myTags) {
                listener.newTagRead(tag.tagID)
            }
        }
    }

    fun onDestroy() {
        try {
            if (reader != null) {
                reader.Events?.removeEventsListener(this)
                reader.disconnect()
                reader.Dispose()
                readers.Dispose()
            }
        } catch (e: InvalidUsageException) {
            e.printStackTrace()
        } catch (e: OperationFailureException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}