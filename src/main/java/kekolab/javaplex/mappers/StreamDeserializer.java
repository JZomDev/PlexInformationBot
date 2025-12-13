package kekolab.javaplex.mappers;

import java.io.IOException;

import com.fasterxml.jackson.databind.node.ObjectNode;

import kekolab.javaplex.PlexAudioStream;
import kekolab.javaplex.PlexLyricsStream;
import kekolab.javaplex.PlexStream;
import kekolab.javaplex.PlexTextStream;
import kekolab.javaplex.PlexVideoStream;

public class StreamDeserializer extends ObjectNodeDeserializer<PlexStream> {
	private static final long serialVersionUID = -1200655581653620432L;

	public StreamDeserializer() {
		super(PlexStream.class);
	}

	@Override
	protected Class<? extends PlexStream> chooseDeserializingClass(ObjectNode node) throws IOException {
		if (node.has("streamType")) {
			int type = extractIntegerFieldValueFromObjectNode(node, "streamType");
			switch (type) {
				case PlexVideoStream.TYPE_ID:
					return PlexVideoStream.class;
				case PlexAudioStream.TYPE_ID:
					return PlexAudioStream.class;
				case PlexTextStream.TYPE_ID:
					return PlexTextStream.class;
				case PlexLyricsStream.TYPE_ID:
					return PlexLyricsStream.class;
				default:
					// TODO Log that we could not find a subclass of PlexStream to deserialize to
			}
		} else {
			// TODO Log that we could not find a subclass of PlexStream to deserialize to
		}

		// TODO Log that we are using the default implementation of PlexStream
		return PlexStream.class;
	}
}
