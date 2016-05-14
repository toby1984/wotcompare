package de.codesourcery.wotcompare;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import de.codesourcery.wotcompare.APIMethod.Category;

public class SimpleAPIClient implements IAPIClient
{
	private static final File CACHE_FILE = new File("wotcompare.cache");

	private static final String PARAM_APPLICATION_ID = "application_id";

	private static final String DEFAULT_SERVER_URL="https://api.worldoftanks.eu/wot";

	private static final Cache CACHE = new Cache();

	private LanguageId language=LanguageId.ENGLISH;
	private final ApplicationId appId;
	private final String serverUrl;
	private CloseableHttpClient client;
	private final boolean cacheEnabled = true;
	private Duration maxCacheAge = Duration.ofDays( 7 );

	protected static final class Cache
	{
		private static final String PROP_CREATION_TIME_MILLIS = "creationTimeMillis";

		private final HashMap<String,String> map = new HashMap<>();
		private boolean cacheLoaded = false;

		private LocalDateTime cacheTimestamp = LocalDateTime.now();

		private boolean isDirty = false;

		public String get(String key)
		{
			synchronized(map)
			{
				init();
				return map.get( key );
			}
		}

		public void discardIfOlderThan(Duration age)
		{
			synchronized(map)
			{
				init();
				final Duration cacheAge = Duration.between( cacheTimestamp , LocalDateTime.now() );
				if ( cacheAge.compareTo( age ) > 0 )
				{
					System.out.println("Discarding cache with age "+cacheAge+" that is older than "+age);
					discardCache();
				}
			}
		}

		public void discardCache()
		{
			synchronized(map)
			{
				init();
				map.clear();
				isDirty = true;
			}
		}

		private void init()
		{
			synchronized(map)
			{
				if ( ! cacheLoaded ) {
					try {
						map.putAll( load() );
					} catch (final IOException e) {
						e.printStackTrace(); // swallow exception and just continue with empty cache
					}
					cacheLoaded = true;
				}
			}
		}

		public void put(String key,String value)
		{
			if (StringUtils.isBlank(key)) {
				throw new IllegalArgumentException("key must not be NULL/blank");
			}
			if (StringUtils.isBlank(value)) {
				throw new IllegalArgumentException("value must not be NULL/blank");
			}
			synchronized(map)
			{
				init();
				map.put(key, value);
				isDirty = true;
			}
		}

		public void save() throws IOException
		{
			synchronized(map)
			{
				if ( isDirty ) {
					final Properties p = new Properties();
					p.putAll( map );

					final ZoneId zone = ZoneId.systemDefault();
				    final ZonedDateTime zdt = cacheTimestamp.atZone(zone);
				    final ZoneOffset offset = zdt.getOffset();
					p.put( PROP_CREATION_TIME_MILLIS , Long.toString( cacheTimestamp.toInstant( offset ).toEpochMilli()  ) );
					p.storeToXML( new FileOutputStream(CACHE_FILE), "automatically generated file - DO NOT EDIT, CHANGES WILL BE OVERWRITTEN" );
					System.out.println("Saved "+map.size()+" requests to cache file "+CACHE_FILE.getAbsolutePath());
					isDirty = false;
				} else {
					System.out.println("Cache not dirty, not updating cache file");
				}
			}
		}

		private Map<String,String> load() throws FileNotFoundException, IOException
		{
			final Map<String,String> result = new HashMap<>();
			if ( CACHE_FILE.exists() )
			{
				final Properties p = new Properties();
				p.loadFromXML( new FileInputStream( CACHE_FILE ) );
				for ( final String key : p.stringPropertyNames() )
				{
					result.put( key , p.getProperty( key ) );
				}
				System.out.println("Loaded "+result.size()+" requests from cache file "+CACHE_FILE.getAbsolutePath());
				final String creationMillis = p.getProperty( PROP_CREATION_TIME_MILLIS);
				if ( creationMillis != null ) {
					cacheTimestamp = LocalDateTime.ofInstant( Instant.ofEpochMilli( Long.parseLong( creationMillis ) ), ZoneId.systemDefault() );
				}
				System.out.println("Cache created on "+cacheTimestamp);
			}
			return result;
		}
	}

	public SimpleAPIClient(ApplicationId appId) {
		this( appId , DEFAULT_SERVER_URL );
	}

	public SimpleAPIClient(ApplicationId appId,String serverUrl) {
		if (appId == null) {
			throw new IllegalArgumentException("appId must not be NULL");
		}
		if ( serverUrl == null ) {
			throw new IllegalArgumentException("serverUrl must not be NULL");
		}
		this.appId = appId;
		this.serverUrl = serverUrl;
	}

