package data;

import java.io.Serializable;
import java.util.ArrayList;

public class VariableInfo implements Serializable {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3076084483945917949L;
	public String varName;
	public String typeName;
	public ArrayList<Long> scopeList = new ArrayList<Long>(1);
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}

	public VariableInfo( String varName, String typeName, ArrayList<Long> scopeList) {
		super();
		this.varName = varName.intern();
		this.typeName = typeName;
		this.scopeList.addAll(scopeList);
	}



}
