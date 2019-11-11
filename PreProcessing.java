package emailClassifier;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Scanner;

public class PreProcessing {
	Scanner in;
	String[] eMail;
	String spam;
	/*
	 * grabs email and returns the email contents (subject and body) as the first index in a string array,
	 * then returns whether not the email is spam (1 for spam, 0 for non-spam) as the second instance in the
	 * string array.
	 */
	public String[] getEmail(Path d) throws IOException{
		in= new Scanner(d);
		eMail= new String[2];
		eMail[0]="";
		while(in.hasNextLine()) {
			eMail[0]=eMail[0]+" "+in.nextLine();
		}
		if(d.toString().contains("spmsg")) {
			spam="1";
		}
		else {
			spam="0";
		}
		eMail[1]=spam;
		in.close();
		return eMail;
	}
}
