import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.LinkedHashMap;
import java.util.Map;

public class SepFas
{
    public static void main (String[] args)
    {
        try
        {
            LineNumberReader lnr = new LineNumberReader(new FileReader(args[0]));
            lnr.skip(Long.MAX_VALUE);
            int length = lnr.getLineNumber() + 1;
            lnr.close();
            length/=2;
            BufferedReader in = new BufferedReader(new FileReader(args[0]));
            LinkedHashMap<String,String> SeqSet = new LinkedHashMap<String, String>();
            for(int i = 0; i < length; i++)
            {
                String Name = in.readLine();
                String Seq = in.readLine();
                SeqSet.put(Name,Seq);
            }
            Set<String> CARList = new HashSet<String>();
            for(Map.Entry<String,String> entry : SeqSet.entrySet())
            {
                String Key = entry.getKey();
                String[] Split1 = Key.split("-");
                if(!CARList.add(Split1[0]))
                {
                    CARList.add(Split1[0]);
                }
            }

            for(String CAR : CARList)
            {
                String FileName = CAR.substring(1);
                FileName +=".fas";
                BufferedWriter out = new BufferedWriter(new FileWriter(FileName));
                for(Map.Entry<String,String> entry : SeqSet.entrySet())
                {
                    String Key = entry.getKey() ;
                    if(Key.contains(CAR))
                    {
                        out.write(Key);
                        out.write('\n');
                        out.write(SeqSet.get(Key));
                        out.write('\n');
                    }
                }
                out.flush();
                out.close();
            }
        }
        catch(IOException IO)
        {
            IO.printStackTrace();
        }
        catch(Exception E)
        {
            E.printStackTrace();
        }
    }
}
