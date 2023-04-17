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

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

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
			ByteBuffer cmdPkt = rc.genPacket(rc.SERVERDATA_AUTH, cmd);
			rc.sendCmdPkt(cmdPkt);

			byte[] resp = new byte[rc.MAX_RESP_SIZE];
			resp = rc.recResp(resp);
			rc.printByteArray(resp);

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

	public void sendCommand(byte[] command) {
		System.out.println("Sending command");
		try {
			this.out.write(command);
		} catch (IOException e) {
			//e.printStackTrace();
			System.out.print("FAILURE: Write for command failed! Bytes for attempted command: ");
			this.printByteArray(command);
		}
	}

	public byte[] recResp(byte[] resp) {
		try {
			this.in.read(resp);

		} catch (IOException e) {
			e.printStackTrace();
		}
		return resp;
	}

	public ByteBuffer genPacket(int pktType, String command) {
		// Calculate packet size
		int pktSize  = this.MIN_PKT_SIZE + command.length();
		int buffSize = this.SIZE_SIZE + pktSize;
		// Create ByteBuffer object
		ByteBuffer cmdPkt = ByteBuffer.allocate(buffSize);
		cmdPkt.order(ByteOrder.LITTLE_ENDIAN);	
		// Add command size to buffer
		cmdPkt.putInt(pktSize);
		// Add packet id to buffer, increment id
		cmdPkt.putInt(this.pktId.incrementAndGet());
		// Add packet type to buffer
		cmdPkt.putInt(pktType);
//		System.out.print("Buffer before command: ");
		this.printByteArray(cmdPkt.array());
		// Convert string to byte array
//		System.out.print("Command string to bytes: ");
		this.printByteArray(command.getBytes());
		cmdPkt.put(command.getBytes());
		// Return the damn thing
		return cmdPkt;
	}

	public void sendCmdPkt(ByteBuffer cmdPkt) {
		System.out.println("Sending command");
		try {
			this.out.write(cmdPkt.array());
		} catch (IOException e) {
			System.out.println("FAILURE: Command failed to send!");
			e.printStackTrace();
		}
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

