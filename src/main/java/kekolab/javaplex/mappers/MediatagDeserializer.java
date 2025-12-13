package kekolab.javaplex.mappers;

import java.io.IOException;

import com.fasterxml.jackson.databind.node.ObjectNode;

import kekolab.javaplex.PlexAlbum;
import kekolab.javaplex.PlexArtist;
import kekolab.javaplex.PlexClip;
import kekolab.javaplex.PlexEpisode;
import kekolab.javaplex.PlexMediatag;
import kekolab.javaplex.PlexMovie;
import kekolab.javaplex.PlexPhoto;
import kekolab.javaplex.PlexPhotoalbum;
import kekolab.javaplex.PlexSeason;
import kekolab.javaplex.PlexShow;
import kekolab.javaplex.PlexTrack;

public class MediatagDeserializer extends ObjectNodeDeserializer<PlexMediatag> {
	protected MediatagDeserializer() {
		super(PlexMediatag.class);
	}

	protected Class<? extends PlexMediatag<?>> chooseDeserializingClass(ObjectNode node) throws IOException {
		if (!node.has("type"))
			throw new IOException(
					"Cannot determine the right class to deserialize metadata. PlexMetadata has no `type` attribute");

		String type = extractTextFieldValueFromObjectNode(node, "type");
		if (type.equals(PlexArtist.TYPE_DESCRIPTION))
			return PlexArtist.class;
		if (type.equals(PlexAlbum.TYPE_DESCRIPTION))
			return PlexAlbum.class;
		if (type.equals(PlexTrack.TYPE_DESCRIPTION))
			return PlexTrack.class;
		if (type.equals(PlexShow.TYPE_DESCRIPTION))
			return PlexShow.class;
		if (type.equals(PlexSeason.TYPE_DESCRIPTION))
			return PlexSeason.class;
		if (type.equals(PlexEpisode.TYPE_DESCRIPTION))
			return PlexEpisode.class;
		if (type.equals(PlexMovie.TYPE_DESCRIPTION))
			return PlexMovie.class;
		if (type.equals(PlexClip.TYPE_DESCRIPTION))
			return PlexClip.class;
		if (type.equals(PlexPhoto.TYPE_DESCRIPTION) || (type.equals(PlexPhotoalbum.TYPE_DESCRIPTION)))
			// They're the same
			return node.has("composite") ? PlexPhotoalbum.class : PlexPhoto.class;
		throw new IOException("Cannot determine the right class to deserialize metadata with type " + type);

	}
}
