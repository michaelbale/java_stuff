package PhylogenStats;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Created by andresbale on 12/17/16.
 */
public class NearestNeighbor
{

    private static double[][] generateHDArr(ArrayList<Sequence> Sequences)
    {
        double[][] HDArr = new double[Sequences.size()][];
        for (int i = 0; i < Sequences.size(); i++) {
            HDArr[i] = new double[Sequences.size()];
            for (int j = 0; j < Sequences.size(); j++) {
                HDArr[i][j] = HammingDistance(Sequences.get(i).getSeq(), Sequences.get(j).getSeq());
            }
        }
        return HDArr;
    }

    private static int HammingDistance(String seq1, String seq2)
    {
        int HD = 0;
        seq1 = seq1.toUpperCase();
        seq2 = seq2.toUpperCase();
        for (int i = 0; i < seq1.length(); i++) {
            if (seq1.charAt(i) != '-' && seq2.charAt(i) != '-') {
                if (seq1.charAt(i) != seq2.charAt(i)) {
                    HD++;
                }
            }
        }
        return HD;
    }

    private static ArrayList<Sequence> Bootstrap(ArrayList<Sequence> SeqSet, int[] nLocals)
    {
        ArrayList<Sequence> Seqs = new ArrayList<>(SeqSet);
        int N = SeqSet.size();
        int Choice;
        Random PRNG = new Random();
        Sequence[][] Resample = new Sequence[nLocals.length][];
        for (int i = 0; i < nLocals.length - 1; i++) {
            Resample[i] = new Sequence[nLocals[i]];
            for (int j = 0; j < nLocals[i]; j++) {
                Choice = PRNG.nextInt(N);
                Resample[i][j] = Seqs.get(Choice);
                N--;
                Seqs.remove(Choice);
            }
        }
        Resample[nLocals.length - 1] = Seqs.toArray(new Sequence[0]);
        ArrayList<Sequence> BootStrapArr = new ArrayList<>();
        for (Sequence[] S : Resample) {
            Collections.addAll(BootStrapArr, S);
        }
        return BootStrapArr;
    }

    private static double NearNeighborLoop(ArrayList<Sequence> Population, int[] nLocals)
    {
        int[] Wk = new int[Population.size()], Tk = new int[Population.size()];
        for(int i = 0; i  < Population.size(); i++)
        {
            Tk[i] = Population.get(i).getTk();
        }
        int CurrentPop = 0, bound = 0;
        int count = 0;
        for(int i = 0; i < nLocals.length; i++) {
            CurrentPop += nLocals[i];
            ArrayList<Sequence> tmpAL = new ArrayList<>();
            for (int j = bound; j < CurrentPop; j++) {
                tmpAL.add(Population.get(j));

            }
            for (int k = 0; k < nLocals[i]; k++) {
                Wk[count] = findWk(tmpAL.get(k), tmpAL);
                count++;
            }
            bound += nLocals[i];
        }
        return CalcSNN(Wk, Tk);
    }

    private static int findWk(Sequence Seq, ArrayList<Sequence> Locality)
    {
        int Wk = 0;
        for(Sequence S : Locality)
        {
            if(Seq.hasNeighbor(S.getSeqCode()))
            {
                Wk++;
            }
        }
        return Wk;
    }

    private static double CalcSNN(int[] Wk, int[] Tk)
    {
        int N = Wk.length;
        double[] Xk = new double[N];
        for(int i = 0; i < N; i++)
        {
            Xk[i] = (double) Wk[i]/Tk[i];
        }
        double Sum = 0;
        for(double X : Xk)
        {
            Sum+=X;
        }
        return Sum/N;
    }

    private static double pVal(double[] Arr, double Base)
    {
        int counter = 0;
        for(double S : Arr)
        {
            if (S >= Base)
            {
                counter++;
            }
        }

        return (double) counter / Arr.length;
    }

    public static void printResults(String[] args, int nReps, double Base, double pVal, int[] nLoc)
    {
        System.out.println("           Results");
        System.out.println("-----------------------------");
        for(int i = 0; i < nLoc.length; i++)
        {
            System.out.printf("%s has Size: %d\n", args[i], nLoc[i]);
        }
        System.out.printf("Snn: %.7f\n", Base);
        System.out.printf("p-Value using %d Relabellings: %.7f\n", nReps, pVal);
    }


    public static void main(String[] args) {
        int nReps;
        boolean hasNReps = false;
        try {
            nReps = Integer.parseInt(args[args.length - 1]);
            hasNReps = true;
        } catch (NumberFormatException NFE) {
            nReps = 10000;
        }

        try {
            ArrayList<Sequence> Population = new ArrayList<>();
            int[] nLocals;
            if (hasNReps) {
                nLocals = new int[args.length - 1];
            } else {
                nLocals = new int[args.length];
            }
            for (int i = 0; i < args.length; i++) {
                if (i == args.length - 1 && hasNReps) {
                    break;
                }
                LineNumberReader lnr = new LineNumberReader(new FileReader(args[i]));
                lnr.skip(Long.MAX_VALUE);
                int length = lnr.getLineNumber() + 1;
                lnr.close();
                length = length / 2;
                nLocals[i] = length;
                BufferedReader in = new BufferedReader(new FileReader(args[i]));
                for (int j = 0; j < length; j++) {
                    String Name = in.readLine();
                    Sequence tmp = new Sequence(in.readLine(), Name);
                    Population.add(tmp);
                }
                in.close();
            }
            double[][] HDArr = generateHDArr(Population);
            for (int i = 0; i < Population.size(); i++) {
                (Population.get(i)).setHDArray(HDArr[i]);
                (Population.get(i)).setNN();
            }
            double baseS = NearNeighborLoop(Population, nLocals);
            double[] bootS = new double[nReps];
            for (int i = 0; i < nReps; i++) {
                bootS[i] = NearNeighborLoop(Bootstrap(Population, nLocals), nLocals);
            }
            double pVal = pVal(bootS, baseS);
            printResults(args, nReps, baseS, pVal, nLocals);
        }
        catch(Exception E)
        {
            E.printStackTrace();
        }
    }
}
