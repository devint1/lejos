//Created by Devin Tuchsen
//Version 1.0
//January 6, 2010
//This program navigates an NXT rover. It operates in either of two modes: Bluetooth controlled or autonomous. In controlled mode, an NXT controller
//controls the robot. In autonomous mode, the robot moves without any input.
import java.io.*;
import java.util.Vector;
import javax.bluetooth.*;
import lejos.nxt.*;
import lejos.nxt.comm.*;
import lejos.nxt.remote.*;

public class BTNavigator
{
	public static void main(String args[])
	{
		int speed = -1;
		int direction = 4;
		int previous = 4;
		boolean avoidance = true;
		boolean stopped = false;
		boolean draw = true;
		boolean mode = true;
		boolean menuActive = true;
		DataInputStream dis = null;
		DataOutputStream dos = null;
	 	UltrasonicSensor uss = new UltrasonicSensor(SensorPort.S2);
		TouchSensor ts = new TouchSensor(SensorPort.S1);
		LCD.drawString("Select Mode",3,0);
	 	LCD.drawString(" Controlled",0,1);
	 	LCD.drawString(" Autonomous",0,2);
	 	LCD.refresh();
	 	while(menuActive) { //Display menu while it is active.
	 		switch(Button.readButtons()) {
	 			case 2:
	 				mode = !mode;
	 				draw = true;
	 				Button.LEFT.waitForPressAndRelease();
	 			break;
	 			case 4:
	 				mode = !mode;
	 				draw = true;
	 				Button.RIGHT.waitForPressAndRelease();
	 			break;
	 			case 1:
	 				menuActive = false;
	 				Button.ENTER.waitForPressAndRelease();
	 			break;
	 			case 8:
	 				System.exit(0);
	 			break;
	 		}
	 		if(draw) {
	 			if(mode) {
	 				LCD.drawChar('>',0,8,false);
	 				LCD.drawChar(' ',0,16,false);
	 			}
	 			else {
	 				LCD.drawChar(' ',0,8,false);
	 				LCD.drawChar('>',0,16,false);
	 			}
	 			LCD.refresh();
	 			draw = false;
	 		}
		}
		for(int x = 0; x <= 500; x++) //This will allow the uss to get some sample senses.
			uss.getDistance();
		Movement.calibrateUSS();
	 	if(mode) { //Do this if controlled mode selected.
	 		UltrasonicSensor uss1 = new UltrasonicSensor(SensorPort.S3);
	 		if(!Bluetooth.getPower()) { //If Bluetooth is off, turn it on.
				LCD.clear();
				LCD.drawString("Bluetooth",3,2);
				LCD.drawString("powering on.",2,3);
				LCD.drawString("Please Wait...",2,4);
				LCD.refresh();
				Bluetooth.setPower(true);
			}
	 		LCD.clear();
			LCD.drawString("Awating",4,3);
			LCD.drawString("Connection",3,4);
			LCD.refresh();
			BTConnection btc = Bluetooth.waitForConnection(); //Wait for an incoming connection.
			try { //Attempt to open I/O streams.
				dis = btc.openDataInputStream();
				dos = btc.openDataOutputStream();
			} catch (Exception e) { //If it fails, exit with error.
				System.exit(1);
			}
			LCD.clear();
			LCD.drawString("Connected",4,3);
			LCD.refresh();
			try{Thread.sleep(3000);} catch(Exception e) {} //Pause 3 seconds.
			LCD.clear();
			LCD.drawString("Status: Idle",0,0);
			LCD.drawString("Motor Speed:",0,1);
			LCD.drawString("Avoidance:",0,2);
			while(Button.readButtons() == 0) { //Keep running until a button is pressed.
				try {
					if(dis.available() > 0) //These programs use a two paramater transmission method. The controller sends two variables: a command,
											//and a value. The navigator reads and uses these.
						switch(dis.readInt()) { //Read the command variable.
							case 0: //0: movement command.
								previous = direction;
								direction = dis.readInt(); //Read the direction value.
							break;
							case 1: //1: motor speed command.
								speed = dis.readInt(); //Read the motor speed value.
								LCD.drawInt(speed,13,1);
								if(speed < 100)
									LCD.drawChar(' ',90,8,false);
								if(speed < 10)
									LCD.drawChar(' ',84,8,false);
								Movement.setSpeed(speed);
							break;
							case 2: //2: avoidance command.
								avoidance = dis.readBoolean(); //Read avoidance value.
								if(avoidance)
									LCD.drawString("On ",11,2);
								else
									LCD.drawString("Off",11,2);
							break;
							case 3: //3: controller disconnected, exit.
								System.exit(0);
							break;
						}
					if(direction != previous) {
						switch(direction) {
							case 0:
								LCD.drawString("Forward ",8,0);
							break;
							case 1:
								LCD.drawString("Backward",8,0);
							break;
							case 2:
								LCD.drawString("Left    ",8,0);
							break;
							case 3:
								LCD.drawString("Right   ",8,0);
							break;
							case 4:
								LCD.drawString("Idle    ",8,0);
							break;
						}
						LCD.refresh();
					}
					switch(direction) { //Process directional commands.
						case 0:
							stopped = false;
							if(avoidance) //Use avoidance if set.
								if(uss.getDistance() < 8) 
									if(!ts.isPressed()) 
										Movement.moveFwd();
									else {
										dos.writeInt(0);
										dos.flush();
										Movement.turn(0); //If there is an obstruction, turn around and display message.
										Movement.setSpeed(speed);
										dos.writeInt(2);
										dos.flush();
									}
								else {
									dos.writeInt(1);
									dos.flush();
									Movement.turn(1); //If there is a cliff, turn around and display message.
									Movement.setSpeed(speed);
									dos.writeInt(2);
									dos.flush();	
								}
							else //Ignore sensors if avoidance is off.
								Movement.moveFwd();
						break;
						case 1:
							stopped = false;
							if(avoidance) //Use avoidance if set.
								if(uss1.getDistance() < 15)
									Movement.moveBwd(); //Move backwards if there is no cliff.
								else {
									dos.writeInt(1);
									dos.flush();
									Movement.backTurn();
									Movement.setSpeed(speed);
									dos.writeInt(2);
									dos.flush();
								}
							else //Ignore sensors if avoidance is off.
								Movement.moveBwd();
						break;
						case 2:
							stopped = false;
							Movement.turnLeft();
						break;
						case 3:
							stopped = false;
							Movement.turnRight();
						break;
						case 4:
							if(!stopped) {
								Movement.stop();
								dos.writeInt(2);
								dos.flush();
								stopped = true;
							}
						break;
					}
				}
				catch (IOException e) {}
			}
		}
		else { //Run in autonomous mode.
			LCD.clear();
			Movement.setFwdSpeed();
			LCD.drawString("Moving forward.", 1, 3);
			while(Button.readButtons() == 0) //Go until an NXT button is pressed.
				if(uss.getDistance() < 8) 
					if(!ts.isPressed())
						Movement.moveFwd(); //Go forward if there is no obstruction or cliff.
					else
						Movement.turn("Obstruction", "detected!"); //If there is an obstruction, turn around and display message.
				else
					Movement.turn("Cliff detected!", ""); //If there is a cliff, turn around and display message.
		}
	}
}

