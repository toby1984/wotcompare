package de.codesourcery.wotcompare;

public class Module {

	private final ModuleId id;
	private final ModuleType type;

	public Module(ModuleId id,ModuleType type) {
		if (type == null) {
			throw new IllegalArgumentException("type must not be NULL");
		}
		if ( id == null ) {
			throw new IllegalArgumentException("id must not be NULL");
		}
		this.id = id;
		this.type = type;
	}

	public ModuleId getId() {
		return id;
	}

	public ModuleType getType() {
		return type;
	}

	public boolean hasType(ModuleType t) {
		return t.equals( this.type );
	}

	@Override
	public int hashCode() {
		return 31 + id.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof Module && ((Module) obj).id == this.id;
	}

	@Override
	public String toString() {
		return "Module [id=" + id + ", type=" + type + "]";
	}
}