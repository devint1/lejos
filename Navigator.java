import lejos.nxt.*;

public class Navigator
{
	public static void main(String args[])
	{
		UltrasonicSensor uss = new UltrasonicSensor(SensorPort.S2);
		TouchSensor ts = new TouchSensor(SensorPort.S1);
		for(int x = 0; x <= 500; x++) //This will allow the uss to get some sample senses.
			uss.getDistance();
		Movement.calibrateUSS(); //This will allow the other uss to get some sample senses.
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

class Movement
{
	static UltrasonicSensor uss = new UltrasonicSensor(SensorPort.S3);
	
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
	
	public static void turn(String s1, String s2)
	{
		LCD.clear();
		LCD.drawString(s1, (17-s1.length())/2, 3); //Display the message.
		LCD.drawString(s2, (17-s2.length())/2, 4);
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
	}
}
