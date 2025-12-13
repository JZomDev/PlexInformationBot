package kekolab.javaplex;

public class PlexGuid extends PlexMediatagAttribute {
	private String id;

	public String getId() {
		ensureDetailed(id);
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