	@Override
	public List<Tank> getTanks()
	{
		final List<Tank> result = newRequest( Category.ENCYCLOPEDIA, "tanks").execute( new JSONHandler<List<Tank>>()
		{
			@Override
			public List<Tank> process(JSONObject root)
			{
				final JSONObject data = root.getJSONObject("data");
				@SuppressWarnings("unchecked")
				final List<Tank> results = (List<Tank>) data.keySet().stream().map( key -> Tank.parse( data.getJSONObject( key.toString() ) ) ).collect(Collectors.toCollection(ArrayList::new));
				results.removeIf( Tank::isBot );
				final Map<String,Integer> tankCountsByShortName = new HashMap<>();
				for ( Tank t : results ) {
					Integer count = tankCountsByShortName.get(t.getShortName());
					if ( count == null ) {
						count = Integer.valueOf(1);
					} else {
						count = count + 1;
					}
					tankCountsByShortName.put( t.getShortName() , count );
				}
				for ( Tank t : results ) 
				{
					t.setShortNameUnique( tankCountsByShortName.get(t.getShortName()).intValue() == 1 );
				}
				return results;
			}
		});
//		System.out.println("Got "+result.size()+" tanks");
//
//		final Map<Integer, List<Tank>> tanksByTier = result.stream().collect(Collectors.groupingBy( tank -> tank.getTier() ) );
//		for ( final Map.Entry<Integer,List<Tank>> entry : tanksByTier.entrySet() )
//		{
//			System.out.println("Tier "+entry.getKey()+":");
//				final Map<Nation, List<Tank>> tanksByNation = entry.getValue().stream().collect( Collectors.groupingBy( tank -> tank.getNation() ) );
//			tanksByNation.forEach( (nation,tanks) ->
//			{
//				final String tanksByType = tanks.stream().collect(Collectors.groupingBy( tank -> tank.getType() ) )
//				.entrySet().stream().map( entry2 -> entry2.getKey().toString()+"="+entry2.getValue().size() ).collect( Collectors.joining(",") );
//				System.out.println( nation+" - "+tanks.size()+" tanks ( "+tanksByType+" )");
//
//			});
//			System.out.println("------------------------------");
//		}
		return result;
	}

	@Override
	public List<TankDetails> getTankDetails(List<Tank> tanks) throws APIException
	{
		if ( tanks.isEmpty() ) {
			return new ArrayList<>();
		}

		final String ids = tanks.stream().map( tank -> Long.toString( tank.getId().getId() ) ).collect( Collectors.joining("," ) );

		final Map<Long, List<Tank>> tanksById = tanks.stream().collect( Collectors.groupingBy( tank -> tank.getId().getId() ) );

		return newRequest( Category.ENCYCLOPEDIA, "tankinfo").param("tank_id", ids ).execute( new JSONHandler<List<TankDetails>>()
		{
			@Override
			public List<TankDetails> process(JSONObject root)
			{
				final JSONObject data = root.getJSONObject("data");
				return (List<TankDetails>) data.keySet().stream().map( key ->
				{
					final Tank tank = tanksById.get( Long.parseLong( key.toString() ) ).get(0);
					return TankDetails.parse( tank , data.getJSONObject( key.toString() ) );
				}).collect(Collectors.toList());
			}
		});
	}

	@Override
	public TankDetails getTankDetails(Tank tank)
	{
		return getTankDetails( Collections.singletonList( tank ) ).get(0);
	}

	// =========== helper methods ==============

	protected final class APIRequestBuilder
	{
		private final APIMethod method;
		private final Map<String,Object> parameters = new HashMap<>();
		private final boolean cacheable = true;

		public APIRequestBuilder(Category category,String methodName) {
			this( APIMethod.create(category,methodName) );
		}

		public APIRequestBuilder(APIMethod method)
		{
			if (method == null) {
				throw new IllegalArgumentException("method must not be NULL");
			}
			this.method = method;
		}

		public APIRequestBuilder param(String key,Object value)
		{
			if ( StringUtils.isBlank(key) ) {
				throw new IllegalArgumentException("Key must not be NULL/blank");
			}
			if ( value == null ) {
				throw new IllegalArgumentException("value must not be NULL");
			}
			if ( parameters.containsKey( key ) ) {
				throw new RuntimeException("Internal error, parameter '"+key+"' already set with value '"+parameters.get(key)+"' (instead of '"+value+"')");
			}
			parameters.put( key ,  value );
			return this;
		}

