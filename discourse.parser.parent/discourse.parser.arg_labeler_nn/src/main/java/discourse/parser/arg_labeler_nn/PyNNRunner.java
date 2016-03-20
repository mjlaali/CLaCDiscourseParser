package discourse.parser.arg_labeler_nn;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.ProcessBuilder.Redirect;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

public class PyNNRunner {

	static Process executeWithArgs(String[] Args) {
		ProcessBuilder builder = new ProcessBuilder("sudo", "/usr/bin/python", "keras_model.py", Args[0], Args[1], Args[2],
				Args[3]).redirectErrorStream(true)
						.redirectOutput(Redirect.INHERIT)
						.redirectInput(Redirect.INHERIT);
		Process pyNNProcess = null;
		try {
			pyNNProcess = builder.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return pyNNProcess;
	}
	
	static Socket pyNNSocketConn = null;
	public static BufferedReader sin = null;
	public static PrintWriter sout = null;

	static void connectToSocket(String hostname, int port) {
		if (hostname == null) {
			try {
				hostname = InetAddress.getLocalHost().getHostName();
			} catch (UnknownHostException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		try {
			pyNNSocketConn = new Socket(hostname, port);
			sout = new PrintWriter(pyNNSocketConn.getOutputStream(), true);
			sin = new BufferedReader(new InputStreamReader(pyNNSocketConn.getInputStream()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	static void disconnectFromSocket() {
		sout.println("exit");
		try {
			pyNNSocketConn.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		// executeWithArgs(new String[]{"--train",
		// "outputs/resources/package/training-data.DNN", "--model-file",
		// "outputs/resources/package/model.DNN"});
/*		try {
			
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	*/
		Process pyProcess = executeWithArgs(new String[] { "--test", "conditionubuntu:77", "--model-file", "outputs/resources/package/model.h5" });
		try {
			pyProcess.waitFor(30, TimeUnit.SECONDS);
		} catch (InterruptedException ex) {
			System.out.println("INTERRUPTED");
		}
		connectToSocket("conditionubuntu", 77);

	}
}