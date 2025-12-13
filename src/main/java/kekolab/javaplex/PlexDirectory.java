package kekolab.javaplex;

import java.net.URI;

public class PlexDirectory extends PlexInitialisableItem {
	  
	private UriProvider key;
	private String title;

	public PlexDirectory() {
		key = new UriProvider(this::uri);
	}

	
	public String getKey() {
		return (String) key.getValue();
	}

	public void setKey(String key) {
		this.key.setValue(key);
	}

	
	public URI key() {
		return key.uri();
	}

	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	/* TODO?
	public MediaContainer contents() {
		return new MediaContainer(key(), getClient(), getToken());
	}
     */
}
