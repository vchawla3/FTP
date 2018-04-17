public class Client {
	
	static String serverHost;
	static String serverPort;
	static String filename;
	static int windowSizeN;
	static int mss;

	static int sequenceNum = 0;
	static int sequenceMax;
	public static void main(String args[]) {
		try {
			serverHost = args[0];
			serverPort = args[1];
			filename = args[2];
			windowSizeN = Integer.parseInt(args[3]);
			mss = Integer.parseInt(args[4]);
			sequenceMax = windowSizeN + 1;

			rdt_send();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private static void rdt_send() {

	}
}