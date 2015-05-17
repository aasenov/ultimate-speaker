package com.aasenov.synthesis.espeak;

public enum SyntheseLanguage {
	BULGARIAN("bg"),
	ENGLISH("en");
	
	private String mValue;

	private SyntheseLanguage(String value) {
		mValue = value;
	}

	/**
	 * Retrieve language configuration value.
	 * 
	 * @return Value to be passed to espeak command.
	 */
	public String getValue() {
		return mValue;
	}
}
