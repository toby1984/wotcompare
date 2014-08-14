package de.codesourcery.wotcompare;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ModuleInfo {

	private final Module module;
	private final Map<WotProperty,ValueWithUnit> properties = new HashMap<>();

	public ModuleInfo(Module module,Map<WotProperty,ValueWithUnit> properties )
	{
		if ( module == null ) {
			throw new IllegalArgumentException("module must not be NULL");
		}
		if ( properties == null ) {
			throw new IllegalArgumentException("properties must not be NULL");
		}
		this.module = module;
		this.properties.putAll(properties);
	}

	public Module getModule() {
		return module;
	}

	public Map<WotProperty, ValueWithUnit> getProperties() {
		return Collections.unmodifiableMap( properties );
	}
}