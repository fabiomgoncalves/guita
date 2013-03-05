import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;


public class Plugin {

	public static final int PORT_IN = 5658;
	
	public static void main(String[] args) throws ClassNotFoundException {
		try {
			String aux = "Modelo.java:27";
			Socket socket = new Socket("localhost", PORT_IN);
			ObjectOutputStream stream = new ObjectOutputStream(socket.getOutputStream());
			stream.writeObject(aux);
			
			stream.reset();
			stream.close();
			socket.close();

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