class Movement
{
	static UltrasonicSensor uss = new UltrasonicSensor(SensorPort.S3);
	static UltrasonicSensor uss1 = new UltrasonicSensor(SensorPort.S2);
	
	public static void calibrateUSS()
	{
		for(int x = 0; x <= 500; x++)
			uss.getDistance();
	}
	
	public static void setFwdSpeed()
	{
		Motor.B.setSpeed(200);
		Motor.C.setSpeed(200);
	}
	
	public static void setBwdSpeed()
	{
		Motor.B.setSpeed(360);
        Motor.C.setSpeed(360);
	}
	
	public static void setSpeed(int s) {
		Motor.B.setSpeed(s);
        Motor.C.setSpeed(s);
	}
	public static void moveFwd()
	{
		Motor.B.forward();
        Motor.C.forward();
	}
	
	public static void moveBwd()
	{
		Motor.B.backward();
        Motor.C.backward();
	}
	
	public static void turnLeft() {
		Motor.B.forward();
		Motor.C.backward();
	}
	
	public static void turnRight() {
		Motor.B.backward();
		Motor.C.forward();
	}
	
	public static void stop() {
		Motor.B.stop();
		Motor.C.stop();
	}
	
	public static void turn(String s1, String s2)
	{
		LCD.clear();
		LCD.drawString(s1, (17-s1.length())/2, 3); //Display the message.
		LCD.drawString(s2, (17-s2.length())/2, 4);
		LCD.refresh();
		setBwdSpeed();
		for(int x = 0; x <= 1500; x++)
		{
			if(uss.getDistance() < 15)
				moveBwd(); //Move backwards if there is no cliff.
			else //If a cliff is detected while backing up, avoid it.
			{
				for(int y = 0; y <= 1500; y++)
					moveFwd();
				Motor.C.stop();
				Motor.C.resetTachoCount();
				while(Motor.C.getTachoCount() <= 240) //Turn 90 degrees.
				{	
					Motor.B.backward();
					Motor.C.forward();
				}
				setFwdSpeed();
				return;
			}
		}
		Motor.C.stop();
		Motor.C.resetTachoCount();
		while(Motor.C.getTachoCount() <= 240) //Turn 90 degrees.
		{	
			Motor.B.backward();
			Motor.C.forward();
		}
		setFwdSpeed();
  		LCD.clear();
		LCD.drawString("Moving forward.", 1, 3);
		LCD.refresh();
	}
	
