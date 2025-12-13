package kekolab.javaplex;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.ProtocolException;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.apache.hc.core5.http.protocol.BasicHttpContext;
import org.apache.hc.core5.net.URIBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public class PlexHTTPClient {
	private static final String CONTEXT_ATTRIBUTE_PEER_CERTIFICATES = "peer-certificates";
	private static final String HEADER_X_PLEX_PROVIDES = "X-Plex-Provides";
	private static final String HEADER_X_PLEX_DEVICE_NAME = "X-Plex-Device-Name";
	private static final String HEADER_X_PLEX_DEVICE = "X-Plex-Device";
	private static final String HEADER_X_PLEX_PLATFORM_VERSION = "X-Plex-Platform-Version";
	private static final String HEADER_X_PLEX_PLATFORM = "X-Plex-Platform";
	private static final String HEADER_X_PLEX_CLIENT_IDENTIFIER = "X-Plex-Client-Identifier";
	private static final String HEADER_X_PLEX_VERSION = "X-Plex-Version";
	private static final String HEADER_X_PLEX_PRODUCT = "X-Plex-Product";
	private static final String PARAMETER_X_PLEX_TOKEN = "X-Plex-Token";

	private final Logger logger = LogManager.getLogger(PlexHTTPClient.class);

	private final Optional<String> plexProduct;
	private final Optional<String> plexProductVersion;
	private final Optional<String> plexClientIdentifier;
	private final Optional<String> plexPlatform;
	private final Optional<String> plexPlatformVersion;
	private final Optional<String> plexDevice;
	private final Optional<String> plexDeviceName;
	private final List<String> plexProvides;

	private final HttpClient client;
	private final ObjectMapper xmlMapper, jsonMapper;

	PlexHTTPClient(HttpClient client, Optional<String> plexProduct, Optional<String> plexProductVersion,
			Optional<String> plexClientIdentifier, Optional<String> plexPlatform, Optional<String> plexPlatformVersion,
			Optional<String> plexDevice, Optional<String> plexDeviceName, List<String> plexProvides) {
		this.client = client;
		this.plexProduct = plexProduct;
		this.plexProductVersion = plexProductVersion;
		this.plexClientIdentifier = plexClientIdentifier;
		this.plexPlatform = plexPlatform;
		this.plexPlatformVersion = plexPlatformVersion;
		this.plexDevice = plexDevice;
		this.plexDeviceName = plexDeviceName;
		this.plexProvides = plexProvides;
		xmlMapper = new XmlMapper().configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
		jsonMapper = new JsonMapper().configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
	}

	private void ensureResponseIsNotError(ClassicHttpRequest request, ClassicHttpResponse response) {
		if (response.getCode() >= 400)
			throw new PlexHttpClientException("Unknown client error", request, response);
	}

	private <A> Optional<A> parseResponse(ClassicHttpRequest request, ClassicHttpResponse response,
			Optional<Class<A>> cls) {
		ensureResponseIsNotError(request, response);
		if (cls.isPresent()) {
			try {
				return Optional.of(chooseMapper(response).readValue(response.getEntity().getContent(), cls.get()));
			} catch (UnsupportedOperationException | IOException e) {
				throw new PlexHttpClientException("Unknown client exception. See attached stacktrace", e, request,
						response);
			}
		}
		return Optional.empty();
	}

	private <A extends PlexMediaContainer> A parseResponse(ClassicHttpRequest request, ClassicHttpResponse response,
			A container) {
		ensureResponseIsNotError(request, response);
		try {
			chooseMapper(response).readerForUpdating(container).readValue(response.getEntity().getContent());
			return container;
		} catch (UnsupportedOperationException | IOException e) {
			throw new PlexHttpClientException("Unknown client exception. See attached stacktrace", e, request,
					response);
		}
	}

	private ClassicRequestBuilder buildBaseRequestBuilder(URI uri, String method, Optional<String> token) {
		URI _uri = uri;
		if (token.isPresent())
			try {
				_uri = new URIBuilder(_uri).addParameter(PARAMETER_X_PLEX_TOKEN, token.get()).build();
			} catch (URISyntaxException e) {
				throw new PlexHttpClientException("Cannot append token to uri", e);
			}
		ClassicRequestBuilder builder = ClassicRequestBuilder.create(method).setUri(_uri);
		builder.setHeader("Accept", "application/json");
		plexProduct.ifPresent(value -> builder.setHeader(HEADER_X_PLEX_PRODUCT, value));
		plexProductVersion.ifPresent(value -> builder.setHeader(HEADER_X_PLEX_VERSION, value));
		plexClientIdentifier.ifPresent(value -> builder.setHeader(HEADER_X_PLEX_CLIENT_IDENTIFIER, value));
		plexPlatform.ifPresent(value -> builder.setHeader(HEADER_X_PLEX_PLATFORM, value));
		plexPlatformVersion.ifPresent(value -> builder.setHeader(HEADER_X_PLEX_PLATFORM_VERSION, value));
		plexDevice.ifPresent(value -> builder.setHeader(HEADER_X_PLEX_DEVICE, value));
		plexDeviceName.ifPresent(value -> builder.setHeader(HEADER_X_PLEX_DEVICE_NAME, value));
		if (plexProvides.size() > 0)
			builder.setHeader(HEADER_X_PLEX_PROVIDES, String.join(",", plexProvides));


		// TODO Logging	
		
		return builder;
	}


	private <A> Optional<A> execute(ClassicHttpRequest request, Optional<Class<A>> cls) {
		try {
			return client.execute(request, response -> parseResponse(request, response, cls));
		} catch (IOException e) {
			throw new PlexHttpClientException("Unknown client error", e, request);
		}
	}

	private <A extends PlexMediaContainer> A execute(ClassicHttpRequest request, A mediaContainer) {
		try {
			return client.execute(request, response -> parseResponse(request, response, mediaContainer));
		} catch (IOException e) {
			throw new PlexHttpClientException("Unknown client error", e, request);
		}
	}

	private void setRequestEntity(ClassicRequestBuilder builder, PlexBaseItem entity) {
		builder.setHeader("Content-Type", "application/json");
		try {
			builder.setEntity(jsonMapper.writeValueAsString(entity));
		} catch (JsonProcessingException e) {
			throw new PlexHttpClientException("Error while serializing entity to request. See attached exception", e);
		}
	}

	<A extends PlexMediaContainer> Optional<A> post(URI uri, Optional<String> token,
			Optional<? extends PlexBaseItem> entity, Optional<Class<A>> responseEntityClass) {
		ClassicRequestBuilder requestBuilder = buildBaseRequestBuilder(uri, "POST", token);
		entity.ifPresent(e -> setRequestEntity(requestBuilder, e));
		return execute(requestBuilder.build(), responseEntityClass);
	}

	private <T> T executeAndCallResponseHandler(ClassicHttpRequest request, String token,
			HttpClientResponseHandler<T> handler) {
		addTokenToRequest(request, token);
		try {
			return client.execute(request, new BasicHttpContext(), handler);
		} catch (IOException e) {
			throw new PlexException(e);
		}
	}

	@Deprecated
	<A> A executeAndCreateFromResponse(ClassicRequestBuilder builder, PlexBaseItem body, String contentType, Class<A> cls,
			String token) {
		ObjectMapper mapper = contentType.toLowerCase().contains("xml") ? xmlMapper : jsonMapper;
		try {
			builder.setEntity(mapper.writeValueAsString(body));
		} catch (JsonProcessingException e) {
			throw new PlexException(e); // TODO Maybe something better
		}
		return executeAndCallResponseHandler(builder.build(), token, (response) -> parseIntoNewObject(response, cls));
	}

	@Deprecated
	<A> A executeAndCreateFromResponse(ClassicHttpRequest request, Class<A> cls, String token) {
		return executeAndCallResponseHandler(request, token, (response) -> parseIntoNewObject(response, cls));
	}

	@Deprecated
	<A> A executeAndUpdateFromResponse(ClassicHttpRequest request, A item, String token) {
		return executeAndCallResponseHandler(request, token, (response) -> parseAndUpdateObject(response, item));
	}

	@Deprecated
	Void execute(ClassicHttpRequest request, String token) {
		return executeAndCallResponseHandler(request, token, this::checkRespondeCode);
	}

	@Deprecated
	private void addTokenToRequest(ClassicHttpRequest request, String token) {
		if (token != null) {
			try {
				URIBuilder uri = new URIBuilder(request.getUri());
				uri.setParameter(PARAMETER_X_PLEX_TOKEN, token);
				request.setUri(uri.build());
			} catch (URISyntaxException e) {
				throw new PlexException(e);
			}
		}
	}

	@Deprecated
	private <A> A parseIntoNewObject(ClassicHttpResponse response, Class<A> cls) {
		HttpEntity entity = extractEntity(response);
		if (entity == null)
			throw new PlexException("Cannot parse the response: received entity is null");
		try {
			return chooseMapper(response).readValue(entity.getContent(), cls);
		} catch (UnsupportedOperationException | IOException e) {
			throw new PlexException(e);
		}
	}

	@Deprecated
	private <A> A parseAndUpdateObject(ClassicHttpResponse response, A item) {
		HttpEntity entity = extractEntity(response);
		if (entity == null)
			throw new PlexException("Cannot parse the response: received entity is null");
		try {
			return chooseMapper(response).readerForUpdating(item).readValue(entity.getContent());
		} catch (UnsupportedOperationException | IOException e) {
			throw new PlexException(e);
		}
	}

	@Deprecated
	private Void checkRespondeCode(ClassicHttpResponse response) {
		int code = response.getCode();
		if (code >= 400) {
			StringBuilder exceptionMessageBuilder = new StringBuilder().append("Error processing the server response:")
					.append(System.lineSeparator()).append(System.lineSeparator()).append("CODE : ")
					.append(response.getCode()).append(System.lineSeparator());
			for (Header header : response.getHeaders())
				exceptionMessageBuilder.append(header.getName()).append(" : ").append(header.getValue())
						.append(System.lineSeparator());
			IOException inner = null;
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				StringBuilder body = new StringBuilder();
				try (Scanner s = new Scanner(entity.getContent())) {
					while (s.hasNextLine())
						body.append(s.nextLine());
					exceptionMessageBuilder.append(System.lineSeparator()).append(body);
				} catch (IOException e) {
					exceptionMessageBuilder.append(System.lineSeparator()).append("ERROR WHILE READING RESPONSE BODY");
					inner = e;
				} finally {
					exceptionMessageBuilder.append(System.lineSeparator());
				}
			}
			String exceptionMessage = exceptionMessageBuilder.toString();
			if (inner == null)
				throw new PlexException(exceptionMessage);
			throw new PlexException(exceptionMessage, inner);
		}
		return null;
	}

	@Deprecated
	private HttpEntity extractEntity(ClassicHttpResponse response) {
		checkRespondeCode(response);
		return response.getEntity();
	}

	private ObjectMapper chooseMapper(HttpResponse response) {
		Header contentTypeHeader;
		try {
			contentTypeHeader = response.getHeader("Content-Type");
		} catch (ProtocolException e) {
			throw new PlexException(e);
		}
		if (contentTypeHeader == null)
			throw new PlexException("Cannot determine mapper to use to deserialize entity. No Content-Type detected");
		String contentType = contentTypeHeader.getValue().toLowerCase();
		if (contentType.contains("xml"))
			return xmlMapper;
		if (contentType.contains("json"))
			return jsonMapper;
		throw new PlexException("Cannot determine mapper deserialize entity with Content-Type: " + contentType);
	}

	// Method created for the authorizer, whose remote service does not return a
	// MediaContainer
	PlexPin requestPin(URI uri) {
		ClassicHttpRequest request = buildBaseRequestBuilder(uri, "POST", Optional.empty()).build();
		return execute(request, Optional.of(PlexPin.class)).get();
	}

	// Method created for the authorizer, whose remote service does not return a
	// MediaContainer
	PlexPin verifyPin(URI uri) {
		ClassicHttpRequest request = buildBaseRequestBuilder(uri, "GET", Optional.empty()).build();
		return execute(request, Optional.of(PlexPin.class)).get();
	}

	<A extends PlexMediaContainer> A get(Optional<String> token, A mediaContainer) {
		ClassicHttpRequest request = buildBaseRequestBuilder(mediaContainer.getUri(), "GET", token).build();
		return execute(request, mediaContainer);
	}

	<A extends PlexMediaContainer> Optional<A> get(URI uri, Optional<String> token, Optional<Class<A>> cls) {
		ClassicHttpRequest request = buildBaseRequestBuilder(uri, "GET", token).build();
		return execute(request, cls);
	}

	<A extends PlexMediaContainer> A post(Optional<String> token, A mediaContainer) {
		ClassicHttpRequest request = buildBaseRequestBuilder(mediaContainer.getUri(), "POST", token).build();
		return execute(request, mediaContainer);
	}

	<A extends PlexMediaContainer> A post(Optional<String> token, Optional<? extends PlexBaseItem> entity, A mediaContainer) {
		ClassicRequestBuilder requestBuilder = buildBaseRequestBuilder(mediaContainer.getUri(), "POST", token);
		entity.ifPresent(e -> setRequestEntity(requestBuilder, e));
		return execute(requestBuilder.build(), mediaContainer);
	}

	<A extends PlexMediaContainer> Optional<A> post(URI uri, Optional<String> token, Optional<Class<A>> cls) {
		ClassicHttpRequest request = buildBaseRequestBuilder(uri, "POST", token).build();
		return execute(request, cls);
	}

	<A extends PlexMediaContainer> A put(Optional<String> token, A mediaContainer) {
		ClassicHttpRequest request = buildBaseRequestBuilder(mediaContainer.getUri(), "PUT", token).build();
		return execute(request, mediaContainer);
	}

	<A extends PlexMediaContainer> Optional<A> put(URI uri, Optional<String> token, Optional<Class<A>> cls) {
		ClassicHttpRequest request = buildBaseRequestBuilder(uri, "PUT", token).build();
		return execute(request, cls);
	}

	<A extends PlexMediaContainer> A delete(Optional<String> token, A mediaContainer) {
		ClassicHttpRequest request = buildBaseRequestBuilder(mediaContainer.getUri(), "DELETE", token).build();
		return execute(request, mediaContainer);
	}

	<A extends PlexMediaContainer> Optional<A> delete(URI uri, Optional<String> token, Optional<Class<A>> cls) {
		ClassicHttpRequest request = buildBaseRequestBuilder(uri, "DELETE", token).build();
		return execute(request, cls);
	}

}