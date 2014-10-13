Weighing_Scale_App
==================
Android application used to connect to bluetooth devices in master/slave mode. Primary application involves querying->fetching->parsing data from Bluetooth enabled weighing scales.

This build provides support for *TANITA BC-590BT*

Building Requirements
---------------------
* Android SDK >= 15.0
* Bluetooth v2.0 + EDR - SPP support
* *Android wear currently not supported*

Current Functionality
---------------------
* Dummy discover module to 'search' for new devices. Finds and connects as slave to any Bluetooth device with SPP.
* Program opens a 'Bluetooth ServerSocket' to listen for RFCOMM servers.
* Listens for an input stream and parses and displays data when EOF character is read.

Known Bugs
----------
* Connection progress dialog fails to terminate on callback.
* Incomplete support for parsing data from TANITA-BC590BT
