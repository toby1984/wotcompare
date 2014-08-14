package de.codesourcery.wotcompare;

import org.apache.commons.lang.StringUtils;

public class APIMethod {

	private final Category category;
	private final String methodName;

	public static enum Category
	{
		ENCYCLOPEDIA("encyclopedia");

		private final String id;

		private Category(String id) {
			this.id = id;
		}

		public String id() { return id; }
	}

	public APIMethod(Category category, String methodName) {
		if ( category == null ) {
			throw new IllegalArgumentException("category must not be NULL");
		}
		if ( StringUtils.isBlank(methodName) ) {
			throw new IllegalArgumentException("methodName must not be NULL/blank");
		}
		this.category = category;
		this.methodName = methodName;
	}

	public static APIMethod create(Category category,String methodName) {
		return new APIMethod(category,methodName);
	}

	@Override
	public int hashCode() {
		final int result = 31 + category.hashCode();
		return 31 * result +methodName.hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if ( obj instanceof APIMethod) {
			final APIMethod other = (APIMethod) obj;
			return category == other.category && this.methodName.equals(other.methodName);
		}
		return false;
	}

	@Override
	public String toString() {
		return "APIMethod [category=" + category + ", methodName=" + methodName+ "]";
	}

	public String getMethodName() {
		return methodName;
	}

	public Category getCategory() {
		return category;
	}
}