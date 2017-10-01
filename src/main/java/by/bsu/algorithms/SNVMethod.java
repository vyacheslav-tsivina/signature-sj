package by.bsu.algorithms;

import by.bsu.model.SNVStructure;
import by.bsu.model.Sample;
import by.bsu.util.Utils;

import java.io.IOException;

public class SNVMethod {
    static String al = "ACGT-";

    public long run(Sample sample, SNVStructure struct, Sample src) throws IOException {
        //TODO remove allHits
        int[][] allHits = new int[sample.sequences[0].length()][];
        int count = 0;
        for (int i = 0; i < sample.sequences[0].length(); i++) {
            int l = struct.rowMinors[i].length;
            if (l < 10) {
                continue;
            }
            int[] hits = getHits(sample.sequences[0], struct, struct.rowMinors[i], l);
            allHits[i] = hits;
            for (int j = 0; j < hits.length; j++) {
                //skip small amount of hits
                if (i != j && hits[j] >= 10 && Math.abs(i-j) > 5) {
                    //get unsplitted columns, minors, o_kl
                    int first = i / 4;
                    int second = j / 4;
                    int allele1 = i % 4 >= Utils.getMajorAllele(struct.profile, first) ? i % 4 + 1 : i % 4;
                    int allele2 = j % 4 >= Utils.getMajorAllele(struct.profile, second) ? j % 4 + 1 : j % 4;
                    char m1 = al.charAt(allele1);
                    char m2 = al.charAt(allele2);

                    int o22 = hits[j];
                    int o21 = struct.rowMinors[i].length-hits[j]; //both 21 and 2N
                    int o12 = struct.rowMinors[j].length-hits[j]; // both 12 and N2
                    // subtract 2N from o21
                    for (int k = 0; k < struct.rowMinors[i].length; k++) {
                        if (src.sequences[struct.rowMinors[i][k]].charAt(second) == 'N'){
                            o21--;
                        }
                    }
                    //subtract N2 from o12
                    for (int k = 0; k < struct.rowMinors[j].length; k++) {
                        if (src.sequences[struct.rowMinors[j][k]].charAt(first) == 'N'){
                            o12--;
                        }
                    }

                    int o11 = sample.sequences.length - o12; //both 11 and 1N
                    int minNColumn = struct.rowN[first].length < struct.rowN[second].length ? first : second;
                    int maxNColumn = struct.rowN[first].length > struct.rowN[second].length ? first : second;
                    //subtract 1N from 011
                    for (int k = 0; k < struct.rowN[minNColumn].length; k++) {
                        if (src.sequences[struct.rowN[minNColumn][k]].charAt( maxNColumn) == 'N'){
                            o11--;
                        }
                    }
                    //amount of common reads for i and j column
                    int reads = o11+o12+o21+o22;
                    //start calculate p-value, starting with p
                    //double p = struct.rowMinors[i].length/(double)(sample.sequences.length - struct.rowN[first].length);
                    double p = (o12*o21)/((double)o11*reads);
                    if (p < 1E-12) {
                        System.out.println(String.format("%d %d %c %c m1=%d m2=%d hits=%.2f p=%.3e zero",
                                first, second, m1, m2, l, struct.rowMinors[j].length, 100 * hits[j] / (double) l, p));
                        count++;
                    }else{

                        double pvalue = Utils.binomialPvalue(o22, p, reads);
                        if (pvalue < 0.01/(reads*(reads-1)/2)){
                            System.out.println(String.format("%d %d %c %c m1=%d m2=%d hits=%.2f p=%.3e",
                                    first, second, m1, m2, l, struct.rowMinors[j].length, 100 * hits[j] / (double) l, pvalue));
                            count++;
                        }
                    }

                }
            }
        }
        System.out.println(count);
        return 0;
    }

    private int[] getHits(String sequence, SNVStructure struct, int[] rowMinor, int l) {
        int[] hits = new int[sequence.length()];

        for (int j = 0; j < l; j++) {
            int[] column = struct.colMinors[rowMinor[j]];
            for (int aColumn : column) {
                hits[aColumn]++;
            }
        }
        return hits;
    }

    //TODO remove method, it's just for debugging
    private int getHitsBetweenPositions(int i, int j, char m1, char m2, int[][] allHits, SNVStructure struct){
        int allele1 = al.indexOf(m1) >= Utils.getMajorAllele(struct.profile, i) ? al.indexOf(m1) - 1 : al.indexOf(m1);
        int allele2 = al.indexOf(m2) >= Utils.getMajorAllele(struct.profile, j) ? al.indexOf(m2) - 1 : al.indexOf(m2);
        int first = i*4+allele1;
        int second = j*4+allele2;
        return allHits[first][second];
    }
}
