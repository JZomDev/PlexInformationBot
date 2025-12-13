package kekolab.javaplex;

public class PlexField extends PlexMediatagAttribute {
	private String name;
	private Boolean locked;

	public String getName() {
		ensureDetailed(name);
		return name;
	}

	public Boolean getLocked() {
		ensureDetailed(locked);
		return locked;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setLocked(Boolean locked) {
		this.locked = locked;
	}
}
