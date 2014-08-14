package de.codesourcery.wotcompare;

public class ApplicationId {

	private final String value;

	private ApplicationId(String value) {
		if ( value == null || value.trim().isEmpty() ) {
			throw new IllegalArgumentException("Value must not be NULL/blank");
		}
		this.value = value;
	}

	public static ApplicationId createInstance(String value) {
		return new ApplicationId(value);
	}

	@Override
	public int hashCode() {
		return 31 + value.hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if ( obj instanceof ApplicationId) {
			return this.value.equals( ((ApplicationId) obj).value );
		}
		return false;
	}

	public String value() {
		return value;
	}

	@Override
	public String toString() {
		return value;
	}
}
