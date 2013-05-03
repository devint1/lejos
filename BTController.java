//Created by Devin Tuchsen
//Version 1.0
//January 6, 2010
//This program controls a remote NXT rover. A wireless connection is made via Bluetooth to wirelessly communicate with and control it.
import java.io.*;
import java.util.Vector;
import javax.bluetooth.*;
import lejos.nxt.*;
import lejos.nxt.comm.*;
import lejos.nxt.remote.*;
import javax.microedition.lcdui.Graphics;

public class BTController
{
	public static void main(String args[]){
		int menu = 1;
		while(true) {
			switch(menu)
			{
				case 0:
					System.exit(0);
				break;
				case 1:
					menu = Menus.dispMain(); 
				break;
				case 2:
					menu = Menus.connect();
				break;
				case 3:
					menu = Menus.dispMotorSpeed();
				break;
				case 4:
					menu = Menus.dispAvoidance();
				break;
				case 5:
					menu = Menus.about();
				break;
			}
		}
	}
}

class Menus {
    static Controller cont = new Controller(200,true);
   
	public static int dispMain(){
		int menuItem = 2;
		boolean draw = false;
		LCD.clear();
		LCD.drawString("Main Menu",4,0);
		LCD.drawString(" Set Motor Speed",0,2);
		LCD.drawString(" Set Avoidance",0,3);
		LCD.drawString(" About",0,4);
		if(cont.isConnected()) { //If the NXT is connnected, display this in the menu.
			LCD.drawString(">Control",0,1);
			LCD.drawString("Connected to:",0,6);
			LCD.drawString("\"" + cont.getDeviceName() + "\"",0,7);
		}
		else //If not, display this.
			LCD.drawString(">Connect",0,1);
		LCD.refresh();
		while(true) {
			switch(Button.readButtons()) {
				case 4:
					menuItem++;
					draw = true;
					Button.RIGHT.waitForPressAndRelease();
				break;
				case 2:
					menuItem--;
					draw = true;
					Button.LEFT.waitForPressAndRelease();
				break;
				case 1:
					Button.ENTER.waitForPressAndRelease();
				return menuItem;
				case 8:
					if(cont.isConnected())
						cont.disconnect();
				return 0;
			}		
			if(draw) {
				switch(menuItem) {
					case 2:
						LCD.drawChar('>',0,8,false);
						LCD.drawChar(' ',0,16,false);
						LCD.drawChar(' ',0,32,false);
						draw = false;
					break;
					case 3:
						LCD.drawChar(' ',0,8,false);
						LCD.drawChar('>',0,16,false);
						LCD.drawChar(' ',0,24,false);
						draw = false;
					break;
					case 4:
						LCD.drawChar(' ',0,16,false);
						LCD.drawChar('>',0,24,false);
						LCD.drawChar(' ',0,32,false);
						draw = false;
					break;
					case 5:
						LCD.drawChar(' ',0,8,false);
						LCD.drawChar(' ',0,24,false);
						LCD.drawChar('>',0,32,false);
						draw = false;
					break;
					default:
						if(menuItem == 6)
							menuItem = 2;
						if(menuItem == 1)
							menuItem = 5;
					break;
				}
				LCD.refresh();
			}
		}
	}
	
