package backup;

//*******************************************************************************
//**********************************LOG******************************************
//March 15th - added convertToDistance,convertToNode, identify Node, Node class
//March 16th - added findPath, checkSurrounding, travelPath, resetPath
//March 18th - Debug
//*******************************************************************************
//*******************************************************************************

import java.util.ArrayList;

import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;

public class Navigator_V1 {

	private static Odometer odometer;
	private static NXTRegulatedMotor leftMotor = Motor.A;
	private static NXTRegulatedMotor rightMotor = Motor.B;
	private static RobotCtrl robotCtrl;

	// initialize the array for the size of the court
	private Navigator_V1.Node[][] nodes = new Navigator_V1.Node[12][12];
	private ArrayList<Navigator_V1.Node> path = new ArrayList<Navigator_V1.Node>();

	public Navigator_V1(RobotCtrl robotCtrl, Odometer odometer) {
		this.odometer = odometer;
		this.robotCtrl = robotCtrl;

	}

	public static void drive() {

		// reset the motors
		for (NXTRegulatedMotor motor : new NXTRegulatedMotor[] { leftMotor,
				rightMotor }) {
			motor.stop();
			motor.setAcceleration(1000);// 3000
		}

		// wait 5 seconds
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// there is nothing to be done here because it is not expected that
			// the odometer will be interrupted by another thread
		}

	}

	public int[][] toInt() {
		int[][] temp = new int[12][12];
		for (int i = 0; i < nodes.length; i++)
			for (int j = 0; j < nodes.length; j++)
				temp[i][j] = nodes[i][j].number;
		return temp;
	}

	public int[][] toDis() {
		int[][] temp = new int[12][12];
		for (int i = 0; i < nodes.length; i++)
			for (int j = 0; j < nodes.length; j++)
				temp[i][j] = nodes[i][j].distance;
		return temp;
	}

	/**
	 * Method that resets the playing field back to its original state by
	 * calling defineRestrictions
	 * 
	 * @return Void
	 * 
	 **/
	public void resetPath() {
		defineRestrictions();
	}

	/**
	 * Method that identifies the path in terms of nodes and then calls travelTo
	 * on the coordinates of each node
	 * 
	 * @return Void
	 * 
	 **/
	public void travelPath(Navigator_V1.Node destination) {

		Navigator_V1.Node current = searchNode(14);// nodes[9][10];

		Navigator_V1.Node temp = null;
		destination.distance = 253;
		// Looks at neighboring nodes and select the smallest distance
		findPath(current, destination);
		int start = optimalDistance(current);
		current.distance = 254;

		for (int i = start; i > 0; i--) {
			ArrayList<Node> surrounding = checkSurrounding(current);
			for (int j = 0; j < surrounding.size(); j++) {
				if (surrounding.get(j).distance == start
						|| surrounding.get(j).distance == 253) {
					temp = surrounding.get(j);

				}
			}
			// System.out.print(current.number+" ");
			path.add(temp);
			current = temp;
			start--;
		}

		for (int i = 0; i < path.size(); i++) {

			int[] distance = convertToDistance(path.get(i));
			System.out.print(distance[0] + "  ");
			robotCtrl.travelTo(distance[0], distance[1]);
		}
		// Puts nodes into arraylist in decending order

	}

	/**
	 * Method that searches for a node given its associated number
	 * 
	 * @return n
	 **/
	public Navigator_V1.Node searchDistanceNode(int distance) {
		Navigator_V1.Node n = null;
		for (int i = 0; i < nodes.length; i++)
			for (int j = 0; j < nodes.length; j++) {
				if (nodes[i][j].distance == distance)
					n = nodes[i][j];
			}
		return n;

	}

	/**
	 * Method that searches for a node given its associated number
	 * 
	 * @return n
	 * 
	 **/
	public Navigator_V1.Node searchNode(int number) {
		Navigator_V1.Node n = null;
		for (int i = 0; i < nodes.length; i++)
			for (int j = 0; j < nodes.length; j++) {
				if (nodes[i][j].number == number)
					n = nodes[i][j];
			}
		return n;

	}

	/**
	 * Creates path
	 * 
	 * @return void
	 **/
	public void findPath(Node start, Navigator_V1.Node finish) {
		// Returns node x and y of robot and destination
		int[] robot = identifyNode(start);// convertToNode(30,30).number;
		int[] destination = identifyNode(finish);

		// Determines the orientation of the system or path
		// if the destination is to the right or to the left
		// and on top or on the bottom of the robot, therefore
		// determining the oreintation of the scan
		int orientation = 3;
		if (destination[0] >= robot[0]) {
			if (destination[1] >= robot[1])
				orientation = 0;
			else
				orientation = 0;
		}
		if (destination[0] < robot[0]) {
			if (destination[1] <= robot[1])
				orientation = 2;
			else
				orientation = 1;
		}

		// Cases which vary depending on the type of scan determined above
		switch (orientation) {
		// right to left and down to up
		case 0:
			for (int i = 11; i > -1; i--)
				for (int j = 11; j > -1; j--) {
					if (nodes[i][j].distance != 0
							&& nodes[i][j].number != nodes[robot[0]][robot[1]].number
							&& nodes[i][j].distance != 253) {
						if (nodes[i][j].nextTo(finish))
							nodes[i][j].distance = 2;

						else if (!isIsolated(nodes[i][j]))
							nodes[i][j].distance = optimalDistance(nodes[i][j]) + 1;
					}
					if (!isIsolated(start))
						break;
				}
			for (int y = 0; y < 12; y++)
				for (int x = 0; x < 4; x++) {
					if (nodes[x][y].distance != 0
							&& nodes[x][y].number != nodes[robot[0]][robot[1]].number
							&& nodes[x][y].distance != 253) {
						if (!isIsolated(nodes[x][y]))
							nodes[x][y].distance = optimalDistance(nodes[x][y]) + 1;
					}
					if (!isIsolated(start))
						break;
				}
			break;
		// left to right and up to down
		case 1:
			for (int i = 0; i < 12; i++)
				for (int j = 1; j < 11; j++) {
					if (nodes[i][j].distance != 0
							&& nodes[i][j].number != nodes[robot[0]][robot[1]].number
							&& nodes[i][j].distance != 253) {
						if (nodes[i][j].nextTo(finish))
							nodes[i][j].distance = 2;
						else if (!isIsolated(nodes[i][j]))
							nodes[i][j].distance = optimalDistance(nodes[i][j]) + 1;
					}
					if (!isIsolated(start))
						break;
				}
			break;
		// left to right and down to up
		case 2:
			for (int i = 0; i < 12; i++)
				for (int j = 11; j > -1; j--) {
					if (nodes[i][j].distance != 0
							&& nodes[i][j].number != nodes[robot[0]][robot[1]].number
							&& nodes[i][j].distance != 253) {
						if (nodes[i][j].number == finish.number)
							nodes[i][j].distance = 253;
						else if (nodes[i][j].nextTo(finish))
							nodes[i][j].distance = 2;
						else if (!isIsolated(nodes[i][j]))
							nodes[i][j].distance = optimalDistance(nodes[i][j]) + 1;
					}
					if (!isIsolated(start))
						break;
				}
			for (int y = 0; y < 12; y++)
				for (int x = 8; x < 12; x++) {
					if (nodes[x][y].distance != 0
							&& nodes[x][y].number != nodes[robot[0]][robot[1]].number
							&& nodes[x][y].distance != 253) {
						if (!isIsolated(nodes[x][y]))
							nodes[x][y].distance = optimalDistance(nodes[x][y]) + 1;
					}
					if (!isIsolated(start))
						break;
				}
			break;
		// right to left and up to down
		case 3:
			for (int i = 11; i > -1; i--)
				for (int j = 1; j < 11; j++) {
					if (nodes[i][j].distance != 0
							&& nodes[i][j].number != nodes[robot[0]][robot[1]].number
							&& nodes[i][j].distance != 253) {
						if (nodes[i][j].nextTo(finish))
							nodes[i][j].distance = 2;
						else if (!isIsolated(nodes[i][j]))
							nodes[i][j].distance = optimalDistance(nodes[i][j]) + 1;
					}
					if (!isIsolated(start))
						break;
				}

			break;
		}

	}

	/**
	 * Returns the smallest distance contained within the 4 neighbor nodes
	 * 
	 * @return dis
	 **/
	public int optimalDistance(Navigator_V1.Node n) {
		int dis = 144;
		ArrayList<Node> next = checkSurrounding(n);

		for (int i = 0; i < next.size(); i++) {
			if (next.get(i).distance > 1 && next.get(i).distance <= dis)
				dis = next.get(i).distance;
		}

		return dis;
	}

	/**
	 * Checks to see if the node is isolated from other nodes along the path:
	 * one small logical error
	 * 
	 * @return isolated
	 **/
	public boolean isIsolated(Navigator_V1.Node n) {
		ArrayList<Navigator_V1.Node> surrounding = new ArrayList<Navigator_V1.Node>();
		boolean isolated = true;
		int[] location = identifyNode(n);
		if (location[0] + 1 < 12 && location[0] - 1 > -1) {
			surrounding.add(nodes[location[0] + 1][location[1]]);
			surrounding.add(nodes[location[0] - 1][location[1]]);
		}
		if (location[1] + 1 < 12 && location[1] - 1 > -1) {
			surrounding.add(nodes[location[0]][location[1] + 1]);
			surrounding.add(nodes[location[0]][location[1] - 1]);
		}
		for (int i = 0; i < surrounding.size(); i++) {
			if (surrounding.get(i).distance != 0
					&& surrounding.get(i).distance != 1)
				isolated = false;

		}
		return isolated;
	}

	/**
	 * Method that returns the nodes surrounding the current one
	 * 
	 * @return surrounding
	 * 
	 **/
	private ArrayList<Node> checkSurrounding(Navigator_V1.Node n) {
		ArrayList<Navigator_V1.Node> surrounding = new ArrayList<Navigator_V1.Node>();

		int[] location = identifyNode(n);
		if (location[0] + 1 < 12)
			surrounding.add(nodes[location[0] + 1][location[1]]);
		if (location[0] - 1 > 0)
			surrounding.add(nodes[location[0] - 1][location[1]]);
		if (location[1] + 1 < 12)
			surrounding.add(nodes[location[0]][location[1] + 1]);
		if (location[1] - 1 > 0)
			surrounding.add(nodes[location[0]][location[1] - 1]);
		return surrounding;
	}

	/**
	 * Method that defines the restrictions as to where the robot is allowed to
	 * go :Needs to complete for dispenser
	 * 
	 * @return void
	 **/
	public void defineRestrictions() {
		int num = 1;
		// Area surrounding the playing field
		for (int i = 0; i < nodes.length; i++)
			for (int j = 0; j < nodes.length; j++) {
				Navigator_V1.Node node;
				if (i < 2 || i > 9 || j == 0 || j == 11)
					node = new Navigator_V1.Node(0, num);
				else
					node = new Navigator_V1.Node(1, num);
				nodes[i][j] = node;
				num++;
			}
		// Defender's box
		int[] defenders = { 59, 58, 57, 56, 68, 69, 70, 71, 80, 81, 82, 83, 92,
				93, 94, 95 };
		for (int i = 0; i < defenders.length; i++) {
			searchNode(defenders[i]).distance = 0;
		}
		searchNode(9).distance = 1;
		searchNode(36).distance = 1;
	}

	/**
	 * Method that converts node into actual x and y distance
	 * 
	 * @return distance
	 **/
	private int[] convertToDistance(Navigator_V1.Node n) {
		int[] distance = identifyNode(n);
		distance[0] = (distance[0]) * 30 - 45;
		distance[1] = (distance[1]) * 30 - 45;
		return distance;
	}

	/**
	 * Method that identifies node depending one its assigned number Position[0]
	 * is x Position[1] is y
	 * 
	 * @return position
	 **/
	private int[] identifyNode(Navigator_V1.Node n) {
		int[] position = new int[2];
		position[0] = n.number / 12;
		position[1] = n.number % 12 - 1;
		if (position[1] < 0)
			position[1] = 0;
		return position;
	}

	/**
	 * Method that converts distance into node
	 * 
	 * @return nodes[x][y]
	 **/
	private Navigator_V1.Node convertToNode(double x, double y) {
		int xNode = 0;
		int yNode = 0;
		xNode = (int) x / 30;
		yNode = (int) y / 30;
		return nodes[xNode][yNode];
	}

	// *************************************************************
	// An inner class that represents the node.
	// 254 - Robot
	// 253 - Dispenser
	// 0 - inaccsessable or wall
	// 1 - Accessable route
	// Other - Number depend on distance away from objective
	// *************************************************************
	private class Node {
		public int distance;
		public int number;

		public Node(int d, int n) {
			distance = d;
			number = n;
		}

		public boolean nextTo(Navigator_V1.Node n) {
			boolean nextTo = false;
			if (Math.abs(number - n.number) == 1)
				nextTo = true;
			if (Math.abs(number - n.number) == 12)
				nextTo = true;
			return nextTo;
		}

	}

}
