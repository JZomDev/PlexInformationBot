package kekolab.javaplex;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.net.URIBuilder;

import kekolab.javaplex.filtering.PlexFilter;

public class PlexSectionQueryBuilder<ResultType extends PlexMediatag<?>> {
	private PlexSection section;
	private String path;
	private PlexFilter filter;
	private String sort;
	private List<NameValuePair> parameters;

	protected PlexSectionQueryBuilder() {
		this.parameters = new ArrayList<>();
	}

	protected PlexSectionQueryBuilder<ResultType> addParameter(NameValuePair parameter) {
		this.parameters.add(parameter);
		return this;
	}

	protected PlexSectionQueryBuilder<ResultType> withSection(PlexSection section) {
		this.section = section;
		return this;
	}

	protected PlexSectionQueryBuilder<ResultType> withPath(String path) {
		this.path = path;
		return this;
	}

	
	public PlexSectionQueryBuilder<ResultType> filtered(PlexFilter condition) {
		this.filter = condition;
		return this;
	}

	
	public PlexSectionQueryBuilder<ResultType> sorted(String sort) {
		this.sort = sort;
		return this;
	}

	
	public List<ResultType> execute() {
		URIBuilder uri = new URIBuilder(section.key()).appendPath(path);
		uri.addParameters(parameters);
		if (filter != null)
			uri.addParameters(filter.getQueryParameters());
		if (sort != null)
			uri.addParameter("sort", sort);

		try {
			return new PlexGeneralPurposeMediaContainer<ResultType, PlexDirectory>(uri.build(), section.getServer()).getMetadata();
		} catch (URISyntaxException e) {
			throw new PlexException(e);
		}
	}
}