package data;

import java.util.LinkedHashMap;

public class TokenData {
	public static LinkedHashMap<String, Integer> tokenIdxMap = new LinkedHashMap<>();
	public static LinkedHashMap<Integer, String> idxTokenMap = new LinkedHashMap<>();
	public int tokenIdx;
	public int count;
	public TokenData(String token, int count) {
		if (!tokenIdxMap.containsKey(token)){
			int idx = tokenIdxMap.size();
			tokenIdxMap.put(token, idx);
			idxTokenMap.put(idx, token);
		}
		this.tokenIdx = tokenIdxMap.get(token);
		this.count = count;
	}
	public int simpleHashCode() {
		// TODO Auto-generated method stub
		return tokenIdx;
	}
	
	public boolean roleEquals(TokenData other){
		if (this.tokenIdx == other.tokenIdx){
			return true;
		}
		return false;
	}
	@Override
	public String toString() {
		return idxTokenMap.get(tokenIdx);
	}
	
	
}
