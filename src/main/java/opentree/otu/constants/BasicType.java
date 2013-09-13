package opentree.otu.constants;

/**
 * Specifies several basic types that are used for looking up object types on property import.
 */
public enum BasicType {

	BOOLEAN (Boolean.class),
	INTEGER (Integer.class),
	DECIMAL (Double.class),
	STRING (String.class);
	
	public final Class<?> type;
	
	BasicType(Class<?> type) {
		this.type = type;
	}
}