	public static int connect() {
		boolean draw = false;
		int menuItem = 0;
		Vector devList = null;
		LCD.clear();
		if(!cont.isConnected()) { //If there is no connection, display the menu.
			LCD.drawString("Please Wait...",2,3);
			LCD.drawString("Loading devices.",0,4 );
			LCD.refresh();
			devList = cont.getDeviceList(); //Create a vector for the device list.
			LCD.clear();
			if(devList.size() > 0) { //If the vector contains something, list the contents.
				for(int i = 0; i < devList.size(); i++)
					LCD.drawString(" " + cont.getDeviceName(i),0,i+1);
				LCD.drawString("Select a Device:",0,0);
				LCD.drawChar('>',0,8,false);
			}
			else { //If not, display this error message.
				LCD.drawString("No known devices",0,1);
				LCD.drawString("Please pair",0,2);
				LCD.drawString("devices in the",0,3);
				LCD.drawString("NXT's Bluetooth",0,4);
				LCD.drawString("menu.",0,5);
			}
		}
		else { //If NXT is connected, display the control menu.
			cont.sendData(1,cont.getMotorSpeed()); //Send the motor speed to the remote NXT.
			cont.sendData(2,cont.getAvoidance()); //Send the avoidance.
			LCD.drawString("\"" + cont.getDeviceName() + "\"",0,0);
			LCD.drawString("Motor Speed:",0,1);
			LCD.drawString("Avoidance:",0,2);
			LCD.drawString("Status: Idle",0,3);
			LCD.drawString("<ENTER>+<LEFT>:",0,4);
			LCD.drawString("Main Menu",3,5);
			LCD.drawString("<ENTER>+<RIGHT>:",0,6);
			LCD.drawString("Disconnect",3,7);
			LCD.drawInt(cont.getMotorSpeed(),13,1);
			if(cont.getAvoidance())
				LCD.drawString("On",11,2);
			else
				LCD.drawString("Off",11,2);
			LCD.refresh();
		}
		LCD.refresh();
		while(true) {
			if(cont.isConnected()) {
				cont.drawStatus(false); //Display status if there is a change in it.
			}
			switch(Button.readButtons()) { //Handle actions while connected and while disconnected.
				case 4:
					if(!cont.isConnected()) {
						menuItem++;
						draw = true;
						Button.RIGHT.waitForPressAndRelease();
					}
					else {
						cont.sendData(0,3); //These methods tell the remote NXT to move and they also monitor the
											//status as long as the button is held down. Tells the NXT to stop once released.
						while(Button.RIGHT.isPressed())
							cont.drawStatus(3);
						cont.drawStatus(true);
						LCD.refresh();
						cont.sendData(0,4);
					}
				break;
				case 2:
					if(!cont.isConnected()) {
						menuItem--;
						draw = true;
						Button.LEFT.waitForPressAndRelease();
					}
					else {
						cont.sendData(0,2);
						while(Button.LEFT.isPressed())
							cont.drawStatus(2);
						cont.drawStatus(true);
						LCD.refresh();
						cont.sendData(0,4);
					}
					
				break;
				case 1:
					if(!cont.isConnected()) {
						Button.ENTER.waitForPressAndRelease();
						if(devList.size() == 0)
							break;
						if(!Bluetooth.getPower()) { //Turn on Bluetooth if it is not on already.
							LCD.clear();
							LCD.drawString("Bluetooth",3,2);
							LCD.drawString("powering on.",2,3);
							LCD.drawString("Please Wait...",2,4);
							LCD.refresh();
							Bluetooth.setPower(true);
						}
						LCD.clear();
						LCD.drawString("Connecting to:",1,2);
						LCD.drawString("\"" + cont.getDeviceName(menuItem) + "\"",7 - (cont.getDeviceName(menuItem)).length()/2,3);
						LCD.drawString("Please Wait...",2,4);
						LCD.refresh();
						cont.connect(menuItem); //Connect to the selected instance of the vector.
						if(cont.isConnected()) { //Display message if connection succeeded.
							LCD.clear();
							LCD.drawString("Connected to:",2,3);
							LCD.drawString("\"" + cont.getDeviceName(menuItem) + "\"",7 - (cont.getDeviceName(menuItem)).length()/2,4);
						}
						else { //Display message if connection failed.
							LCD.clear();
							LCD.drawString("Connection",3,3);
							LCD.drawString("failed!",5,4);
						}
						LCD.refresh();
						try{Thread.sleep(3000);} catch(Exception e) {} //Pause for 3 seconds.
						return 2;
					}
					else {
						cont.sendData(0,0);
						while(Button.ENTER.isPressed())
							cont.drawStatus(0);
						cont.drawStatus(true);
						LCD.refresh();
						cont.sendData(0,4);
						break;
					}
				case 8:
					if(!cont.isConnected()) {
						Button.ESCAPE.waitForPressAndRelease();
						return 1;
					}
					else {
						cont.sendData(0,1);
						while(Button.ESCAPE.isPressed())
							cont.drawStatus(1);
						cont.drawStatus(true);
						LCD.refresh();
						cont.sendData(0,4);
						break;
					}
				case 3:
					if(cont.isConnected()) 
						return 1;
					else
						break;
				case 5:
					if(cont.isConnected()) {
						cont.disconnect();
						return 1;
					}
					else
						break;
			}
			if(draw) {
				if(menuItem == devList.size())
					menuItem = 0;
				switch(menuItem) {
					case 0:
						LCD.drawChar('>',0,8,false);
						LCD.drawChar(' ',0,16,false);
						LCD.drawChar(' ',0,56,false);
						draw = false;
					break;
					case 1:
						LCD.drawChar(' ',0,8,false);
						LCD.drawChar('>',0,16,false);
						LCD.drawChar(' ',0,24,false);
						draw = false;
					break;
					case 2:
						LCD.drawChar(' ',0,16,false);
						LCD.drawChar('>',0,24,false);
						LCD.drawChar(' ',0,32,false);
						draw = false;
					break;
					case 3:
						LCD.drawChar(' ',0,24,false);
						LCD.drawChar('>',0,32,false);
						LCD.drawChar(' ',0,40,false);
						draw = false;
					break;
					case 4:
						LCD.drawChar(' ',0,32,false);
						LCD.drawChar('>',0,40,false);
						LCD.drawChar(' ',0,48,false);
					break;
					case 5:
						LCD.drawChar(' ',0,40,false);
						LCD.drawChar('>',0,48,false);
						LCD.drawChar(' ',0,56,false);
					break;
					case 6:
						LCD.drawChar(' ',0,8,false);
						LCD.drawChar(' ',0,48,false);
						LCD.drawChar('>',0,56,false);
					default:
						if(menuItem == 7)
							menuItem = 0;
						if(menuItem == -1)
							menuItem = devList.size() - 1;
					break;
				}
				LCD.refresh();
			}
		}		
	}
	