		public <T> T execute(JSONHandler<T> handler)
		{
			try
			{
				parameters.put( PARAM_APPLICATION_ID, appId.value() );

				final List<BasicNameValuePair> pairs = parameters.entrySet().stream().map( entry -> new BasicNameValuePair(entry.getKey(), entry.getValue().toString() ) )
					.collect( Collectors.toCollection( () -> new ArrayList<>() ) );

				return sendRequest( method , pairs , handler , cacheable );
			} catch (IOException | URISyntaxException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private APIRequestBuilder newRequest(Category category,String methodName) {
		return new APIRequestBuilder(category,methodName);
	}

	private void connect()
	{
		if ( client == null ) {
			client = HttpClients.createDefault();
		}
	}

	protected interface JSONHandler<T> {
		public T process(JSONObject root);
	}

	private <T> T sendRequest(APIMethod method,List<? extends NameValuePair> parameters, JSONHandler<T> handler,boolean cacheable) throws ClientProtocolException, IOException, URISyntaxException
	{
		/*
The format of all URL requests:

http(s)://<server>/<API_name>/<method block>/<method name>/?<get params>Where:

    <server> — URI for a game server on the corresponding cluster
    <API_name> — API version
    <method block> — name of the method group
    <method name> — method name
    <get params> — method name

		 */
        final ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

            @Override
			public String handleResponse(final HttpResponse response) throws ClientProtocolException, IOException
            {
                final int status = response.getStatusLine().getStatusCode();
                if (status >= 200 && status < 300)
                {
                    final HttpEntity entity = response.getEntity();
                    final String result = entity != null ? EntityUtils.toString(entity) : "";
                    System.out.println("RECEIVED = "+result);
                    return result;
                } else {
                    throw new ClientProtocolException("Unexpected response status: " + status);
                }
            }
        };

        // perform request
        String baseURL = createURL( method );
        if ( ! baseURL.endsWith("/" ) ) {
        	baseURL += "/"; // wargaming requires use of strange HTTP URI that looks like 'server/something/?queryParams'
        }
        if ( ! parameters.isEmpty() ) {
            final String paramString = URLEncodedUtils.format(parameters, Consts.UTF_8);
            baseURL += "?"+paramString;
        }

        if ( cacheable && cacheEnabled ) {
        	CACHE.discardIfOlderThan( maxCacheAge );
        }

        final String cachedBody = cacheable && cacheEnabled ? CACHE.get( baseURL ) : null;

        String body = cachedBody;
        if ( body == null )
        {
        	System.out.println("SENDING REQUEST: "+baseURL+" ...");
    		connect();
			final HttpGet getRequest = new HttpGet( baseURL );
			body = client.execute(getRequest, responseHandler);
        }

        // invoke callback
        final JSONObject jsonObject = new JSONObject( body );

        // {"status":"error","error":{"field":null,"message":"METHOD_NOT_FOUND","code":404,"value":"\/wot\/encyclopedia\/tanks2\/"}}
        if ( ! "ok".equals( jsonObject.getString("status" ) ) )
        {
        	final JSONObject error = jsonObject.getJSONObject("error");
        	final String msg = error.isNull( "message" ) ? null : error.getString("message");
        	final String field = error.isNull( "field" ) ? null : error.getString("field");
        	final String value = error.isNull( "value" ) ? null : error.getString("value");
        	throw new APIException(msg,field,value);
        }

        if ( cacheable && cacheEnabled && cachedBody == null) {
        	CACHE.put(baseURL, body);
        }
		return handler.process( jsonObject );
	}

	private String createURL(APIMethod method)
	{
		return serverUrl.toString()+"/"+method.getCategory().id()+"/"+method.getMethodName();
	}

	@Override
	public LanguageId getLanguage() {
		return language;
	}

	@Override
	public void setLanguage(LanguageId language) {
		if (language == null) {
			throw new IllegalArgumentException("language must not be NULL");
		}
		this.language = language;
	}

	@Override
	public void dispose() {
		try {
			CACHE.save();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public int getMinTier() throws APIException {
		return getTanks().stream().map( tank -> tank.getTier() ).min( (a,b) -> Integer.compare( a, b ) ).orElseThrow( () -> new RuntimeException("Internal error, no tanks ?"));
	}

	@Override
	public int getMaxTier() throws APIException {
		return getTanks().stream().map( tank -> tank.getTier() ).max( (a,b) -> Integer.compare( a, b ) ).orElseThrow( () -> new RuntimeException("Internal error, no tanks ?"));
	}

	private Tank getTankByName(String name) throws APIException
	{
		final List<Tank> matches = getTanks().stream().filter( tank -> name.equals( tank.getName() ) ).collect( Collectors.toList() );
		if ( matches.size() == 1 ) {
			return matches.get(0);
		}
		throw new NoSuchElementException("Found "+matches.size()+" tank(s) with name '"+name+"' : "+
		matches.stream().map( Tank::toString ).collect( Collectors.joining("\n") ) );
	}
	
	@Override
	public Tank getTankByShortNameOrName(String name) throws APIException 
	{
		final List<Tank> matches = getTanks().stream().filter( tank -> name.equals( tank.getShortName() ) ).collect( Collectors.toList() );
		if ( matches.size() == 1 ) {
			return matches.get(0);
		}
		if ( matches.isEmpty() ) {
			return getTankByName(name);
		}
		throw new NoSuchElementException("Found "+matches.size()+" tank(s) with short name '"+name+"' : "+
		matches.stream().map( Tank::toString ).collect( Collectors.joining("\n") ) );
	}

	@Override
	public void discardCache() {
		CACHE.discardCache();
	}

	@Override
	public void setCacheEnabled(boolean onOff) {
		this.setCacheEnabled( onOff );
	}

	@Override
	public void setMaxCacheAge(Duration maxAge) {
		if (maxAge == null) {
			throw new IllegalArgumentException("maxAge must not be NULL");
		}
		this.maxCacheAge = maxAge;
	}
}