package data;

import java.io.Serializable;

/**
 * @author thanhnguyen
 * Created on June, 21
 */

public class CodeElementData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6362300235449693688L;
	
	/** Partially qualified name, e.g System.out ... */
	public String pqn;
	
	/** qualified name, i.e method calls, field access, ... */
	public String name;
	
	/** kind of a code element, i.e type (Class, Simple Type), method, field and unknown 
	 * Any potential issues with String-formatted field? */
	public String kind;
	
	public CodeElementData(String extractedQpn, String extractedName, String extractedKind) {
		pqn = extractedQpn;
		name = extractedName;
		kind = extractedKind;
	}

	public String getPqn() {
		return pqn;
	}

	public void setPqn(String pqn) {
		this.pqn = pqn;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CodeElementData [pqn=");
		builder.append(pqn);
		builder.append(", name=");
		builder.append(name);
		builder.append(", kind=");
		builder.append(kind);
		builder.append("]");
		return builder.toString();
	}

}
