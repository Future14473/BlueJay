package Main;

import java.util.HashMap;

import org.opencv.core.Mat;

public class RecyclingPlant {
	//yes, a String Key is okay b/c hashmap uses .equals()
	static HashMap<String, Mat> mats = new HashMap<>();
	
	public static Mat request(String name, int width, int height ,int type) {
		if(mats.containsKey(name)) return mats.get(name);
		
		System.out.println("created new Mat of name "+name);
		Mat created = new Mat(height, width, type);
		mats.put(name, created);
		return created;
	}
	
	public static Mat request(String name) {
		if(mats.containsKey(name)) return mats.get(name);
		
		Mat created = new Mat();
		mats.put(name, created);
		return created;
	}
}
