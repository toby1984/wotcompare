package de.codesourcery.wotcompare;

public class ModuleId {

	private final long id;

	public ModuleId(long id) {
		this.id = id;
	}

	@Override
	public int hashCode() {
		return 31 + (int) (id ^ (id >>> 32));
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof ModuleId && ((ModuleId) obj).id == this.id;
	}

	@Override
	public String toString() {
		return Long.toString(id);
	}
}