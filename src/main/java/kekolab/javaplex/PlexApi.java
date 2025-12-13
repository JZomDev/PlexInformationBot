package kekolab.javaplex;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.net.URIBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PlexApi {
    private final PlexHTTPClient client;
    private Optional<String> token;
	private final Logger logger = LogManager.getLogger(PlexApi.class);

    PlexApi(PlexHTTPClient client) {
        this.client = Objects.requireNonNull(client);
        token = Optional.empty();
    }

    PlexApi(PlexHTTPClient client, String token) {
        this.client = Objects.requireNonNull(client);
        withToken(token);
    }

    public PlexApi withToken(String token) {
        this.token = Optional.of(token);
        return this;
    }

    /**
     * 
     * @return
     * @throws NoSuchElementException if the token is not set. This API call needs a
     *                                token
     */
    public PlexResources getResources() throws NoSuchElementException {
        return new PlexResources(client, token.get());
    }

    /**
     * 
     * @return
     * @throws NoSuchElementException
     */
    public PlexServers getServers() throws NoSuchElementException {
        return new PlexServers(client, token.get());
    }

    public PlexMediaServer getMediaServer(URI uri) {
        return new PlexMediaServer(uri, client, token);
    }

    public PlexMediaServer getMediaServer(String host, int port) {
        try {
            return getMediaServer(new URIBuilder().setHost(host).setPort(port).build());
        } catch (URISyntaxException e) {
            throw new PlexException("Unknown exception. Please see attached stacktrace", e);
        }
    }

    public PlexMediaServer getMediaServer(PlexConnection connection) {
        return new PlexMediaServer(connection.uri(), client, token);
    }

    public PlexMediaServer getMediaServer(PlexDevice device) {
        if (!device.isServer())
            throw new PlexException("The device is not a server");
        if (device.getConnections().size() == 0)
        throw new PlexException("No connections for the device");
        for (PlexConnection connection : device.getConnections()) {
            try {
                return new PlexMediaServer(connection.uri(), client, Optional.of(device.getAccessToken()));
            } catch (Exception e) {
                // TODO
				logger.error(e.getMessage(), e);
            }
        }
        throw new PlexException("Cannot connect to device");
    }

    public PlexAuthorizer getAuthorizer() {
        return new PlexAuthorizer(client);
    }

    public static class Builder {
        private Optional<String> plexClientIdentifier;
        private Optional<String> plexDevice;
        private Optional<String> plexDeviceName;
        private Optional<String> plexPlatform;
        private Optional<String> plexPlatformVersion;
        private Optional<String> plexProduct;
        private Optional<String> plexProductVersion;
        private List<String> plexProvides;
        private HttpClientBuilder clientBuilder;

		private final Logger logger = LogManager.getLogger(PlexApi.class);

        public static Builder withDefaultHttpClient() {
            return new Builder(HttpClientBuilder.create());
        }

        public static Builder withCustomHttpClient(HttpClientBuilder builder) {
            return new Builder(Objects.requireNonNull(builder));
        }

        private Builder(HttpClientBuilder builder) {
            this.clientBuilder = builder;
            plexClientIdentifier = Optional.empty();
            plexDevice = Optional.empty();
            plexDeviceName = Optional.empty();
            plexPlatform = Optional.empty();
            plexPlatformVersion = Optional.empty();
            plexProduct = Optional.empty();
            plexProductVersion = Optional.empty();
            plexProvides = new ArrayList<>();
        }

        public Builder withPlexClientIdentifier(String plexClientIdentifier) {
            this.plexClientIdentifier = Optional.of(plexClientIdentifier);
            return this;
        }

        public Builder withPlexDevice(String plexDevice) {
            this.plexDevice = Optional.of(plexDevice);
            return this;
        }

        public Builder withPlexDeviceName(String plexDeviceName) {
            this.plexDeviceName = Optional.of(plexDeviceName);
            return this;
        }

        public Builder withPlexPlatform(String plexPlatform) {
            this.plexPlatform = Optional.of(plexPlatform);
            return this;
        }

        public Builder withPlexPlatformVersion(String plexPlatformVersion) {
            this.plexPlatformVersion = Optional.of(plexPlatformVersion);
            return this;
        }

        public Builder withPlexProduct(String plexProduct) {
            this.plexProduct = Optional.of(plexProduct);
            return this;
        }

        public Builder withPlexProductVersion(String plexProductVersion) {
            this.plexProductVersion = Optional.of(plexProductVersion);
            return this;
        }

        public Builder withPlexProvides(List<String> plexProvides) {
            if (plexProvides != null)
                this.plexProvides = plexProvides;
            return this;
        }

        public PlexApi build() {
            clientBuilder.addRequestInterceptorLast(this::logRequest);
            PlexHTTPClient client = new PlexHTTPClient(clientBuilder.build(), plexProduct, plexProductVersion,
                    plexClientIdentifier, plexPlatform, plexPlatformVersion, plexDevice, plexDeviceName, plexProvides);
            return new PlexApi(client);
        }

        private void logRequest(HttpRequest request, EntityDetails entity, HttpContext context) {
            try {
                StringBuilder msg = new StringBuilder()
                        .append(request.getMethod())
                        .append(" ")
                        .append(request.getUri().toString()
                        // new
                        // URIBuilder(request.getUri()).removeParameter(PlexHTTPClient.PARAMETER_X_PLEX_TOKEN).build().toString()
                        )
                        .append(System.lineSeparator());
                for (Header header : request.getHeaders()) {
                    msg.append(header.getName()).append(" : ").append(header.getValue())
                            .append(System.lineSeparator());
                }
                if (entity != null && entity instanceof StringEntity stringEntity) {
                    try (InputStream is = stringEntity.getContent()) {
                        byte[] chunk = new byte[4196];
                        int read = 0;
                        while ((read = is.read(chunk)) != -1) {
                            msg.append(new String(chunk, 0, read));
                        }
                    } catch (IOException e) {
						logger.error(e.getMessage(), e);
                    }
                }
				logger.debug(msg.toString());
            } catch (URISyntaxException e) {
				logger.error(e.toString());
            }
        }
    }
}