	public static int dispMotorSpeed() { //Since there is no keypad on the NXT, it is neccesary to implement another method of integer input.
		int menuItem = 0;
		boolean draw = false;
		char digit[] = {'0','0','0'}; //Create an array for the three digits of the motor speed.
		String val = Integer.toString(cont.getMotorSpeed()); //Create a string to serve as a conversion point for the char digits.
		if(cont.getMotorSpeed() >= 100) { //This ensures that data is used correctly without errors. 
			digit[0] = val.charAt(0);
			digit[1] = val.charAt(1);
			digit[2] = val.charAt(2);
		}
	 	else if(cont.getMotorSpeed() >= 10) {
			digit[1] = val.charAt(0);
			digit[2] = val.charAt(1);
	 	}
			
		else if(cont.getMotorSpeed() >= 1)
			digit[2] = val.charAt(0);
		LCD.clear();
		LCD.drawString("Set Motor Speed:",0,0); //Draw the menu.
		LCD.drawChar(digit[0],24,30,false);
		LCD.drawChar(digit[1],48,30,false);
		LCD.drawChar(digit[2],72,30,false);
		LCD.drawChar('^',24,38,false);
		LCD.refresh();
		while(true) {
			switch(Button.readButtons()) { //Input only allows for numerical chars and a maximum of 900 RPM.
				case 4:
					if(digit[menuItem] < '9' && (digit[0] != '9' || menuItem == 0))
						digit[menuItem]++;
					else if (digit[0] != '9' || menuItem == 0)
						digit[menuItem] = '0';
					LCD.drawChar(digit[menuItem],24 + menuItem*24,30,false);
					LCD.refresh();
					Button.RIGHT.waitForPressAndRelease();
				break;
				case 2:
					if(digit[menuItem] > '0' && (digit[0] != '9' || menuItem == 0))
						digit[menuItem]--;
					else if (digit[0] != '9' || menuItem == 0)
						digit[menuItem] = '9';
					LCD.drawChar(digit[menuItem],24 + menuItem*24,30,false);
					LCD.refresh();
					Button.LEFT.waitForPressAndRelease();
				break;
				case 1:
					Button.ENTER.waitForPressAndRelease();
					menuItem++;
					draw = true;
					if(menuItem == 1 && digit[0] == '9') {
						digit[1] = digit[2] = '0';
						LCD.drawChar('0',48,30,false);
						LCD.drawChar('0',72,30,false);
					}
					if(menuItem == 3) {
						val = "" + digit[0] + digit[1] + digit[2];
						cont.setMotorSpeed(Integer.parseInt(val));
						return 1;
					}
				break;
				case 8:
					Button.ESCAPE.waitForPressAndRelease();
					menuItem--;
					draw = true;
					if(menuItem == -1) {
						if(digit[0] == '9') {
							digit[1] = digit[2] = '0';
						}
						val = "" + digit[0] + digit[1] + digit[2];
						cont.setMotorSpeed(Integer.parseInt(val));
						return 1;
					}
				break;
			}
			if(draw) {
				switch(menuItem) {
					case 0:
						LCD.drawChar('^',24,38,false);
						LCD.drawChar(' ',48,38,false);
						LCD.drawChar(' ',72,38,false);
					break;
					case 1:
						LCD.drawChar(' ',24,38,false);
						LCD.drawChar('^',48,38,false);
						LCD.drawChar(' ',72,38,false);
					break;
					case 2:
						LCD.drawChar(' ',24,38,false);
						LCD.drawChar(' ',48,38,false);
						LCD.drawChar('^',72,38,false);
					break;
				}
				LCD.refresh();
			}
		}
	}
	
