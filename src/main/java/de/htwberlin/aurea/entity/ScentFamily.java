package de.htwberlin.aurea.entity;

/** Duftfamilien nach dem klassischen Duftrad. */
public enum ScentFamily {
	CITRUS(true),
	FRESH(true),
	AQUATIC(true),
	GREEN(true),
	FLORAL(true),
	FRUITY(true),
	GOURMAND(false),
	ORIENTAL(false),
	WOODY(false),
	SPICY(false),
	LEATHER(false);

	/** Passt die Familie eher zu warmem Wetter? */
	private final boolean light;

	ScentFamily(boolean light) {
		this.light = light;
	}

	public boolean isLight() {
		return light;
	}
}
