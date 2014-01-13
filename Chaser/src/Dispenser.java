/**
 * 
 * @author Charles-Antoine de Salaberry, Jonathan Cheelip
 * @version 1
 * 
 */
public class Dispenser {

	private RobotCtrl robotCtrl;

	/**
	 * Gives the control of StormChaser.
	 * @param robotCtrl
	 */
	public Dispenser(RobotCtrl robotCtrl) {

		this.robotCtrl = robotCtrl;

	}

	/**
	 * Presses the button of the dispenser by simply going forward 37 centimeters, then backward 32.
	 */
	public void press() {

		robotCtrl.setRobotSpeed(100);

		robotCtrl.goForward(37);

		try {
			Thread.sleep(3000);
		} catch (Exception e) {
		}

		robotCtrl.goBackward(32);

	}
	
	/**
	 * We had to modify the code: at first the dispenser was on a line.
	 * It was then moved to the middle of a tile.
	 * We thus kept the code in case of another change.
	 * 
	 */
	public void pressOnLine() {

		robotCtrl.goToIntersect();
		press();
		robotCtrl.fromIntersectToNode();

	}
}