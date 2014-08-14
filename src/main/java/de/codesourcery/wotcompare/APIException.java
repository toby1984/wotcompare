package de.codesourcery.wotcompare;

import org.apache.commons.lang.StringUtils;

public class APIException extends RuntimeException {

	private final String msg;
	private final String field;
	private final String value;

	public APIException(String msg, String field, String value) {
		super( createMessage(msg,field,value ) );
		this.msg = msg;
		this.field = field;
		this.value = value;
	}

	public String getServerMessage() {
		return msg;
	}

	public String getField() {
		return field;
	}

	public String getValue() {
		return value;
	}

	private static String createMessage(String msg,String field,String value)
	{
		String result = StringUtils.isBlank( msg ) ? "<no message>" : msg;
		if ( StringUtils.isNotBlank( field ) || StringUtils.isNotBlank( value ) ) {
			result = result + " ( ";
			if ( StringUtils.isNotBlank(field ) ) {
				result += "field: "+field;
			}
			if ( StringUtils.isNotBlank( value) )
			{
				if ( StringUtils.isNotBlank(field) ) {
					result += " , ";
				}
				result += "value: "+value;
			}
			result += " )";
		}
		return result;
	}
}