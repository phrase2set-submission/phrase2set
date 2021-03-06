package utils;

import java.util.ArrayList;
import java.util.Arrays;

public class Combination {
	public  ArrayList<int[]> outList = new ArrayList<>();
    public static void main(String[] args){
    	Combination comb = new Combination();
        int[] arr = new int[20];
        for (int i=0; i<20; i++){
        	arr[i] = i;
        }
        comb.combinations2(arr, 10, 0, new int[10]);
        for (int[] list:comb.outList){
        	 System.out.println(Arrays.toString(list));
        }
    }

    public  void combinations2(int[] arr, int len, int startPosition, int[] result){
        if (len == 0){
//            System.out.println(Arrays.toString(result));
            outList.add(Arrays.copyOf(result, result.length));
            return;
        }       
        for (int i = startPosition; i <= arr.length-len; i++){
            result[result.length - len] = arr[i];
            combinations2(arr, len-1, i+1, result);
        }
    }       
}