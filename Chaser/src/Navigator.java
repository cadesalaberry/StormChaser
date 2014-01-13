//*******************************************************************************
//**********************************LOG******************************************
//March 15th - added convertToDistance,convertToNode, identify Node, Node class
//March 16th - added findPath, checkSurrounding, travelPath, resetPath
//March 18th - Debug
//*******************************************************************************
//*******************************************************************************
/*

 1       13      25      37      49      61      73      85      97      109     121     133

 2       14      26      38      50      62      74      86      98      110     122     134

 3       15      27      39      51      63      75      87      99      111     123     135

 4       16      28      40      52      64      76      88      100     112     124     136

 5       17      29      41      53      65      77      89      101     113     125     137

 6       18      30      42      54      66      78      90      102     114     126     138

 7       19      31      43      55      67      79      91      103     115     127     139

 8       20      32      44      56      68      80      92      104     116     128     140

 9       21      33      45      57      69      81      93      105     117     129     141

 10      22      34      46      58      70      82      94      106     118     130     142

 11      23      35      47      59      71      83      95      107     119     131     143

 12      24      36      48      60      72      84      96      108     120     132     144


 */
//Need to do:
//toIntersection()

//**If Statement in line 116 provides the "update" of the map**
//**if obstacle is detected, modify RobotCtrl to add the US  **

import java.util.ArrayList;

import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.Sound;

public class Navigator implements BluetoothReporter {

	private Odometer odometer;

	public RobotCtrl robotCtrl;
	private String status;
	private int w1;
	private int w2;
	// initialize the array for the size of the court
	private Node[][] nodes = new Node[12][12];
	public ArrayList<Node> path = new ArrayList<Node>();
	private ArrayList<Integer> Objects = new ArrayList<Integer>();

	boolean US = false;
	public static Node dispenserNode;

	private int index = 0;
	public int[] Shoot;
	public int[] ShootLeft = { 43, 32, 116};
	public int[] ShootRight = {116, 103, 32};
	

	public Navigator(RobotCtrl robotCtrl, Odometer odometer) {
		this.odometer = odometer;
		this.robotCtrl = robotCtrl;
		w1 = Chaser.DEFENSE_ZONE_X;
		w2 = Chaser.DEFENSE_ZONE_Y;
		
		if (-1 <= Chaser.DISPENSER_X && Chaser.DISPENSER_X <=5)
			Shoot = ShootLeft;
		else
			Shoot = ShootRight;

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
		defineRestrictions(false);
	}

