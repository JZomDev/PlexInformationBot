package kekolab.javaplex;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.net.URIBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;

import kekolab.javaplex.filtering.PlexFilter;

public class PlexSmartPlaylist<S extends PlexSection, M extends PlexMediatag<S>> extends PlexPlaylist<M> {
	private String content;
	private String icon;

	void update(PlexMetadata source) {
		super.update(source);
		PlexSmartPlaylist<?, ?> p = (PlexSmartPlaylist<?, ?>) source;
		setContent(p.getContent());
		setIcon(p.getIcon());
	}

	public S section() {
		Matcher matcher = Pattern.compile(".*/(\\d+)/.*").matcher(contentURIBuilder().getPath());
		if (matcher.matches())
			return (S) getServer().library().sections().byId(Integer.parseInt(matcher.group(1)));

		throw new PlexException("Cannot determine section for PlexSmartPlaylist");
	}

	private URIBuilder contentURIBuilder() {
		try {
			return new URIBuilder(URLDecoder.decode(getContent(), "UTF-8"));
		} catch (UnsupportedEncodingException | URISyntaxException e) {
			throw new PlexException(e);
		}
	}

	@JsonIgnore
	public Optional<String> getSort() {
		return contentURIBuilder().getQueryParams().stream().filter(p -> p.getName().toLowerCase().equals("sort"))
				.map(NameValuePair::getValue).findAny();
	}

	public PlexFilter getFilter() {
		List<NameValuePair> queryParams = contentURIBuilder().getQueryParams();
		queryParams.removeIf(p -> p.getName().toLowerCase().equals("type") || p.getName().toLowerCase().equals("sort"));
		return PlexFilter.parse(queryParams);
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getContent() {
		ensureDetailed(content);
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
}
