package de.codesourcery.wotcompare;

import java.util.Arrays;

public enum TankType {

	LIGHT_TANK("lightTank"),
	MEDIUM_TANK("mediumTank"),
	HEAVY_TANK("heavyTank"),
	TANK_DESTROYER("AT-SPG"),
	SELF_PROPELLED_GUN("SPG");

	private final String id;

	private TankType(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public static TankType fromID(String id) {
		return Arrays.stream( values() ).filter( e -> e.id.equals( id ) ).findFirst().orElseThrow( () -> new IllegalArgumentException("Unknown tank type ID '"+id+"'" ) );
	}
}
