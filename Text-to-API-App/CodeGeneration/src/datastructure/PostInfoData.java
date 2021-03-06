/**
 * 
 */
package datastructure;

import java.util.ArrayList;

/**
 * @author anhnt
 *
 */
public class PostInfoData {

	public long id;
	public ArrayList<String> snippets;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	

	public PostInfoData(long id, ArrayList<String> snippets) {
		this.id = id;
		this.snippets = snippets;
	}



	@Override
	public String toString() {
		return "PostInfoData [id=" + id + ", snippets=" + snippets + "]";
	}



	public ArrayList<String> getSnippets(){
		return snippets;
	}
}