	public static int dispAvoidance() {
		boolean menuItem = cont.getAvoidance();
		boolean draw = true;
		LCD.clear();
		LCD.drawString("Set Avoidance:",1,0);
		LCD.drawString(" On",0,1);
		LCD.drawString(" Off",0,2);
		while(true) {
			switch(Button.readButtons()) {
				case 2:
					menuItem = !menuItem;
					draw = true;
					Button.LEFT.waitForPressAndRelease();
				break;
				case 4:
					menuItem = !menuItem;
					draw = true;
					Button.RIGHT.waitForPressAndRelease();
				break;
				case 1:
					Button.ENTER.waitForPressAndRelease();
					cont.setAvoidance(menuItem);
				return 1;
				case 8:	
					Button.ESCAPE.waitForPressAndRelease();
					cont.setAvoidance(menuItem);
				return 1;		
			}
			if(draw) {
				if(menuItem) {
					LCD.drawChar('>',0,8,false);
					LCD.drawChar(' ',0,16,false);
					draw = false;
				}
				else {
					LCD.drawChar(' ',0,8,false);
					LCD.drawChar('>',0,16,false);
					draw = false;
				}
				LCD.refresh();
			}
		}
	}
	
	public static int about() {
		LCD.clear();
		LCD.drawString("NXT BT Control",1,0);
		LCD.drawString("V1.0",6,1);
		LCD.drawString("Created by:",3,3);
		LCD.drawString("Devin Tuchsen",2,4);
		LCD.drawString("Copyright(c)2009",0,6);
		LCD.refresh();
		while(true) {
			if(Button.readButtons() == 1) {
				Button.ENTER.waitForPressAndRelease();
				return 1;
			}
			if(Button.readButtons() == 8) {
				Button.ESCAPE.waitForPressAndRelease();
				return 1;
			}
		}
	}
}

class Controller {
	private int motorSpeed;
	private int status;
	private int previous;
	private boolean avoidance;
	private boolean connected = false;
	private Vector devices = null;
	private RemoteDevice btrd = null;
	private BTConnection btc = null;
	private String name = null;
	private DataOutputStream dos = null;
	private DataInputStream dis = null;
	private Graphics g = new Graphics();
	
	public Controller(int x, boolean y) {
		motorSpeed = x;
		avoidance = y;
	}
	
	public void setMotorSpeed(int x) {
		motorSpeed = x;
	}
	
	public void setAvoidance(boolean x) {
		avoidance = x;
	}
	
	public int getMotorSpeed() {
		return motorSpeed;
	}
	
