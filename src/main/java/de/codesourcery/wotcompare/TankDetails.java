package de.codesourcery.wotcompare;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

public class TankDetails
{
	private final Tank tank;
	private final Map<WotProperty,ValueWithUnit> properties = new HashMap<>();

	private final Map<ModuleType,List<Module>> defaultModules = new HashMap<>();
	private final Map<ModuleType,List<Module>> nonDefaultModules = new HashMap<>();

	public TankDetails(Tank tank) {
		if (tank == null) {
			throw new IllegalArgumentException("tank must not be NULL");
		}
		this.tank = tank;
	}

	public Map<ModuleType, List<Module>> getDefaultModules() {
		return Collections.unmodifiableMap( defaultModules );
	}

	public Map<ModuleType, List<Module>> getNonDefaultModules() {
		return Collections.unmodifiableMap( nonDefaultModules );
	}

	public Map<WotProperty,ValueWithUnit> properties() {
		return Collections.unmodifiableMap( properties );
	}

	public Tank getTank() {
		return tank;
	}

	public ValueWithUnit getPropertyValue(WotProperty property)
	{
		final ValueWithUnit result = properties.get(property);
		if ( result != null ) {
			return result;
		}
		return new ValueWithUnit( property.getUnit() , property.getUnit().getZeroValue() );
	}

	private void addProperty(WotProperty prop , Comparable value) {
		properties.put( prop , prop.value( value ) );
	}

	private void parseModules(ModuleType type,JSONArray json)
	{
		final int length = json.length();
		for ( int i = 0 ; i < length ; i++ )
		{
			final JSONObject object = json.getJSONObject( i );
			final boolean isDefault = object.getBoolean("is_default");
			final long moduleId = object.getLong("module_id");
			final Module m = new Module(new ModuleId(moduleId),type);

			final Map<ModuleType,List<Module>> map = isDefault ? defaultModules : nonDefaultModules;
			List<Module> list = map.get( type );
			if ( list == null ) {
				list = new ArrayList<>();
				map.put( type ,  list );
			}
			list.add( m );
		}
	}

	public static TankDetails parse(Tank tank,JSONObject data) {

		final TankDetails result = new TankDetails( tank );
		result.addProperty( WotProperty.TANK_BASE_WEIGHT , data.getDouble("weight" ) );
		result.addProperty( WotProperty.TANK_MAX_WEIGHT , data.getDouble("limit_weight" ) );
		result.addProperty( WotProperty.TANK_BASE_ENGINE_POWER , data.getDouble("engine_power" ) );
		result.addProperty( WotProperty.TANK_MAX_HEALTH, data.getDouble("max_health" ) );
		result.addProperty( WotProperty.VISION_RADIUS, data.getDouble("circular_vision_radius" ) );
		result.addProperty( WotProperty.GUN_DAMAGE_MIN, data.getDouble("gun_damage_min" ) );
		result.addProperty( WotProperty.GUN_DAMAGE_MAX, data.getDouble("gun_damage_max" ) );

		result.addProperty( WotProperty.GUN_PENETRATION_MIN, data.getDouble("gun_piercing_power_min" ) );
		result.addProperty( WotProperty.GUN_PENETRATION_MAX, data.getDouble("gun_piercing_power_max" ) );

		result.addProperty( WotProperty.GUN_RATE, data.getDouble("gun_rate" ) );

		result.addProperty( WotProperty.TURRET_ROTATION_SPEED , data.getDouble("turret_rotation_speed" ) );
		result.addProperty( WotProperty.TANK_SPEED , data.getDouble("speed_limit" ) );

		result.addProperty( WotProperty.TANK_ROTATION_SPEED, data.getDouble("chassis_rotation_speed" ) );

		result.parseModules( ModuleType.RADIO , data.getJSONArray("radios") );
		result.parseModules( ModuleType.GUN, data.getJSONArray("guns") );
		result.parseModules( ModuleType.ENGINE , data.getJSONArray("engines") );
		result.parseModules( ModuleType.CHASSIS, data.getJSONArray("chassis") );
		result.parseModules( ModuleType.TURRET, data.getJSONArray("turrets") );
		return result;
		/*
"data": {
        "64817": {
            "vehicle_armor_fedd": 19,
            >> "engine_power": 500,
            >> "circular_vision_radius": 390,
            >> "weight": 23,
            >> "gun_damage_min": 86,
            "image": "http://worldoftanks.eu/static/2.13.1/encyclopedia/tankopedia/vehicle/china-ch24_type64.png",
            "is_premium": true,
            "contour_image": "http://worldoftanks.eu/static/2.13.1/encyclopedia/tankopedia/vehicle/contour/china-ch24_type64.png",
            "short_name_i18n": "Type 64",
            "turret_armor_board": 12,
            "crew": [
                {
                    "additional_roles_i18n": [],
                    "role_i18n": "Commander",
                    "role": "commander",
                    "additional_roles": []
                },
                {
                    "additional_roles_i18n": [],
                    "role_i18n": "Gunner",
                    "role": "gunner",
                    "additional_roles": []
                },
                {
                    "additional_roles_i18n": [],
                    "role_i18n": "Driver",
                    "role": "driver",
                    "additional_roles": []
                },
                {
                    "additional_roles_i18n": [],
                    "role_i18n": "Radio Operator",
                    "role": "radioman",
                    "additional_roles": []
                },
                {
                    "additional_roles_i18n": [],
                    "role_i18n": "Loader",
                    "role": "loader",
                    "additional_roles": []
                }
            ],
            >> "max_health": 580,
            "parent_tanks": [],
            "tank_id": 64817,
            "localized_name": "Type 64",
            "radios": [
                {
                    "is_default": false,
                    "module_id": 4151
                }
            ],
            "engines": [
                {
                    "is_default": false,
                    "module_id": 7221
                }
            ],
            "radio_distance": 700,
            >> "gun_piercing_power_max": 160,
            "type_i18n": "Light Tank",
            "vehicle_armor_forehead": 25,
            >> "chassis_rotation_speed": 56,
            "gun_name": "76 mm Gun M1A2",
            "guns": [
                {
                    "is_default": false,
                    "module_id": 8756
                }
            ],
            "gun_max_ammo": 45,
            >> "limit_weight": 25,
            "nation_i18n": "China",
            "vehicle_armor_board": 12,
            "nation": "china",
            >> "gun_piercing_power_min": 96,
            "chassis": [
                {
                    "is_default": false,
                    "module_id": 10546
                }
            ],
            >> "turret_rotation_speed": 36,
            >> "gun_rate": 18.18,
            "is_gift": false,
            "name": "#china_vehicles:Ch24_Type64",
            "price_gold": 3500,
            "level": 6,
            "type": "lightTank",
            "image_small": "http://worldoftanks.eu/static/2.13.1/encyclopedia/tankopedia/vehicle/small/china-ch24_type64.png",
            "speed_limit": 72.4,
            "turret_armor_forehead": 25,
            "price_xp": 0,
            "turret_armor_fedd": 12,
            "price_credit": 0,
            >> "gun_damage_max": 144,
            "name_i18n": "Type 64",
            "turrets": [
                {
                    "is_default": false,
                    "module_id": 10291
                }
            ]
        }
		 */
	}
}
