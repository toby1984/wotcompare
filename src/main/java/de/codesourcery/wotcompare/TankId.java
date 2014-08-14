package de.codesourcery.wotcompare;

public class TankId {

	private final long id;

	public TankId(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

	@Override
	public int hashCode() {
		return 31 + (int) (id ^ (id >>> 32));
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof TankId && ((TankId) obj).id == this.id;
	}

	@Override
	public String toString() {
		return Long.toString(id);
	}
}