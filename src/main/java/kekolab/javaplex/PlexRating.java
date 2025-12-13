package kekolab.javaplex;

public class PlexRating extends PlexMediatagAttribute {
	private String image;
	private Double value;
	private String type;
	private Integer count;

	
	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

	
	public String getImage() {
		ensureDetailed(image);
		return image;
	}

	
	public String getType() {
		ensureDetailed(type);
		return type;
	}

	
	public Double getValue() {
		ensureDetailed(value);
		return value;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public void setValue(Double value) {
		this.value = value;
	}

	public void setType(String type) {
		this.type = type;
	}
}
