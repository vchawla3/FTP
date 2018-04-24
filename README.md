# CSC 573 IP Project 2 Report - FTP
Vaibhav Chawla (vchawla3)  
Shikhar Sharma (ssharm29)

Click [Here](https://github.ncsu.edu/vchawla3/573_FTP) to access ncsu the repository and view the readme in a better format

### Compilation Instructions  
`javac *.java`

### Running the Program
* Server: `java Server [port#] [file-name] [probability]`
  * No interaction, will print the sequence number whenever packet is dropped (r <= p)
  * Will also notify/print once the file is finished downloading
  * Runs the server on localhost
* Client: `java Client [server-host-name] [server-port#] [file-name] [N] [MSS]`
  * No interaction, will simply output whenever Timeout occurs and at what sequence number it occurred at
  * Will also nofity/print once the file is finished sending, print delay time it took, and then exit.

# Report and Task Data

File: data.txt  
File Size: 1.204631 MegaBytes  
RTT to 159.65.229.221 from Traceroute: 22.24933333 seconds (Avg of 3 values outputted from Traceroute)  
Timeout set on Client: 100 ms  

Click [Here](https://docs.google.com/spreadsheets/d/1yi312RJvs_x-Ckh5s_HkV73U5v2Zt9t0uBld9yhZwB0/edit?usp=sharing) to view the Google spreadsheet with our data and charts

## Task 1

## Task 2

## Task 3