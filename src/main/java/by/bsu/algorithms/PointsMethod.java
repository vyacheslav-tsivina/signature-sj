package by.bsu.algorithms;

import by.bsu.model.Pair;
import by.bsu.model.Point;
import by.bsu.model.Points;
import by.bsu.model.Sample;

import com.carrotsearch.hppc.IntSet;
import com.carrotsearch.hppc.cursors.IntCursor;
import org.apache.commons.text.beta.similarity.HammingDistance;
import org.apache.commons.text.beta.similarity.LevenshteinDistance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Algorithm use points info to filter sequences from comparing
 */
public class PointsMethod {

    public static List<Pair> run(Sample sample1, Sample sample2, Points points1, Points points2, int k){
        List<Pair> closePairs = new ArrayList<>();
        LevenshteinDistance distance = new LevenshteinDistance(k);
        HammingDistance hammingDistance = new HammingDistance();
        int comps = 0;
        boolean sameLength = sample1.sequences.values().iterator().next().length() ==
                sample2.sequences.values().iterator().next().length();
        for (Map.Entry<Point, IntSet> point1 : points1.pointSeqMap.entrySet()){
            for (Map.Entry<Point, IntSet> point2 : points2.pointSeqMap.entrySet()){
                if (lowerBoundEstimate(point1.getKey().arr, point2.getKey().arr) <= k){
                    for ( IntCursor s1 : point1.getValue()){
                        for (IntCursor s2 : point2.getValue()){
                            comps++;
                            if (sameLength && hammingDistance.apply(sample1.sequences.get(s1.value),
                                    sample2.sequences.get(s2.value)) <= k){
                                closePairs.add(new Pair(s1.value, s2.value));
                                continue;
                            }
                            if (distance.apply(sample1.sequences.get(s1.value),
                                    sample2.sequences.get(s2.value)) != -1){
                                closePairs.add(new Pair(s1.value, s2.value));
                            }
                        }
                    }
                }
            }
        }
        System.out.println("comps = "+comps);
        System.out.println("length = "+closePairs.size());
        return closePairs;
    }

    private static int lowerBoundEstimate(int[] c1, int[] c2){
        int positive = 0;
        int negative = 0;
        for (int i = 0; i < 4; i++) {
            int dif = c1[i] - c2[i];
            if (dif > 0){
                positive += dif;
            } else{
                negative -= dif;
            }
        }
        return positive > negative ? positive : negative;
    }
}
