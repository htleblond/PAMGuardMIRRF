package wmat;

/**
 * Object containing species, callType and comment information to be passed to a MIRFEE Classifier
 * whenever the table is updated.
 * @author Holly LeBlond
 */
public class WMATAnnotationInfo {
	
	public String species;
	public String callType;
	public String comment;
	
	/**
	 * Sets all variables to empty strings.
	 * @author Holly LeBlond
	 */
	public WMATAnnotationInfo() {
		species = "";
		callType = "";
		comment = "";
	}
	
	public WMATAnnotationInfo(String species, String callType, String comment) {
		this.species = species;
		this.callType = callType;
		this.comment = comment;
	}
}