	public static void turn(int s)
	{
		if(s == 0) 
			LCD.drawString("Obst.   ", 8, 0); //Display the message.
		if(s == 1)
			LCD.drawString("Cliff   ", 8, 0);
		LCD.refresh();
		setBwdSpeed();
		for(int x = 0; x <= 1500; x++)
		{
			if(uss.getDistance() < 15)
				moveBwd(); //Move backwards if there is no cliff.
			else //If a cliff is detected while backing up, avoid it.
			{
				for(int y = 0; y <= 1500; y++)
					moveFwd();
				Motor.C.stop();
				Motor.C.resetTachoCount();
				while(Motor.C.getTachoCount() <= 240) //Turn 90 degrees.
				{	
					Motor.B.backward();
					Motor.C.forward();
				}
				return;
			}
		}
		Motor.C.stop();
		Motor.C.resetTachoCount();
		while(Motor.C.getTachoCount() <= 240) //Turn 90 degrees.
		{	
			Motor.B.backward();
			Motor.C.forward();
		}
		LCD.drawString("Idle    ", 8, 0);
		LCD.refresh();
	}
	
	public static void backTurn()
	{
		LCD.drawString("Cliff   ", 8, 0);
		LCD.refresh();
		setBwdSpeed();
		for(int x = 0; x <= 1500; x++)
		{
			if(uss1.getDistance() < 8)
				moveFwd(); //Move backwards if there is no cliff.
			else //If a cliff is detected while backing up, avoid it.
			{
				for(int y = 0; y <= 1500; y++)
					moveBwd();
				Motor.C.stop();
				Motor.C.resetTachoCount();
				while(Motor.C.getTachoCount() <= 240) //Turn 90 degrees.
				{	
					Motor.B.backward();
					Motor.C.forward();
				}
				return;
			}
		}
		Motor.C.stop();
		Motor.C.resetTachoCount();
		while(Motor.C.getTachoCount() <= 240) //Turn 90 degrees.
		{	
			Motor.B.backward();
			Motor.C.forward();
		}
		LCD.drawString("Idle    ", 8, 0);
		LCD.refresh();
	}
}
