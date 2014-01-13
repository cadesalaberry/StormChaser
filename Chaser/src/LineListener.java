/**
 * LineListener.java
 * 
 * This Interface allows us to stop monitoring the lineRead boolean in LineSensor.
 * Instead, every time the sensor sees a lineit calls the method lineIsRead() of every classes that implement it.
 * 
 * It has not been yet implemented. (we are missing the code for lineIsRead() in all the classes that implement it.
 * 
 * @author Charles-Antoine de Salaberry
 * @version 1
 * @
 */


public interface LineListener{
	
	/**
	 * Routine to follow when a Line Sensor sees a line.
	 */
	public void lineIsRead();
}
