package de.codesourcery.wotcompare;

import java.time.Duration;
import java.util.List;

public interface IAPIClient {

	public LanguageId getLanguage();

	public void setLanguage(LanguageId language);

	public List<Tank> getTanks() throws APIException;

	public TankDetails getTankDetails(Tank tank) throws APIException;

	public List<TankDetails> getTankDetails(List<Tank> tanks) throws APIException;

	public void dispose();

	public int getMinTier() throws APIException;

	public int getMaxTier() throws APIException;

	/**
	 * Looks up a tank by short name first, attempting to match the long name
	 * if lookup by shortname fails.
	 * 
	 * @param name
	 * @return
	 * @throws APIException
	 */
	public Tank getTankByShortNameOrName(String name) throws APIException;	

	public void discardCache();

	public void setCacheEnabled(boolean onOff);

	public void setMaxCacheAge(Duration maxAge);
}