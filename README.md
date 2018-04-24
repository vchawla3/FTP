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

File: **data.txt**  
File Size: **1.204631 MegaBytes**  
RTT to Server IP (159.65.229.221 for us) from Traceroute: **22.24933333 seconds** (Avg of 3 values outputted from Traceroute)  
Timeout set on Client: **100 ms**  

Click [Here](https://docs.google.com/spreadsheets/d/1yi312RJvs_x-Ckh5s_HkV73U5v2Zt9t0uBld9yhZwB0/edit?usp=sharing) to view the Google spreadsheet with our data and charts

## Task 1
![data](https://github.ncsu.edu/vchawla3/573_FTP/blob/master/TaskResults/Task1Table.png)
![chart](https://github.ncsu.edu/vchawla3/573_FTP/blob/master/TaskResults/Task1Chart.png)  

For this task, we can see that as the Window Size N increases, the average delay at first speeds up by a lot, but as N gets bigger, the average delay plateaus and in the long run it seems like it can actually be detrimental to the average delay. It makes sense that the delay would not be substantially better with a large N because whenever a timeout occurs, the sender has to **retransmit** a large amount of packets from a loss of simply 1 packet which would increase the overall time to send.

## Task 2
![data](https://github.ncsu.edu/vchawla3/573_FTP/blob/master/TaskResults/Task2Table.png)
![chart](https://github.ncsu.edu/vchawla3/573_FTP/blob/master/TaskResults/Task2Chart.png)

## Task 3
![data](https://github.ncsu.edu/vchawla3/573_FTP/blob/master/TaskResults/Task3Table.png)
![chart](https://github.ncsu.edu/vchawla3/573_FTP/blob/master/TaskResults/Task3Chart.png)