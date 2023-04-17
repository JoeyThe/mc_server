package rconclient;

import java.net.Socket;
import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Arrays;

public class RconClient {
	private Socket soc;
	private DataOutputStream out;
   	private DataInputStream in;

	final static int SERVERDATA_AUTH 	   = 3;
	final static int SERVERDATA_AUTH_RESPONSE  = 2;
	final static int SERVERDATA_EXECCOMMAND    = 2;
	final static int SERVERDATA_RESPONSE_VALUE = 0;

	final static int SIZE_SIZE 	= 4;
	final static int ID_SIZE 	= 4;
	final static int TYPE_SIZE 	= 4;
	final static int NULL_SIZES 	= 2;
	final static int MIN_PKT_SIZE 	= ID_SIZE + TYPE_SIZE + NULL_SIZES;

	final static int MAX_RESP_SIZE = 4096;

	private AtomicInteger pktId = new AtomicInteger(0);

	public RconClient() {
		try {
			soc = new Socket("localhost", 25575);
	        	out = new DataOutputStream(soc.getOutputStream());
     			in  = new DataInputStream(soc.getInputStream());
		} catch (Exception e) {
			e.printStackTrace();
		}

//		final int MIN_PKT_SIZE = ID_SIZE + TYPE_SIZE + TERM_SIZE;
	}

        public static void main(String[] args) {
		RconClient rc = new RconClient();
		try {
			byte[] command = {
				(byte) 0x11, (byte) 0x00, (byte) 0x00, (byte) 0x00,
				(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
				(byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x00,
				(byte) 0x70, (byte) 0x61, (byte) 0x73, (byte) 0x73, (byte) 0x77, (byte) 0x72, (byte) 0x64, (byte) 0x00,
				(byte) 0x00};
			byte[] query = new byte[256];

			ByteBuffer test = ByteBuffer.wrap(command);

//			System.out.println("Connecting socket");
//			Thread.sleep(2000);

			System.out.println("Getting socket data");
			rc.getData();
			Thread.sleep(2000);

			String cmd = "SeVeN_mc_890";
			rc.sendPacket(rc.SERVERDATA_AUTH, cmd);
			byte[] resp1 = new byte[rc.MAX_RESP_SIZE];
			resp1 = rc.readPacket(resp1);

			cmd = "whitelist list";
			rc.sendPacket(rc.SERVERDATA_EXECCOMMAND, cmd);
			byte[] resp2 = new byte[rc.MAX_RESP_SIZE];
			resp2 = rc.readPacket(resp2);


			rc.closeConnection();
			System.exit(0);

//			System.out.println("Getting response");
//			rc.printByteArray(rc.readResponse(query));
//			Thread.sleep(2000);
		} catch (Exception e) {
			e.printStackTrace();
		}
        }

	public void getData() {
		System.out.println("Bounded? "+		this.soc.isBound());
		System.out.println("Closed? "+		this.soc.isClosed());
		System.out.println("Connected? "+	this.soc.isConnected());
		System.out.println("Input shutdown? "+	this.soc.isInputShutdown());
		System.out.println("Output shutdown? "+	this.soc.isOutputShutdown());
	}

	public void sendPacket(int pktType, String body) {
		// Calculate packet size
                int pktSize  = this.MIN_PKT_SIZE + body.length();
                int buffSize = this.SIZE_SIZE + pktSize;
                // Create ByteBuffer object
                ByteBuffer pkt = ByteBuffer.allocate(buffSize);
                pkt.order(ByteOrder.LITTLE_ENDIAN);
                // Add body size to buffer
                pkt.putInt(pktSize);
                // Add packet id to buffer, increment id
                pkt.putInt(this.pktId.incrementAndGet());
                // Add packet type to buffer
                pkt.putInt(pktType);
                // Convert string to byte array
                pkt.put(body.getBytes());
                // Return the damn thing
		try {
			this.out.write(pkt.array());
			System.out.println("Packet SENT. Packet details:");
			this.printByteArray(pkt.array());
//			this.getPacketDetails(pkt.array());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public byte[] readPacket(byte[] pkt) {
		int noBytes;
		try {
			noBytes = this.in.read(pkt);
			pkt = Arrays.copyOf(pkt, noBytes);
			System.out.println("Packet READ. Packet details:");
			this.printByteArray(pkt);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return pkt;
	}

	public void closeConnection() {
		System.out.println("Closing connection");
		try {
			this.soc.close();
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

}

