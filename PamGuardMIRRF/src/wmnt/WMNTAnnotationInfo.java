package wmnt;

/**
 * Object containing species, callType and comment information to be passed to a MIRRF Classifier
 * whenever the table is updated.
 * @author Taylor LeBlond
 */
public class WMNTAnnotationInfo {
	
	public String species;
	public String callType;
	public String comment;
	
	/**
	 * Sets all variables to empty strings.
	 * @author Taylor LeBlond
	 */
	public WMNTAnnotationInfo() {
		species = "";
		callType = "";
		comment = "";
	}
	
	public WMNTAnnotationInfo(String species, String callType, String comment) {
		this.species = species;
		this.callType = callType;
		this.comment = comment;
	}
}