package kekolab.javaplex.mappers;

import java.io.IOException;

import com.fasterxml.jackson.databind.node.ObjectNode;

import kekolab.javaplex.PlexDirectory;
import kekolab.javaplex.PlexMovieSection;
import kekolab.javaplex.PlexMusicSection;
import kekolab.javaplex.PlexPhotoSection;
import kekolab.javaplex.PlexShowSection;
import kekolab.javaplex.PlexSectionSecondaryDirectory;

public class DirectoryDeserializer extends ObjectNodeDeserializer<PlexDirectory> {
	private static final long serialVersionUID = 7550593827443213344L;

	public DirectoryDeserializer() {
		super(PlexDirectory.class);
	}

	protected Class<? extends PlexDirectory> chooseDeserializingClass(ObjectNode node) throws IOException {
		if (node.has("composite")) {
			String composite = extractTextFieldValueFromObjectNode(node, "composite");
			if (composite.contains("sections")) {
				String type = extractTextFieldValueFromObjectNode(node, "type");
				if (type.equalsIgnoreCase(PlexShowSection.TYPE_DESCRIPTION))
					return PlexShowSection.class;
				if (type.equalsIgnoreCase(PlexMovieSection.TYPE_DESCRIPTION))
					return PlexMovieSection.class;
				if (type.equalsIgnoreCase(PlexMusicSection.TYPE_DESCRIPTION))
					return PlexMusicSection.class;
				if (type.equalsIgnoreCase(PlexPhotoSection.TYPE_DESCRIPTION))
					return PlexPhotoSection.class;
					// TODO Log that it was impossible to deserialize this type of section
				throw new IOException("Cannot determine the right class to deserialize directory with type " + type);
			} else {
				// TODO Log that it was impossible to deserialize a node with composite set to something different from "composite"
			}
		}

		if (node.has("fastKey")) 			
			return PlexSectionSecondaryDirectory.class;		

		// TODO Log that we're deserializing to the default implementation of PlexDirectory
		return PlexDirectory.class;
	}
}
