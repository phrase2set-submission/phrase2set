package data;

public class TokenPair {

	public TokenData token1;
	public TokenData token2;
	
	public int count;

	public TokenPair(String token1Str, String token2Str, int count){
		this.token1 = new TokenData(token1Str, count);
		this.token2 = new TokenData(token2Str, count);
		
		this.count = count;
	}
	public TokenPair(TokenData token1, TokenData token2, int count) {
		this.token1 = token1;
		this.token2 = token2;
		this.count = count;
	}

	public int simpleHashCode() {
		// TODO Auto-generated method stub
		long tmp = (token1.simpleHashCode () + token2.simpleHashCode())/2;
		
		return (int)tmp;
	}
	
	public boolean roleEquals(TokenPair other){
		if (this.token1.roleEquals(other.token1)&&this.token2.roleEquals(other.token2)){
			return true;
		}
		else if (this.token1.roleEquals(other.token2)&&this.token2.roleEquals(other.token1)){
			return true;
		}
		return false;

	}

	@Override
	public String toString() {
		return "TokenPair [token1=" + token1 + ", token2=" + token2
				+ ", count=" + count + "]";
	}
	
	
	
}
