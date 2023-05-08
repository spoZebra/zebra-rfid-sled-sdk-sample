# ZebraRFIDSledSample
Sample app that shows how to use properly our Zebra SDK to control both RFID antenna and embedded scanner of Zebra RFID Sleds.
If your RFID sled has not embeeded scanner (available on premium versions) and you're using a Zebra device connected thru eConnex, you could trigger the terminal one - See this link as reference: https://techdocs.zebra.com/dcs/rfid/android/2-0-2-94/tutorials/triggerremapping/

***Right now, DataWedge does not support embedded scan engine of RFD40 and RFD90, therefore you should use the scanner module of Zebra RFID SDK to make it run.***

## Hardware Requirements
- One of these Zebra RFID sleds: RFD40 or RFD90
- A device running A8 or above

## Code snippets

- **Use embedded scan engine of RFID Sled**. 
This can be achieve by importing the library *com.zebra.scannercontrol* available within the RFID SDK.
https://github.com/spoZebra/ZebraRFIDSledSample/blob/811cf39d2a708118fd272d515b618b4c4f4f8d0b/app/src/main/java/com/spozebra/zebrarfidsledsample/barcode/BarcodeScannerInterface.kt#L15-L52

- **Use Zebra device scan engine**. This can be achieved by modifing trigger settings with our RFID SDK and creating a DataWedge profile to receive barcodes via intent.
RFID SDK setup: https://github.com/spoZebra/ZebraRFIDSledSample/blob/811cf39d2a708118fd272d515b618b4c4f4f8d0b/app/src/main/java/com/spozebra/zebrarfidsledsample/rfid/RFIDReaderInterface.kt#L72-L73
DataWedge profile setup: https://github.com/spoZebra/ZebraRFIDSledSample/blob/811cf39d2a708118fd272d515b618b4c4f4f8d0b/app/src/main/java/com/spozebra/zebrarfidsledsample/barcode/TerminalScanDWInterface.kt#L18-L64

- **Configure RFID**
https://github.com/spoZebra/ZebraRFIDSledSample/blob/811cf39d2a708118fd272d515b618b4c4f4f8d0b/app/src/main/java/com/spozebra/zebrarfidsledsample/rfid/RFIDReaderInterface.kt#L19-L85

## Zebra RFID SDK Full documentation
https://techdocs.zebra.com/dcs/rfid/
