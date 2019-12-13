package org.futurerobotics.bluejay.original.detectors.foundation;

import org.opencv.core.Mat;
import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Analysis {
	
	public static Point skystonePosition(List<Stone> stones, Mat canvas) {
		List<Gap> gaps = gaps(stones);
		
		for (Gap g : gaps) {
			g.draw(canvas);
		}
		
		if (gaps.size() == 0) return null;
		
		//noinspection ComparatorCombinators
		Collections.sort(gaps, (o1, o2) -> Integer.compare(o1.width, o2.width));
		
		return gaps.get(0).center;
	}
	
	/*
	 * In order of left to right, return gaps between stones (sorted),
	 * provided that there is a non negative distance between
	 * the end of one stone and the start of another
	 */
	private static List<Gap> gaps(List<Stone> stones) {
		if (stones.size() == 0) return new ArrayList<>();
		
		Stone first = stones.get(0);
		Stone last = stones.get(stones.size() - 1);
		
		first.bounds.x -= first.bounds.width * 2;
		last.bounds.x += last.bounds.width * 2;
		stones.add(0, first);
		stones.add(last);
		
		List<Gap> ret = new ArrayList<>();
		
		if (stones.size() <= 1) return ret;
		
		//sort stones
		//noinspection ComparatorCombinators
		Collections.sort(stones, (one, two) -> Double.compare(one.x, two.x));
		
		//iterate over stones and make gaps
		for (int i = 0; i < stones.size() - 1; i++) {
			int gap = stones.get(i + 1).bounds.x -
					          (stones.get(i).bounds.x + stones.get(i).bounds.width);
			
			if (gap > 0) {
				ret.add(new Gap((int) stones.get(i).y,
						stones.get(i).bounds.x + stones.get(i).bounds.width,
						stones.get(i + 1).bounds.x));
			}
		}
		
		return ret;
	}
}
