import javax.bluetooth.*;
import lejos.nxt.comm.*;

public class SetNavName{
	public static void main(String args[]){
		Bluetooth.setFriendlyName("Navigator");
	}
}