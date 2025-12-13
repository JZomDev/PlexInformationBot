package kekolab.javaplex;

import java.net.URI;

// TODO Should be splitted in different classes... Chapter, Review, Tag, ...
public class PlexTag extends PlexMediatagAttribute {
	private Integer id;
	private String filter;
	private String tag;
	private String path;
	private String tagKey;
	private Integer count;

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

	private UriProvider thumb;

	public PlexTag() {
		thumb = new UriProvider(null);
	}

	public String getThumb() {
		ensureDetailed(thumb.getValue());
		return (String) thumb.getValue();
	}

	void setThumb(String thumb) {
		this.thumb.setValue(thumb);
	}

	public URI thumb() {
		return thumb.uri();
	}

	public Integer getId() {
		ensureDetailed(id);
		return id;
	}

	public String getFilter() {
		ensureDetailed(filter);
		return filter;
	}

	public String getTag() {
		if (getParentTag() != null) // When editing this could be null. This is the only method called in
									// TaglistFieldEditor.queryParameters
			ensureDetailed(tag);
		return tag;
	}

	public String getPath() {
		ensureDetailed(path);
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getTagKey() {
		ensureDetailed(tagKey);
		return tagKey;
	}

	public void setTagKey(String tagKey) {
		this.tagKey = tagKey;
	}
}
