package detectors.FoundationPipeline;

import org.opencv.core.Mat;

import java.util.HashMap;
import java.util.Map;

public class MatAllocator {
	static Map<String, Mat> matrices = new HashMap<String, Mat>();

	public static Mat getMat(String name){
		if(matrices.containsKey(name)) {
			 return matrices.get(name);
		} else {
			matrices.put(name, new Mat());
			return matrices.get(name);
		}
	}
	public static Mat getMat(String name, int rows, int cols, int type){
		if(matrices.containsKey(name)) {
			return matrices.get(name);
		} else {
			matrices.put(name, new Mat(rows, cols, type));
			return matrices.get(name);
		}
	}


	public static void emptyAll (){
		//we're still in java 1.8... NOOOOO
		for(Map.Entry<String,Mat> s: matrices.entrySet()) {
			compute.rectangle(s.getValue(), true);
		}
	}
}
