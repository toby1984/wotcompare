package de.codesourcery.wotcompare;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;


public class Tank
{
	private final TankId id;
	private final Nation nation;
	private final int tier;
	private final boolean isPremium;
	private final String name;
	private final String shortName;
	private final TankType type;

	private Tank(TankId id, String name, String shortName, TankType type,Nation nation, int tier, boolean isPremium)
	{
		if (StringUtils.isBlank( name) ) {
			throw new IllegalArgumentException("name must not be NULL/blank");
		}
		if (StringUtils.isBlank( shortName ) ) {
			throw new IllegalArgumentException("name must not be NULL/blank");
		}
		this.id = id;
		this.name = name;
		this.shortName = shortName;
		this.type = type;
		this.nation = nation;
		this.tier = tier;
		this.isPremium = isPremium;
	}

	@Override
	public String toString() {
		return "ID="+id+",type="+type+" , tier="+tier+", nation="+nation+", name="+name;
	}

	public static Tank parse(JSONObject data) {
		/*
		 "14337": {
		            "nation_i18n": "U.S.S.R.",
		            "name": "#ussr_vehicles:Object263",
		            "level": 10,
		            "nation": "ussr",
		            "is_premium": false,
		            "type_i18n": "Tank Destroyer",
		            "short_name_i18n": "Obj. 263",
		            "name_i18n": "Object 263",
		            "type": "AT-SPG",
		            "tank_id": 14337
		        },
			 */

		// NOT PARSED: nation_i18n
		// NOT PARSED: name
		final int tier = data.getInt("level");
		final Nation nation = Nation.fromID( data.getString("nation" ) );
		final boolean isPremium = data.getBoolean( "is_premium");
		// NOT PARSED: type_i18n
		final String shortName = data.getString("short_name_i18n");
		final String name = data.getString("name_i18n");
		final TankType type = TankType.fromID( data.getString("type") );
		final long id = data.getLong("tank_id" );
		return new Tank(new TankId(id), name, shortName, type, nation, tier, isPremium);
	}

	public TankId getId() {
		return id;
	}

	public Nation getNation() {
		return nation;
	}

	public int getTier() {
		return tier;
	}

	public boolean isPremium() {
		return isPremium;
	}

	public String getName() {
		return name;
	}

	public String getShortName() {
		return shortName;
	}

	public TankType getType() {
		return type;
	}

	public boolean hasType(TankType t) {
		return t.equals( this.type );
	}

	@Override
	public int hashCode() {
		return 31 + id.hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if ( obj instanceof Tank) {
			final Tank other = (Tank) obj;
			return this.id.equals( other.id );
		}
		return false;
	}
}