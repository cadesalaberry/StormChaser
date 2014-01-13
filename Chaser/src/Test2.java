import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * 
 * @author Tedderic
 */
public class Test2 {

	private static NXTRegulatedMotor leftMotor = Motor.A;
	private static NXTRegulatedMotor rightMotor = Motor.B;

	public static void main(String[] args) {

		Odometer o = new Odometer(Motor.A, Motor.B);
		RobotCtrl robotCtrl = new RobotCtrl(o, leftMotor, rightMotor, 16.1,
				2.67, 2.67);
		Navigator n = new Navigator(robotCtrl, o);
		n.defineRestrictions(false);
		n.travelPath(n.searchNode(45));
		
		//robotCtrl.goToIntersect();
		//robotCtrl.dispenser();
		//robotCtrl.fromIntersectToNode();
		
		
		/*
		 * int[][] temp = n.toInt(); int[][] temp2 = n.toDis(); for(int i=0;
		 * i<temp.length;i++) { System.out.println(); System.out.println();
		 * for(int j=0; j<temp.length;j++) { System.out.print(temp[j][i] +
		 * "    ");
		 * 
		 * } } System.out.println("\n"); for(int i=0; i<temp2.length;i++) {
		 * System.out.println(); System.out.println(); for(int j=0;
		 * j<temp2.length;j++) { System.out.print(temp2[j][i] + "    ");
		 * 
		 * } }
		 */

		// System.out.println(n.findPath(n.searchNode(45),n.searchNode(1)));
	}

	private static void goToIntersect() {
		// TODO Auto-generated method stub
		
	}
}
