package PhylogenStats;
/**
 * Class: Sequence
 *
 * Description: Utility class for PhylogenStats package
 *
 * Data Containers: Seq, SeqID, SeqCode, HDArray, TkArr, NN, Tk
 *
 *
 */



import java.util.ArrayList;


public class Sequence
{
    private String Seq;
    private static int ObjectCode = 0;
    private int SeqCode, Tk;
    private double NN;
    private ArrayList<Integer> TkArr;
    private double[] HDArray;
    private String Name;

    public Sequence(String Seq, String Name)
    {
        this.Name = Name;
        this.Seq = Seq;
        this.SeqCode = ObjectCode;
        ObjectCode++;
        TkArr = new ArrayList<Integer>();
    }

    public void setHDArray(double[] HDArray)
    {
        this.HDArray = HDArray;
    }

    public void setNN()
    {
        double test;
        if(SeqCode == 0)
        {
            test = HDArray[1];
        }
        else
        {
            test = HDArray[0];
        }
        for(int i = 0; i < HDArray.length; i++)
        {
            if( HDArray[i] <= test)
            {
                if( i != SeqCode)
                {
                    test = HDArray[i];
                }
            }
        }
        this.NN = test;
        setTk();
    }

    public void setName(String Name)
    {
        this.Name = Name;
    }

    public String getName()
    {
        return this.Name;
    }

    private void setTk()
    {
        int testTk = 0;
        for(int i = 0; i < HDArray.length; i++)
        {
            if(HDArray[i] == this.NN && i != SeqCode)
            {
                testTk++;
                TkArr.add(Integer.valueOf(i));
            }
        }
        this.Tk = testTk;
    }

    public boolean hasNeighbor(int code)
    {
        for(Integer Int : TkArr)
        {
            if(Int.intValue() == code)
            {
                return true;
            }
        }
        return false;
    }

    public int getTk()
    {
        return this.Tk;
    }

    public double getNN()
    {
        return this.NN;
    }

    public int getSeqCode()
    {
        return this.SeqCode;
    }

    public String getSeq()
    {
        return this.Seq;
    }

    public double getHD(int index)
    {
        return HDArray[index];
    }

    public String toString() { return getName() + '\n' + getSeq(); }

}
