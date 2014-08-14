package de.codesourcery.wotcompare;

public enum Unit {
	TONS("t"),
	ENGINE_POWER("hp"),
	HEALTH(""),
	MILLIMETER("mm") {
		@Override
		public Comparable convert(Unit other, Comparable value)
		{
			switch( other ) {
			case METER:
				return ((Number) value).doubleValue() / 1000.0d;
				default:
					return super.convert(other, value);
			}
		}
	},
	METER("m") {
		@Override
		public Comparable  convert(Unit other, Comparable value)
		{
			switch( other ) {
			case MILLIMETER:
				return ((Number) value).doubleValue() * 1000.0d;
				default:
					return super.convert(other, value);
			}
		}
	},
	DAMAGE(""),
	KM_PER_SECOND("km/s")
	, ROUNDS_PER_MINUTE("rounds/minute"),
	DEGREES_PER_MINUTE("deg/minute");

	private final String symbol;

	private Unit(String symbol) {
		this.symbol = symbol;
	}

	public Comparable  convert(Unit other,Comparable value) {
		throw new UnsupportedOperationException("Cannot convert "+this+" to "+other);
	}

	public String getSymbol() {
		return symbol;
	}

	public Comparable<Double> getZeroValue() {
		return Double.valueOf(0);
	}
}