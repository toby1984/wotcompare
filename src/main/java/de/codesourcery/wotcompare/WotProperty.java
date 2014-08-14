package de.codesourcery.wotcompare;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public enum WotProperty
{
	TANK_BASE_WEIGHT(Unit.TONS),
	TANK_MAX_WEIGHT(Unit.TONS),
	TANK_BASE_ENGINE_POWER(Unit.ENGINE_POWER),
	TANK_MAX_HEALTH(Unit.HEALTH),
	VISION_RADIUS(Unit.METER),
	GUN_DAMAGE_MIN(Unit.DAMAGE),
	GUN_DAMAGE_MAX(Unit.DAMAGE),
	GUN_RATE(Unit.ROUNDS_PER_MINUTE),
	GUN_PENETRATION_MIN(Unit.MILLIMETER),
	GUN_PENETRATION_MAX(Unit.MILLIMETER),
	TURRET_ROTATION_SPEED(Unit.DEGREES_PER_MINUTE),
	TANK_ROTATION_SPEED(Unit.DEGREES_PER_MINUTE),
	TANK_SPEED(Unit.KM_PER_SECOND);

	private static final Set<WotProperty> TANK_PROPERTIES = Collections.unmodifiableSet( new HashSet<>(Arrays.asList(WotProperty.TANK_BASE_ENGINE_POWER,
			TANK_BASE_WEIGHT,
			TANK_MAX_WEIGHT,
			TANK_BASE_ENGINE_POWER,
			TANK_MAX_HEALTH,
			VISION_RADIUS,
			GUN_DAMAGE_MIN,
			GUN_DAMAGE_MAX,
			GUN_RATE,
			GUN_PENETRATION_MIN,
			GUN_PENETRATION_MAX,
			TURRET_ROTATION_SPEED,
			TANK_ROTATION_SPEED,
			TANK_SPEED
			)));

	private final Unit unit;

	private WotProperty(Unit unit)
	{
		this.unit = unit;
	}

	public Unit getUnit() {
		return unit;
	}

	public final ValueWithUnit value(Comparable value) {
		return new ValueWithUnit( unit , value);
	}

	public boolean isTankProperty() {
		return getTankProperties().contains(this);

	}

	public static Set<WotProperty> getTankProperties()
	{
		return TANK_PROPERTIES;
	}
}