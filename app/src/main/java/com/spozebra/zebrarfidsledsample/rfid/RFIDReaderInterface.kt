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
                    // Read all memory banks
                    val memoryBanksToRead = arrayOf(MEMORY_BANK.MEMORY_BANK_EPC, MEMORY_BANK.MEMORY_BANK_TID, MEMORY_BANK.MEMORY_BANK_USER);
                    for (bank in memoryBanksToRead) {
                        val ta = TagAccess()
                        val sequence = ta.Sequence(ta)
                        val op = sequence.Operation()
                        op.accessOperationCode = ACCESS_OPERATION_CODE.ACCESS_OPERATION_READ
                        op.ReadAccessParams.memoryBank = bank ?: throw IllegalArgumentException("bank must not be null")
                        reader.Actions.TagAccess.OperationSequence.add(op)
                    }

                    reader.Actions.TagAccess.OperationSequence.performSequence()

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else if (rfidStatusEvents.StatusEventData.HandheldTriggerEventData.handheldEvent === HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_RELEASED) {
                try {
                    reader.Actions.TagAccess.OperationSequence.stopSequence()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    // Read Event Notification
    override fun eventReadNotify(e: RfidReadEvents) {
        // Each access belong to a tag.
        // Therefore, as we are performing an access sequence on 3 Memory Banks, each tag could be reported 3 times
        // Each tag data represents a memory bank
        val readTags = reader.Actions.getReadTags(100)
        if (readTags != null) {
            val readTagsList = readTags.toList()
            val tagReadGroup = readTagsList.groupBy { it.tagID }.toMutableMap()

            var epc = ""
            var tid = ""
            var usr = ""
            for (tagKey in tagReadGroup.keys) {
                val tagValueList = tagReadGroup[tagKey]

                for (tagData in tagValueList!!) {
                    if (tagData.opCode == ACCESS_OPERATION_CODE.ACCESS_OPERATION_READ) {
                        when (tagData.memoryBank.ordinal) {
                            MEMORY_BANK.MEMORY_BANK_EPC.ordinal -> epc = getMemBankData(tagData.memoryBankData, tagData.opStatus)
                            MEMORY_BANK.MEMORY_BANK_TID.ordinal -> tid = getMemBankData(tagData.memoryBankData, tagData.opStatus)
                            MEMORY_BANK.MEMORY_BANK_USER.ordinal -> usr = getMemBankData(tagData.memoryBankData, tagData.opStatus)
                        }
                    }
                }
                var myTag = "EPC ${epc}\nTID ${tid}\nUSER ${usr}\n"
                listener.newTagRead(myTag)
            }
        }
    }

    fun getMemBankData(memoryBankData : String?, opStatus : ACCESS_OPERATION_STATUS) : String {
        return if(opStatus != ACCESS_OPERATION_STATUS.ACCESS_SUCCESS){
            opStatus.toString()
        } else
            memoryBankData!!
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