	public boolean getAvoidance() {
		return avoidance;
	}
	
	public Vector getDeviceList() {
		devices = Bluetooth.getKnownDevicesList();
		return devices;
	}
	
	public String getDeviceName(int ins) {
		return ((RemoteDevice)devices.elementAt(ins)).getFriendlyName(false);
	}
	
	public String getDeviceName() {
		return name;
	}
	
	public boolean connect(int ins) {
		name = ((RemoteDevice)devices.elementAt(ins)).getFriendlyName(false); //Save the name of the connected device.
		btrd = Bluetooth.getKnownDevice(name); //Create a Bluetooth remote device.
		btc = Bluetooth.connect(btrd); //Connect to the remote device.
		try { //Attempt to open I/O streams.
			dos = btc.openDataOutputStream(); 
			dis = btc.openDataInputStream();
			connected = true;
		} catch (Exception e) { //If I/O streams cannot be opened, the connection has failed.
			return false;
		}
		return true;
	}
	
	public void disconnect() {
		try {
			dos.writeInt(3); //Tell the remote NXT to exit program.
			dos.flush();
		}
		catch (IOException e) {}
		name = null;
		btrd = null;
		btc = null;
		dos = null;
		connected = false;
		LCD.clear();
		LCD.drawString("Disconnected.",2,3);
		LCD.refresh();
		try{Thread.sleep(3000);} catch(Exception e) {} //Pause for 3 seconds
	}
	
	public void sendData(int cmd, int param) {
		try { //Write two variables to the remote device.
			dos.writeInt(cmd);
			try{Thread.sleep(100);} catch(Exception e) {}
			dos.writeInt(param);
			dos.flush();
		}
		catch (IOException e) { //Display error message if it fails.
			LCD.drawString("IO ERROR",8,3);
		}
	}
	
	public void sendData(int cmd, boolean param) { //Same thing as before, but sends boolean data instad of int.
		try {
			dos.writeInt(cmd);
			try{Thread.sleep(100);} catch(Exception e) {}
			dos.writeBoolean(param);
			dos.flush();
		}
		catch (IOException e) {
			LCD.drawString("IO ERROR",8,3);
		}
	}
	
	//There are 3 values used to indicate status. The remote NXT will send this data depending on its conditions.
	//0: obstruction detected.
	//1: cliff detected.
	//2: idle/avoidance complete.
	public void drawStatus(boolean force) {
		try {
			if(dis.available() > 0)
				status = dis.readInt();
			if(status != previous || force) {
				switch(status) {
					case 0:
						LCD.drawString("Obst.   ",8,3);
					break;
					case 1:
						LCD.drawString("Cliff   ",8,3);
					break;
					case 2:
						LCD.drawString("Idle    ",8,3);
					break;
				}
				LCD.refresh();
				previous = status;
			}	
		} catch(Exception e) {
			LCD.drawString("IO ERROR",8,3);
		}
	}
	
	//Values greater than 2 are not sent by the navigator NXT. It is assumed that the NXT will execute directional action when a button is pressed.
	//If avoidance is triggered while in motion, however, it will send back a status message that will be read by the controller.
	public void drawStatus(int msg) { //Used only when directional buttons pressed.
		try {	
			if(dis.available() > 0)
				status = dis.readInt();
			else if(status >= 2)
				status = msg + 3;
			if(previous != status) {
				switch(status) {
					case 0:
						LCD.drawString("Obst.   ",8,3);
					break;
					case 1:
						LCD.drawString("Cliff   ",8,3);
					break;
					case 2:
						LCD.drawString("Idle    ",8,3);
					break;
					case 3:
						LCD.drawString("Forward ",8,3);
					break;
					case 4:
						LCD.drawString("Backward",8,3);
					break;
					case 5:
						LCD.drawString("Left    ",8,3);
					break;
					case 6:
						LCD.drawString("Right   ",8,3);
					break;
				}
				LCD.refresh();
				previous = status;
			}
		} catch(Exception e) {
			LCD.drawString("IO ERROR",8,3);
		}
	}
	
	public boolean isConnected() {
		return connected;
	}
}