package de.codesourcery.wotcompare;

import java.awt.Color;
import java.util.Arrays;

public enum Nation
{
	USSR("ussr",Color.RED),
	GERMANY("germany",Color.BLACK),
	USA("usa",Color.BLUE),
	JAPAN("japan",Color.GREEN),
	UK("uk",Color.WHITE),
	FRANCE("france",Color.PINK ),
	CHINA("china",Color.YELLOW);

	private final String id;
	private final Color color;

	private Nation(String id,Color color) {
		this.id = id;
		this.color = color;
	}

	public Color getColor() {
		return color;
	}

	public String getId() {
		return id;
	}

	public static Nation fromID(String id) {
		return Arrays.stream( values() ).filter( e -> e.id.equals( id ) ).findFirst().orElseThrow( () -> new IllegalArgumentException("Unknown nation ID '"+id+"'" ) );
	}
}
