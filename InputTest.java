import java.io.*;
import java.util.Vector;
import javax.bluetooth.*;
import lejos.nxt.*;
import lejos.nxt.comm.*;
import lejos.nxt.remote.*;

public class InputTest {
	public static void main(String args[]) {
		int speed = -1;
		boolean avoidance;
		DataInputStream dis = null;
		DataOutputStream dos = null;
		LCD.drawString("Connecting",0,0);
		BTConnection btc = Bluetooth.waitForConnection();
		try {
			dis = btc.openDataInputStream();
			dos = btc.openDataOutputStream();
		} catch (Exception e) {
			System.exit(1);
		}
		LCD.clear();
		LCD.drawString("Cmd:",0,1);
		LCD.drawString("Motor Speed:",0,2);
		LCD.drawString("Avoidance:",0,3);
		while(Button.readButtons() == 0) {
			try {
				LCD.drawInt(dis.available(),0,0);
				if(dis.available() > 0)
					switch(dis.readInt()) {
						case 0:
							LCD.drawInt(dis.readInt(),5,1);
						break;
						case 1:
							LCD.drawInt(dis.readInt(),13,2);
							dos.writeInt(2);
							dos.flush();
						break;
						case 2:
							if(dis.readBoolean())
								LCD.drawString("On",11,3);
							else
								LCD.drawString("Off",11,3);
							dos.writeInt(2);
							dos.flush();
						break;
						case 3:
							System.exit(0);
						break;
					}
			}
			catch (IOException e) {}
		}
	}
}