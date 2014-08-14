package de.codesourcery.wotcompare;

public enum LanguageId
{
	ENGLISH("en");

	private final String id;

	private LanguageId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}
}