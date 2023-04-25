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
import java.lang.String;
import java.nio.charset.StandardCharsets;

import logger.Logger;

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

	private AtomicInteger pktId = new AtomicInteger(0);

        final static int MAX_RESP_SIZE = 4096;

	public RconClient() {
		try {
			soc = new Socket("localhost", 25575);
	        	out = new DataOutputStream(soc.getOutputStream());
     			in  = new DataInputStream(soc.getInputStream());
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
			String msg = "";
			msg = msg + "Packet SENT. Packet details:\n";
			// Packet detail: Size
			msg += "\t> Size:  " + pktSize + "\n";
			// Packet detail: ID
			msg += "\t> Id:    " + pktId.get() + "\n";
			// Packet detail: Type
			msg += "\t> Type:  " + pktType + "\n";
			// Packet detail: body
			if (pktType == 3) {
				msg += "\t> Body:  " + body.replaceAll(".","*") + "\n";
			} else {
				msg += "\t> Body:  " + body;
			}
			Logger.log(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public byte[] readPacket(byte[] pkt) {
		int noBytes;
		try {
			noBytes = this.in.read(pkt);
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
			String body = new String(Arrays.copyOfRange(pkt,12,pkt.length-2), StandardCharsets.UTF_8);
                        msg += "\t> Body:  " + body;
			Logger.log(msg);
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

