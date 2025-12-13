package kekolab.javaplex;

import java.util.Objects;

public abstract class PlexMediatagAttribute extends PlexBaseItem {
	private PlexMediatag<?> tag;
	
	void initialise(PlexMediatag<?> tag) {
		Objects.requireNonNull(tag);
		this.tag = tag;
	}
	
	PlexMediatag<?> getParentTag() {		
		return tag;
	}

	void ensureDetailed(Object field) {
		getParentTag().ensureDetailed(field);
	}
}
