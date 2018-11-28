package PhylogenStats; /**
 * Created by Michael J. Bale on 12/15/2016.
 *
 * Analysis adapted from Hudson, Boos, and Kaplan 1992
 * USAGE: java <INFILE1>  *.fas </INFILE1> <INFILE2> *.fas </INFILE2> ...
 *
 * This analysis is (in brief) executed by determining the avg pairwise difference in
 * each population and having a weighted sum. Test of genetic divergence is done by
 * bootstrap to determine if the value found or more extreme happens by chance at less
 * than 5% of a time.
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.io.*;

public class Panmixia_v2
{
    /**
     * Generation of the array member of PhylogenStats.Sequence Class
     * Array is of number of differences between each sequence in the
     * Array List parameter
     * @param Sequences ArrayList of PhylogenStats.Sequence objects; contains all data passed
     *                  in to program via CLAs
     * @return Returns Array of HDarrays for each sequence object in Sequences
     */
    private static double[][] generateHDArr(ArrayList<Sequence> Sequences)
    {
        double[][] HDArr = new double[Sequences.size()][];
        for(int i = 0; i < Sequences.size(); i++)
        {
            HDArr[i] = new double[Sequences.size()];
            for(int j = 0; j < Sequences.size(); j++)
            {
                HDArr[i][j] = HammingDistance(Sequences.get(i).getSeq(), Sequences.get(j).getSeq());
            }
        }
        return HDArr;
    }

    private static double[][] generatelogHDArr(ArrayList<Sequence> Sequences)
    {
        double[][] HDArr = new double[Sequences.size()][];
        for(int i = 0; i < Sequences.size(); i++)
        {
            HDArr[i] = new double[Sequences.size()];
            for(int j = 0; j < Sequences.size(); j++)
            {
                HDArr[i][j] = Math.log(1+HammingDistance(Sequences.get(i).getSeq(), Sequences.get(j).getSeq()));
            }
        }
        return HDArr;

    }

    private static int HammingDistance(String seq1, String seq2)
    {
        int HD = 0;
        seq1 = seq1.toUpperCase();
        seq2 = seq2.toUpperCase();
        for (int i = 0; i<seq1.length(); i++)
        {
            if(seq1.charAt(i) != '-' && seq2.charAt(i) != '-')
            {
                if(seq1.charAt(i) != seq2.charAt(i))
                {
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
        Sequence[][] Resample = new Sequence [nLocals.length][];
        for(int i=0 ; i<nLocals.length-1; i++)
        {
            Resample[i] = new Sequence[nLocals[i]];
            for(int j = 0; j < nLocals[i]; j++)
            {
                Choice = PRNG.nextInt(N);
                Resample[i][j] = Seqs.get(Choice);
                N--;
                Seqs.remove(Choice);
            }
        }
        Resample[nLocals.length-1] = Seqs.toArray(new Sequence[0]);
        ArrayList<Sequence> BootStrapArr = new ArrayList<>();
        for(Sequence[] S : Resample)
        {
            Collections.addAll(BootStrapArr, S);
        }
        return BootStrapArr;
    }

    private static double calcKValue(Sequence[] Set)
    {
        double sum=0;
        for(int i = 0; i< Set.length; i++)
        {
            for(int j = i; j<Set.length; j++)
            {
                sum = sum + Set[i].getHD(Set[j].getSeqCode());
            }

        }
        return sum/(Set.length*(Set.length-1)/2);
    }

    private static double calcWeightFactor(int n, int N, int wf, int nLocals)
    {
        return (double) (n-wf)/(N-nLocals*wf);
    }

    private static double calcPanmixK(double[] KArr, double[] WArr)
    {
        double[] Panmix = new double[KArr.length];
        for(int i = 0; i < Panmix.length; i++)
        {
            Panmix[i] = KArr[i]*WArr[i];
        }
        return Sum(Panmix);
    }

    private static double Sum(double[] K)
    {
        double result =0;
        for(double k : K)
        {
            result+=k;
        }
        return result;
    }

    private static int Sum(int[] nLocals)
    {
        int sum = 0;
        for(int i : nLocals)
        {
            sum+=i;
        }
        return sum;
    }

    private static double pValue(double[] KArr, double baseK)
    {
        int counter = 0;
        for(double K : KArr)
        {
            if(K <= baseK)
            {
                counter++;
            }
        }
        return (double) counter/KArr.length;
    }

    private static double PanmixLoop(ArrayList<Sequence> Population, int[] nLocals, boolean wf)
    {
        double[] K = new double[nLocals.length], W = new double[nLocals.length];
        int CurrentPop = 0, bound = 0;
        for(int i = 0; i < nLocals.length; i++)
        {
            CurrentPop +=nLocals[i];
            Sequence[] tmpArr;
            ArrayList<Sequence> tmpAL = new ArrayList<>();
            for(int j = bound; j<CurrentPop; j++)
            {
                tmpAL.add(Population.get(j));

            }
            bound+=nLocals[i];
            tmpArr = tmpAL.toArray(new Sequence[0]);
            K[i] = calcKValue(tmpArr);
            if(!wf)
            {
                W[i] = calcWeightFactor(tmpArr.length,Population.size(), 0, nLocals.length);
            }
            if(wf)
            {
                W[i] = calcWeightFactor(tmpArr.length,Population.size(), 2, nLocals.length);
            }
        }
        return calcPanmixK(K,W);
    }

    private static void printResults(String[] args, int nReps, double K, double p1, double Ks, double p2, int[] nLocals)
    {
        System.out.println("           Results");
        System.out.println("-----------------------------");
        for(int i = 0; i < nLocals.length; i++)
        {
            System.out.printf("%s has Size: %d\n", args[i], nLocals[i]);
        }
        System.out.printf("Ks: %.7f\n", K);
        System.out.printf("p-Value using Integer p-Distance and %d Relabellings: %.7f\n", nReps, p1);
        System.out.printf("K*s: %.7f\n", Ks);
        System.out.printf("p-Value using Log p-Distance and %d Relabellings: %.7f\n", nReps, p2);

    }

    public static void main(String[] args)
    {
        int nReps;
        boolean hasNReps = false;
        try
        {
            nReps = Integer.parseInt(args[args.length-1]);
            hasNReps = true;
        }
        catch(NumberFormatException NFE)
        {
            nReps = 10000;
        }

        try
        {
            ArrayList<Sequence> Population = new ArrayList<>();
            int[] nLocals;
            if(hasNReps)
            {
                nLocals = new int[args.length-1];
            }
            else
            {
                nLocals = new int[args.length];
            }
            for(int i = 0; i < args.length; i++)
            {
                if(i == args.length-1 && hasNReps)
                {
                    break;
                }
                LineNumberReader lnr = new LineNumberReader(new FileReader(args[i]));
                lnr.skip(Long.MAX_VALUE);
                int length = (int) lnr.getLineNumber() + 1;
                lnr.close();
                length=length/2;
                nLocals[i] = length;
                BufferedReader in = new BufferedReader(new FileReader(args[i]));
                for(int j = 0; j < length; j++)
                {
                    String Name = in.readLine();
                    Sequence tmp = new Sequence(in.readLine(), Name);
                    Population.add(tmp);
                }
                in.close();
            }
            double[][] HDArr = generateHDArr(Population);
            double[][] LogArr = generatelogHDArr(Population);
            for(int i = 0; i < Population.size(); i++)
            {
                (Population.get(i)).setHDArray(HDArr[i]);
            }
            double BaseK = PanmixLoop(Population, nLocals, false);
            double[] BootK = new double[nReps];
            for(int i = 0; i < nReps; i++)
            {
                BootK[i] = PanmixLoop(Bootstrap(Population,nLocals), nLocals, false);
            }
            double p1 = pValue(BootK, BaseK);
            for(int j = 0; j < Population.size(); j++)
            {
                (Population.get(j)).setHDArray(LogArr[j]);
            }
            double baseKs = PanmixLoop(Population, nLocals, true);
            BootK = new double[nReps];
            for(int i = 0; i < nReps; i++)
            {
                BootK[i] = PanmixLoop(Bootstrap(Population,nLocals),nLocals, true);
            }
            double p2 = pValue(BootK, baseKs);

            printResults(args, nReps, BaseK, p1, baseKs, p2, nLocals);



        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

    }
}
