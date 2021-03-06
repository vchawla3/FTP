import java.io.IOException;
import java.io.*;
import java.net.*;
import java.net.Socket;
import java.util.*;


public class Server {
	static int port;
	static String filename;
	static double prob;
	static final String expectedDataPacketValue = "0101010101010101";
	static final String ackPacket = "00000000000000001010101010101010";
	static final String expectedENDPacketValue = "1111111110000000";

	public static void main(String args[]) {
		try {
			port = Integer.parseInt(args[0]);
			filename = args[1];
			prob = Double.parseDouble(args[2]);

			DatagramSocket ssock = new DatagramSocket(port);
			System.out.println("Listening on port " + port);
			handleFTP(ssock);
		} catch (IOException ex) {
				System.out.println("Could not accept client");
				ex.printStackTrace();
		}
	}

	public static void handleFTP(DatagramSocket ssock) {
		try {
			int expectedSeq = 0;

			FileWriter fw = new FileWriter(filename);
			boolean loop = true;

			while(loop) {
				byte[] inData = new byte[2048];
				DatagramPacket rec = new DatagramPacket(inData, 2048);
				// Recieve packet data
				ssock.receive(rec);
				byte[] receivedData = rec.getData();
				
				int seqNumber = getSeqNumber(receivedData);

				//Random generator
				double random = Math.random();
			
				// Get other headers
          		int checksum = getCheckSum(receivedData);
          		String dataPacketValue = getDataPacketIndicator(receivedData);
				
	          	//Check if dataPacket is a endpacket
	          	if (dataPacketValue.equals(expectedENDPacketValue)) {
          			//connection done sending file, connection over
          			System.out.println("Final Segment recieved");
          			loop = false;
	          	} else if (random > prob) {
	          		// Rest of the data, HAVE TO DO IT LIKE THIS OR YOU GET WEIRD BYTE CHARACTERS LEFT OVER THEN OUTPUT FILE WILL DIFFER!!!!
	          		// For reference on finding weird byte characters https://unix.stackexchange.com/questions/45711/diff-reports-two-files-differ-although-they-are-the-same
	          		// sed -n l filename --> will show you those characters
	          		byte[] data = new byte[rec.getLength() - 64];
          			System.arraycopy(receivedData, 64, data, 0, data.length);

					//System.out.println(dataPacketValue.equals(expectedDataPacketValue));
					if (computeChecksum(data, checksum) && expectedSeq == seqNumber && dataPacketValue.equals(expectedDataPacketValue)) {
						//should expect next sequence number now
						expectedSeq++;

						fw.write(new String(data));

						//Get IP and port to respond too
	          			InetAddress senderPort = rec.getAddress();
						int senderIP = rec.getPort();

						//send and generate ack to this IP and port
						generateAndSendACK(ssock, expectedSeq, senderIP, senderPort);
					} else {
						//Get IP and port to respond too
	          			InetAddress senderPort = rec.getAddress();
						int senderIP = rec.getPort();
						//an issue so do not generate new ack, just the one we need still
						generateAndSendACK(ssock, expectedSeq, senderIP, senderPort);
					}
					
				} else {
					//r <= prob so packet loss!!!!
					System.out.println("Packet loss, Sequence number = " + seqNumber);
				}
			}
			fw.close();
			System.out.println(filename + " has downloaded");		
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	private static void generateAndSendACK(DatagramSocket ssock, int seq, int senderPort, InetAddress senderIP) {
		try {
			//Make sequence number a binary string representation, needs padding to make 32 bits
			String binaryString = Integer.toBinaryString(seq);
			String pad = "";
			for(int i = 0; i < 32 - binaryString.length(); i++) {
				pad += "0";
			}
			binaryString = pad + binaryString;
			
			//append with rest of ack values
			String ackString = binaryString + ackPacket;

			//Turn it into byte array
			byte[] ackData = ackString.getBytes();

			//Make the ack packet
			DatagramPacket ack = new DatagramPacket(ackData, ackData.length, senderIP, senderPort);

			//Send to the client
			ssock.send(ack);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Used this for reference - https://stackoverflow.com/questions/4113890/how-to-calculate-the-internet-checksum-from-a-byte-in-java
	private static boolean computeChecksum(byte[] buf, int checksum) {
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

	    // See if they are the same 
	    return checksum == sum;

	}
	private static int getSeqNumber(byte[] array) {
		int s = Integer.parseInt(new String(Arrays.copyOfRange(array, 0, 32)),2); 
		//System.out.println(s);
		return s;
	}

	private static int getCheckSum(byte[] array) {
		String s = new String(Arrays.copyOfRange(array, 32, 48));
		//System.out.println(s);
		return Integer.parseInt(s, 2);
	}

	// Should be 0101010101010101 
	private static String getDataPacketIndicator(byte[] array) {
		String s = new String(Arrays.copyOfRange(array, 48, 64));	
		//System.out.println(s);
		return s;
	}
}