package kekolab.javaplex;

import java.net.URI;
import java.util.List;

public class PlexPhotoalbum extends PlexMediatag<PlexPhotoSection>
		implements PlexParent<PlexPhotoSection, PlexMediatag<PlexPhotoSection>> {
	public static final int TYPE_ID = 11;
	public static final String TYPE_DESCRIPTION = "photo";

	private UriProvider composite;

	public PlexPhotoalbum() {
		composite = new UriProvider(this::uri);
	}

	@Override
	void update(PlexMetadata source) {
		super.update(source);
		PlexPhotoalbum p = (PlexPhotoalbum) source;
		setComposite(p.getComposite());
	}

	@Override
	public List<PlexMediatag<PlexPhotoSection>> children() {
		return new PlexGeneralPurposeMediaContainer<PlexMediatag<PlexPhotoSection>, PlexDirectory>(key(), getServer())
				.getMetadata();
	}

	public String getComposite() {
		ensureDetailed(composite.getValue());
		return (String) composite.getValue();
	}

	public URI composite() {
		ensureDetailed(composite.getValue());
		return composite.uri();
	}

	public void setComposite(String composite) {
		this.composite.setValue(composite);
	}

	@Override
	public int typeId() {
		return TYPE_ID;
	}
}
