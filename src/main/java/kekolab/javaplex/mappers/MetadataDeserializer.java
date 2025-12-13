package kekolab.javaplex.mappers;

import java.io.IOException;

import com.fasterxml.jackson.databind.node.ObjectNode;

import kekolab.javaplex.PlexClassicPlaylist;
import kekolab.javaplex.PlexCollection;
import kekolab.javaplex.PlexMetadata;
import kekolab.javaplex.PlexPlaylist;
import kekolab.javaplex.PlexSmartPlaylist;

public class MetadataDeserializer extends ObjectNodeDeserializer<PlexMetadata> {
	private static final long serialVersionUID = 7550593827443213344L;

	public MetadataDeserializer() {
		super(PlexMetadata.class);
	}

	protected Class<? extends PlexMetadata> chooseDeserializingClass(ObjectNode node) throws IOException {
		if (!node.has("type"))
			throw new IOException(
					"Cannot determine the right class to deserialize metadata. PlexMetadata has no `type` attribute");

		String type = extractTextFieldValueFromObjectNode(node, "type");
		if (type.equals(PlexPlaylist.TYPE_DESCRIPTION)) {
			boolean isSmart = extractBooleanFieldValueFromObjectNode(node, "smart");
			return isSmart ? PlexSmartPlaylist.class : PlexClassicPlaylist.class;
		}
		if (type.equals(PlexCollection.TYPE_DESCRIPTION))
			return PlexCollection.class;
		return new MediatagDeserializer().chooseDeserializingClass(node);
	}
}
