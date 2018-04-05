import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


class Sequence {
    private String id;
    private String seq;

    Sequence(String id, String seq) {
        this.id = id;
        this.seq = seq;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setSeq(String seq) {
        this.seq = seq;
    }

    public String getId() {
        return id;
    }

    public String getSeq() {
        return seq;
    }

    @Override
    public String toString() {
        return this.getId() + "\n" + this.getSeq();
    }
}



public class renameSeqs {

    public static void main(String[] args) {
        try {
            LineNumberReader lnr = new LineNumberReader(new FileReader(args[0]));
            lnr.skip(Long.MAX_VALUE);
            int length = lnr.getLineNumber() + 1;
            lnr.close();
            length/=2;
            Sequence[] seqSet = new Sequence[length];
            BufferedReader in = new BufferedReader(new FileReader(args[0]));
            String tmp1 = "", tmp2 = "";
            for(int i = 0; i < length; i++)
            {
                tmp1 = in.readLine();
                tmp2 = in.readLine();
                seqSet[i] = new Sequence(tmp1, tmp2);
            }
            String regex = "^(>[^-]+-[0-9]+)(.*$)";
            Pattern r = Pattern.compile(regex);
            for(int j = 1; j < length+1; j++)
            {
                Matcher m = r.matcher(seqSet[j-1].getId());
                @SuppressWarnings("unused")
                boolean dummy = m.matches();
                String new_id = m.group(1) + "_" + String.format("%05d",j);
                seqSet[j-1].setId(new_id);
            }
            BufferedWriter out = new BufferedWriter(new FileWriter(args[0] + "_rename.fas"));
            for(Sequence seq : seqSet)
            {
                out.write(seq.toString() + '\n');
            }
            out.flush();
            out.close();
        } catch (IOException IO) {
            IO.printStackTrace();
        }
    }

}
