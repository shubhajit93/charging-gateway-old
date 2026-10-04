package com.ssdtech.main.java.model;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

import com.ssdtech.main.java.model.AppConstant;

public class RequestCounter {
    private static final Queue<Boolean> queue = new LinkedList<Boolean>();
    
    public synchronized static boolean isSuccess() {
    	if(queue.isEmpty()) {
    	    shuffle();
    	}
    	return queue.poll();
    }
    
    public static void shuffle() {
    	Random rand = new Random();
    	Boolean[] successDistribution = Arrays.copyOf(AppConstant.successDistribution, 
    			                                      AppConstant.successDistribution.length);
    	
    	for (int i = successDistribution.length - 1; i > 0; i--) {
            int index = rand.nextInt(i + 1);
            boolean a = successDistribution[index];
            successDistribution[index] = successDistribution[i];
            successDistribution[i] = a;
        }
    	
    	for(int i = 0; i < successDistribution.length; i++) {
    		queue.add(successDistribution[i]);
    	}
    }
}
