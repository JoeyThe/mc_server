import java.net.Socket;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Arrays;
import java.lang.String;
import java.nio.charset.StandardCharsets;

/**
 * This is an RCON client class
 * @author josep
 *
 */
public class RconClient {
	private Socket soc;
	private DataOutputStream out;
   	private DataInputStream in;

	public final static int SERVERDATA_AUTH 	  	= 3;
	public final static int SERVERDATA_AUTH_RESPONSE  	= 2;
	public final static int SERVERDATA_EXECCOMMAND    	= 2;
	public final static int SERVERDATA_RESPONSE_VALUE 	= 0;

	public boolean authenticated		 		= false;

	private final static int SIZE_SIZE 	= 4;
	private final static int ID_SIZE 	= 4;
	private final static int TYPE_SIZE 	= 4;
	private final static int NULL_SIZES 	= 2;
	private final static int MIN_PKT_SIZE 	= ID_SIZE + TYPE_SIZE + NULL_SIZES;

	private AtomicInteger pktId = new AtomicInteger(0);

        public final static int MAX_RESP_SIZE = 4096;

	public RconClient() {
		boolean connectionMade = false;
		while (!connectionMade) {
			try {
	                        soc = new Socket("localhost", 25575);
        	                out = new DataOutputStream(soc.getOutputStream());
                	        in  = new DataInputStream(soc.getInputStream());
				Logger.log("Connection to RCON server made!");
				connectionMade = true;
               		} catch (Exception e) {
                        	//e.printStackTrace();
				Logger.log("RCON connection not made! Waiting for server...");
				try {
					Thread.sleep(5000);
				} catch (Exception e2) {
					e2.printStackTrace();
				}
                	}
		}
	}

	public void sendPacket(int pktType, String body) {	
		// Calculate packet size
                int pktSize  = MIN_PKT_SIZE + body.length();
                int buffSize = SIZE_SIZE + pktSize;
                // Create ByteBuffer object
                ByteBuffer pkt = ByteBuffer.allocate(buffSize);
                pkt.order(ByteOrder.LITTLE_ENDIAN);
                // Add body size to buffer
                pkt.putInt(pktSize);
                // Add packet id to buffer, increment id
                pkt.putInt(pktId.incrementAndGet());
                // Add packet type to buffer
                pkt.putInt(pktType);
                // Convert string to byte array
                pkt.put(body.getBytes());
                // Return the damn thing
		try {
			// If authenticating client, call special method and don't print out message
			if (pktType == 3) {
				System.out.println("Attempting to authenticate RCON client...");
				out.write(pkt.array());
				return;
			}
			out.write(pkt.array());
			String msg = "";
			msg = msg + "Packet SENT. Packet details:\n";
			// Packet detail: Size
			msg += "\t> Size:  " + pktSize + "\n";
			// Packet detail: ID
			msg += "\t> Id:    " + pktId.get() + "\n";
			// Packet detail: Type
			msg += "\t> Type:  " + pktType + "\n";
			// Packet detail: body
			msg += "\t> Body:  " + body;
			Logger.log(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// Get the body of the response packet from sending a command
	public String readPacket() {
		byte[] pkt = new byte[MAX_RESP_SIZE];
		int noBytes;
		String body = "";
		try {
			noBytes = in.read(pkt);
			pkt = Arrays.copyOf(pkt, noBytes);
			String msg = "";
			msg += "Packet RECEIVED. Packet details:\n";
			// Packet detail: Size
			int pktSize = pkt[0] + (pkt[1] << 8) + (pkt[2] << 16) + (pkt[3] << 32);
		        msg += "\t> Size:  " + pktSize + "\n";
                        // Packet detail: ID
			int pktId = pkt[4] + (pkt[5] << 8) + (pkt[6] << 16) + (pkt[7] << 32);
                        msg += "\t> Id:    " + pktId + "\n";
                        // Packet detail: Type
			int pktType = pkt[8] + (pkt[9] << 8) + (pkt[10] << 16) + (pkt[11] << 32);
                        msg += "\t> Type:  " + pktType + "\n";
                        // Packet detail: body
			body = new String(Arrays.copyOfRange(pkt,12,pkt.length-2), StandardCharsets.UTF_8);
                        msg += "\t> Body:  " + body;
			Logger.log(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return body;
	}


	// Separate method for authenticating client to catch mis-authentication(?)
	public boolean authenticateClient(String pwrd) {
		Logger.log("Authenticating...");
		sendPacket(SERVERDATA_AUTH, pwrd);
		readPacket();
		// Do some check on the authenticate response
		setAuthentication(true);
		return getAuthentication();
	}

	public void closeConnection() {
		System.out.println("Closing connection");
		try {
			soc.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void printByteArray(byte[] bytesToPrint) {
		for (byte b : bytesToPrint) {
			System.out.print(String.format("%02x ", b));
		}
		System.out.println();
	}	

	public boolean getAuthentication() {
		return authenticated;
	}

	public void setAuthentication(boolean auth) {
		authenticated = auth;
	}
}

