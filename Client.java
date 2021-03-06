import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.*;
import java.io.IOException;
import java.io.*;
import java.net.*;
import java.net.Socket;
import java.util.*;

public class Client {
	static final String expectedDataPacketValue = "0101010101010101";
	static final String ackPacket = "00000000000000001010101010101010";								 
	static final String expectedENDPacketValue = "1111111110000000";

	static String serverHost;
	static int serverPort;
	static String filename;
	static int windowSizeN;
	static int mss;

	static int currSequenceNum = 0;
	static int latestAckedSeqNo = 0;
	static int sequenceMax;

	static byte[] filedata;
	static ArrayList<byte[]> splitFile;

	static DatagramSocket clientToServer;

	public static void main(String args[]) {
		try {
			serverHost = args[0];
			serverPort = Integer.parseInt(args[1]);
			filename = args[2];
			windowSizeN = Integer.parseInt(args[3]);
			mss = Integer.parseInt(args[4]);
			sequenceMax = windowSizeN + 1;

			split_file();

			long startTime = System.nanoTime();
			rdt_send();
			long estimatedTime = System.nanoTime() - startTime;
			System.out.println("Delay is " + estimatedTime);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private static void split_file() {
		try {
			//First split up the file into byte arrays with max size of mss
			Path path = Paths.get(filename);
			filedata = Files.readAllBytes(path);
			
			//total number of datagrams (file length divided by mss)
			//Also the max sequence number
			sequenceMax = (int) Math.ceil(((double) filedata.length) / mss);

			// System.out.println(filedata.length);
			// System.out.println(mss);
			// System.out.println(filedata.length / mss);
			// System.out.println(total);

			splitFile = new ArrayList<byte[]>(sequenceMax);

			for (int i = 0,j = 0; i <sequenceMax; i++) {
				// Copy data of length mss
				byte[] data;
				if (i == sequenceMax - 1) {
					//is last segment of data
					data = Arrays.copyOfRange(filedata, j, filedata.length);
				} else {
					//always up to size mss
					data = Arrays.copyOfRange(filedata, j, j+mss);
					//increment by mss
					j+=mss;	
				}
	          	splitFile.add(data);
	          	//System.out.println(data);
			}
			//System.out.println("File split up into " + sequenceMax + " ");
		} catch (Exception ex) {
			ex.printStackTrace();
		}		
	}
	private static void rdt_send() {
		try {
			clientToServer = new DatagramSocket();

			ArrayList<Integer> seqNumbersSent = new ArrayList<Integer>(windowSizeN);
			
			int ackSeqNo = 0;
			boolean loop = true;
			while(loop) {
				while (currSequenceNum - latestAckedSeqNo < windowSizeN && currSequenceNum < sequenceMax) {
					byte[] data = splitFile.get(currSequenceNum);
					send_data(currSequenceNum, data);
					//Keep track of the ones sent
					seqNumbersSent.add(currSequenceNum);
					currSequenceNum++;
				}
				byte [] receiveacks = new byte[2048];
         		DatagramPacket ackDGPacket = new DatagramPacket(receiveacks,2048);
				
				try {
					//100 millisecond timeout
					clientToServer.setSoTimeout(100);
					clientToServer.receive(ackDGPacket);

					byte[] ackdata = ackDGPacket.getData();

					ackSeqNo = Integer.parseInt(new String(Arrays.copyOfRange(ackdata, 0, 32)),2);
					
        			String isACK = new String(Arrays.copyOfRange(ackdata, 32, 64));
        			if (isACK.equals(ackPacket)) {
        				//confirmed it is an ack packet
        				if(ackSeqNo == sequenceMax){
        					//if the ack is the last one, no more sending
        					loop = false;
        				}
        				//check if this is the most recent ack aka the cumulitive ack (highest number) 
        				if (latestAckedSeqNo < ackSeqNo) latestAckedSeqNo = ackSeqNo;
        			}
				} catch (SocketTimeoutException ex) {
					//resend packets from latestacked till current seq sent
					System.out.println("Timeout, sequence number = " + ackSeqNo);
					int i = latestAckedSeqNo;
					while(i < currSequenceNum){
						byte[] data = splitFile.get(i);
						send_data(i, data);
						i++;
					}
				}
				
			}
			sendFinish();
			System.out.println("File done sending");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private static void send_data(int seq, byte[] data) {
		try {
			//Make sequence number a binary string representation, needs padding to make 32 bits

			String binaryString = Integer.toBinaryString(seq);
			String pad = "";
			for(int i = 0; i < 32 - binaryString.length(); i++) {
				pad += "0";
			}
			binaryString = pad + binaryString;
			
			String checksum = computeChecksum(data);

			byte[] header = (binaryString + checksum + expectedDataPacketValue).getBytes();
			
			//combine header and file data into one array
			byte[] send = new byte[header.length + data.length];
			System.arraycopy(header,0,send,0,header.length);
			System.arraycopy(data,0,send,header.length,data.length); 


			DatagramPacket dataPack = new DatagramPacket(send, send.length, InetAddress.getByName(serverHost), serverPort);
			clientToServer.send(dataPack);
			//System.out.println("Sent segment number " + seq);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	private static void sendFinish() {
		try {
			//Make sequence number a binary string representation, needs padding to make 48 0 bits and then the expected ack value
			String binaryString = expectedENDPacketValue;
			String pad = "";
			for(int i = 0; i < 48; i++) {
				pad += "0";
			}
			binaryString = pad + binaryString;

			//System.out.println(binaryString);

			byte[] send = (binaryString).getBytes();
			
			DatagramPacket dataPack = new DatagramPacket(send, send.length, InetAddress.getByName(serverHost), serverPort);
			clientToServer.send(dataPack);
			//System.out.println("Sent finish segment!!");
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	private static String computeChecksum(byte[] buf) {
	    int length = buf.length;
	    int i = 0;

	    int sum = 0;
	    int data;

	    // Handle all pairs
	    while (length > 1) {
	      // Corrected to include @Andy's edits and various comments on Stack Overflow
	      data = (((buf[i] << 8) & 0xFF00) | ((buf[i + 1]) & 0xFF));
	      sum += data;
	      // 1's complement carry bit correction in 16-bits (detecting sign extension)
	      if ((sum & 0xFFFF0000) > 0) {
	        sum = sum & 0xFFFF;
	        sum += 1;
	      }

	      i += 2;
	      length -= 2;
	    }

	    // Handle remaining byte in odd length buffers
	    if (length > 0) {
	      sum += (buf[i] << 8 & 0xFF00);
	      // 1's complement carry bit correction in 16-bits (detecting sign extension)
	      if ((sum & 0xFFFF0000) > 0) {
	        sum = sum & 0xFFFF;
	        sum += 1;
	      }
	    }

	    // Final 1's complement value correction to 16-bits
	    sum = ~sum;
	    sum = sum & 0xFFFF;

	    //System.out.println(sum);
	    //System.out.println(checksum);

	    // Return the checksum in String
	    String chk = Integer.toBinaryString(sum);
	    
	    //pad with 0s to make it 16 bits
	    String pad = "";
		for(int j = 0; j < 16 - chk.length(); j++) {
			pad += "0";
		}
	    return pad + chk;
	}
}