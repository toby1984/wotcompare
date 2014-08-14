package de.codesourcery.wotcompare;

public class ValueWithUnit implements Comparable<ValueWithUnit> {

	private final Unit unit;
	@SuppressWarnings("rawtypes")
	private final Comparable value;

	@SuppressWarnings("rawtypes")
	public ValueWithUnit(Unit unit,Comparable value) {
		this.unit = unit;
		this.value = value;
	}

	@SuppressWarnings("unchecked")
	@Override
	public int compareTo(ValueWithUnit o)
	{
		@SuppressWarnings("rawtypes")
		Comparable otherValue = o.value;
		if ( o.unit != this.unit )
		{
			otherValue = o.unit.convert( this.unit , otherValue);
		}
		return this.value.compareTo(o.value);
	}

	public Unit getUnit() {
		return unit;
	}

	public Comparable getValue() {
		return value;
	}

	@Override
	public String toString() {
		return value+" "+unit.getSymbol();
	}
}