	/**
	 * Method that identifies the path in terms of nodes and then calls travelTo
	 * on the coordinates of each node
	 * 
	 * @return Void
	 * 
	 **/
	public void travelPath(Node destination) {
		//Sound.beep();
		Node current = robotNode();//
		System.out.println(robotNode().number);
		path.clear();
		Node temp = null;

		// Looks at neighboring nodes and select the smallest distance
		findPath(current, destination);
		int start = optimalDistance(current);
		searchNode(destination.number).distance = 253;
		// int[] currentDistance = convertToDistance(current);
		// robotCtrl.travelTo(currentDistance[0],currentDistance[1]);
		for (int i = start; i > 0; i--) {
			if (start == 1)
				start = 253;
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
		System.out.print(current.number+" ");
		//for (int i = 0; i < path.size(); i++) {
		 //System.out.print(path.get(i).number+" "); }

		for (int i = 0; i < path.size(); i++) {

			int[] distance = convertToDistance(path.get(i));

			//
			//System.out.println(path.get(i).number);
			if (!robotCtrl.travelTo(distance[0], distance[1]))// If object is
			{
				Objects.add(path.get(i).number);
				if (path.get(i).distance == 253) {
					System.out.println("ERRROOOORRRR HERE!!!!");
					path.clear();
					defineRestrictions(false);
					index++;
					travelPath(searchNode(Shoot[index]));
					
					break;
				}

				defineRestrictions(false);
				travelPath(destination);
				break;
			}

		}
		// Puts nodes into arraylist in decending order

	}

	private Node robotNode() {
		Node robot = convertToNode(odometer.getX(), odometer.getY());

		// robot.distance = 254;

		return robot;
	}

	/**
	 * Method that searches for a node given its associated number
	 * 
	 * @return n
	 **/
	public Node searchDistanceNode(int distance) {
		Node n = null;
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
	public Node searchNode(int number) {
		Node n = null;
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
	public void findPath(Node start, Node finish) {
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
	public int optimalDistance(Node n) {
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
	public boolean isIsolated(Node n) {
		ArrayList<Node> surrounding = new ArrayList<Node>();
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
	private ArrayList<Node> checkSurrounding(Node n) {
		ArrayList<Node> surrounding = new ArrayList<Node>();

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
	public void defineRestrictions(boolean defender) {
		int num = 1;
		int access;
		path.clear();

			access = 0;
		// Area surrounding the playing field
		for (int i = 0; i < nodes.length; i++)
			for (int j = 0; j < nodes.length; j++) {
				Node node;
				if (i < 2 || i > 9 || j == 0 || j == 11)
					node = new Node(0, num);
				else
					node = new Node(1, num);
				nodes[i][j] = node;
				num++;
			}

		// Defender's box
		if (!defender) {
			for (int j = 11; j > 11 - w2; j--)
				for (int i = (12 - w1) / 2; i < (12 + w1) / 2; i++) {
					nodes[i][j].distance = 0;
				}
			}
		else{
			searchNode(71).distance=0;
			searchNode(70).distance=0;
			searchNode(83).distance=0;
			searchNode(83).distance=0;
			
		}
			if (Objects.size() != 0)
				for (int i = 0; i < Objects.size(); i++) {
					searchNode(Objects.get(i)).distance = 0;
				}
		

	}

	/**
	 * Method that converts node into actual x and y distance
	 * 
	 * @return distance
	 **/
	private int[] convertToDistance(Node n) {
		int[] distance = identifyNode(n);
		distance[0] = (distance[0]) * 30 - 15;
		distance[1] = (distance[1]) * 30 - 15;
		return distance;
	}

	/**
	 * Method that identifies node depending one its assigned number Position[0]
	 * is x Position[1] is y
	 * 
	 * @return position
	 **/
	private int[] identifyNode(Node n) {
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
	public Node convertToNode(double x, double y) {
		int xNode = 0;
		int yNode = 0;
		xNode = (int) ((x / 30.0));
		yNode = (int) ((y / 30.0));
		if (x >= 0 || x <= -1)
			xNode += 1;
		if (y >= 0 || y <= -1)
			yNode += 1;
		if (xNode > 11)
			xNode = 11;
		if (yNode > 11)
			yNode = 11;

		// System.out.println("xNd:" + xNode + "yNd:" + yNode);
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

		public boolean nextTo(Node n) {
			boolean nextTo = false;
			if (Math.abs(number - n.number) == 1)
				nextTo = true;
			if (Math.abs(number - n.number) == 12)
				nextTo = true;
			return nextTo;
		}

	}

	/**
	 * method to find the node to navigate to around the dispenser, in order to
	 * launch the press
	 */
	public void dispenserNode() {
		System.out.println("TEMP: ");
		int NodeNumber = 1;
		double angle = 0;
		int dispenser_X=0, dispenser_Y=0;
		dispenser_X = Chaser.DISPENSER_X * 30;
		dispenser_Y = Chaser.DISPENSER_Y * 30;

		Node temp = convertToNode(dispenser_X, dispenser_Y);

		if (1 <= temp.number && temp.number <= 12
				&& Chaser.DISPENSER_ORIENTATION == 2) {
			NodeNumber = temp.number + 12; // +11;
			angle = 180;
		}

		if (133 <= temp.number && temp.number <= 143
				&& Chaser.DISPENSER_ORIENTATION == 4) {
			NodeNumber = temp.number - 12; // -12;
			angle = 0;
		}

		if (temp.number % 12 == 1 && Chaser.DISPENSER_ORIENTATION == 1) {
			NodeNumber = temp.number + 1;// +1;
			angle = 270;
		}

		if (temp.number % 12 == 0 && Chaser.DISPENSER_ORIENTATION == 3) {
			NodeNumber = temp.number - 1;// - 13;
			angle = 90;
		}

		System.out.println("NodeNbr: " + NodeNumber);
		dispenserNode = searchNode(NodeNumber);
		dispenserNode.distance = 1;
		System.out.println("dist" + dispenserNode.number);
		path.clear();
		defineRestrictions(false);
		travelPath(dispenserNode);
		robotCtrl.turnTo(angle);
	}

	/** Method to find the navigate to the available shooting point */
	public boolean shootNode() {
		
		defineRestrictions(false);
		path.clear();
		travelPath(searchNode(Shoot[index]));
		
		robotCtrl.turnTo(90);
		System.out.println("NODE:" + Shoot[index]);
		robotCtrl.face(150, 270);
		return true;
	}

	public String reportStatus(boolean getName) {

		if (getName) {

			status = new String("Field");

		} else {

			status = new String("");

			int[][] temp = toInt();
			int[][] temp2 = toDis();

			for (int i = 0; i < temp.length; i++) {

				status += "\n\n";

				for (int j = 0; j < temp.length; j++) {
					status += "\t" + temp[j][i];
				}
			}
			status += "\n";

			for (int i = 0; i < temp2.length; i++) {

				status += "\n\n";
				for (int j = 0; j < temp2.length; j++) {
					status += "\t" + temp2[j][i];

				}
			}
		}
		return status;
	}

}
