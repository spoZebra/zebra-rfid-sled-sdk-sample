# ZebraRFIDSledSample
Sample app that shows how to use properly our Zebra SDK to control both RFID antenna and scanner of Zebra RFID Sleds.
If your RFID sled does not have the embedded scanner (available on premium versions only) and you're using a Zebra device connected thru eConnex, you could trigger the scan from the device directly from the gun button - See this link as a reference: https://techdocs.zebra.com/dcs/rfid/android/2-0-2-94/tutorials/triggerremapping/

***Right now, DataWedge does not support embedded scan engine of RFD90, therefore you should use the scanner module of Zebra RFID SDK to make it run. We expect to include this funcionnality in DataWedge by the end of 2023***

## Hardware Requirements
- Zebra RFD90
- A device running A8 or above

## Demo

https://user-images.githubusercontent.com/101400857/236826323-47592302-3d2d-4068-9b3f-fbbc0daa4f54.mp4

## Code snippets

- **Use embedded scan engine of RFID Sled**. 
This can be achieve by importing the library *com.zebra.scannercontrol* available within the RFID SDK.
https://github.com/spoZebra/ZebraRFIDSledSample/blob/811cf39d2a708118fd272d515b618b4c4f4f8d0b/app/src/main/java/com/spozebra/zebrarfidsledsample/barcode/BarcodeScannerInterface.kt#L15-L52

- **Use Zebra device scan engine**. This can be achieved by modifing trigger settings with our RFID SDK and creating a DataWedge profile to receive barcodes via intent.
RFID SDK setup: https://github.com/spoZebra/ZebraRFIDSledSample/blob/811cf39d2a708118fd272d515b618b4c4f4f8d0b/app/src/main/java/com/spozebra/zebrarfidsledsample/rfid/RFIDReaderInterface.kt#L72-L73
DataWedge profile setup: https://github.com/spoZebra/ZebraRFIDSledSample/blob/811cf39d2a708118fd272d515b618b4c4f4f8d0b/app/src/main/java/com/spozebra/zebrarfidsledsample/barcode/TerminalScanDWInterface.kt#L18-L64

- **Configure RFID**. Standard configuration to read RFID tags.
Configuration: https://github.com/spoZebra/ZebraRFIDSledSample/blob/811cf39d2a708118fd272d515b618b4c4f4f8d0b/app/src/main/java/com/spozebra/zebrarfidsledsample/rfid/RFIDReaderInterface.kt#L19-L85
Read tags: https://github.com/spoZebra/ZebraRFIDSledSample/blob/811cf39d2a708118fd272d515b618b4c4f4f8d0b/app/src/main/java/com/spozebra/zebrarfidsledsample/rfid/RFIDReaderInterface.kt#L108-L116

## Zebra RFID SDK Full documentation
https://techdocs.zebra.com/dcs/rfid/
