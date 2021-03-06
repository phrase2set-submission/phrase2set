package data;

import java.io.Serializable;

/**
 * @author thanhnguyen
 * Created on June, 21 
 */

public class PosterData implements Serializable {
	
	private static final long serialVersionUID = 8226958554221039619L;
	
	/** the person who posted a question/answer */
	private long id;
	
	private String name;
	
	private long reputation;
	
	/**
	 * Constructor
	 */
	public PosterData(long id, String name, long reputation) {
		this.id = id;
		this.name = name;
		this.reputation = reputation;
	}
	
	public long getPosterId() {
		return id;
	}
	public void setPosterId(long id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public long getPosterReputation() {
		return reputation;
	}
	public void setPosterReputation(long reputation) {
		this.reputation = reputation;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PosterData [id=");
		builder.append(id);
		builder.append(", name=");
		builder.append(name);
		builder.append(", reputation=");
		builder.append(reputation);
		builder.append("]");
		return builder.toString();
	}
	
	
}
