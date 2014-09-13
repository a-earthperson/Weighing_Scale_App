Weighing_Scale_App
==================

Initial build for 'Bluetooth weighing app' for TANITA BC-590BT. 

Current Functionality
---------------------
* Dummy discover module to 'search' for new devices. Finds and connects as slave to any Bluetooth device with Serial Port Profile.
* Program opens a 'Bluetooth ServerSocket' to listen for RFCOMM servers.
* Listens for an input stream and parses and displays data when EOF character is read.

KNOWN BUGS
----------
* Connection progress dialog does not terminate even after successful connection or failed connection.
* Class TANITA_Parser can only successfully parse fixed byte data.
* height, weight, muscle mass and bone mass data is only available in RAW data form. Requires correct parsing algorithm.
