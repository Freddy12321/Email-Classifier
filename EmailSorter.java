package emailClassifier;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.SparseInstance;

public class EmailSorter {
	String dir;
	MakeTrainingData clas;
	WekaAlgorithm al;
	File spamFolder;
	File nonSpamFolder;
	File currentEmail;
	/*
	 * class constructor takes in the directory to sort, an instance of WekaAlgorithm, and an instance of MakeTrainingData 
	 * it uses the directory to create a spam and non-spam folder in the parent directory. It then iterates through the emails 
	 * in the directory provided and for each email the text it taken by getEmail. The text of each email is then processed
	 * using MakeTrainingData's processText method. The processed text is then used to create an Instances object that contains
	 * only one Instance object, the current email. The Instances object is then filtered using the filters form WekaAlgorithem
	 * and finally is classified by the WekaAlgorithm SMO classifier. The classifier outputs a 1 or a 2 depending on whether the 
	 * email was spam or no, this reult is then passed to fileEmail
	 */
	public EmailSorter(String d, WekaAlgorithm a, MakeTrainingData c) throws Exception {
		dir=d;
		al=a;
		clas=c;
		spamFolder=new File(dir);
		nonSpamFolder=new File(spamFolder.getParent()+"\\nonSpamFolder");
		nonSpamFolder.mkdir();
		spamFolder=new File(spamFolder.getParent()+"\\spamFolder");
		spamFolder.mkdir();
		Iterator<Path> iter =Files.list(Paths.get(dir)).iterator();
		while(iter.hasNext()) {
			currentEmail=new File(iter.next().toString());
			String[] email = null;
			try {
				String str=getEmail(currentEmail.toPath());
				email=clas.processText(str);
			}
			catch(IOException e){
				System.out.println("Something went wrong getting an email: "+e.getMessage());
			}
			if(email!=null) {
				Instances inst=makeInstance(email);
				inst=al.getSTWVFilter().useFilter(inst, al.getSTWVFilter());
				inst=al.getASFilter().useFilter(inst, al.getASFilter());
				double classified= al.getClassifier().classifyInstance(inst.firstInstance());
				fileEmail(classified);
			}
		}
	}
	/*
	 * determines whether the email is spam or not based off of the number passed to it. It then calls
	 * copyFile to copy the email to the relevant directory.
	 */
	public void fileEmail(double result) {
		File newFile;
		if(result==1) {
			newFile= new File(spamFolder.getPath().toString()+"\\"+currentEmail.getName());
			try {
				copyFile(newFile);
			}
			catch(IOException e) {
				System.out.println("Error copying file: "+e.getMessage());
			}
		}
		if(result==2) {
			newFile= new File(nonSpamFolder.getPath().toString()+"\\"+currentEmail.getName());
			try {
				copyFile(newFile);
			}
			catch(IOException e) {
				System.out.println("Error copying file: "+e.getMessage());
			}
		}
	}
	//copies currentEmail to the relevant directory
	public void copyFile(File target) throws IOException {
		Scanner in= new Scanner(currentEmail);
		FileWriter out = new FileWriter(target);
		out.flush();
		while(in.hasNext()) {
			out.write(in.nextLine()+"\n");
		}
		out.close();
		in.close();
	}
	/*
	 * makes an Instances using the data from an email, the nominal attribute of the Instance
	 * created in the Instances object is left null since it will be determined by the classifier.
	 * There is a dummy nominal value since SparseInstance would not record a nominal value of 0
	 */
	public Instances makeInstance(String[] data) {
		ArrayList<Attribute> attributes= new ArrayList<Attribute>();
		attributes.add(new Attribute("Text", true));
		List<String> nominal= new ArrayList<String>();
		nominal.add("dummy");
		nominal.add("spam");
		nominal.add("notSpam");
		attributes.add(new Attribute("Spam", nominal));
		Instances out=new Instances("Email",attributes, 0);
		double[] temp=new double[2];
		for(int i=0; i<data.length;i++) {
			temp[0]=attributes.get(0).addStringValue(data[i]+" ");
		}
		out.add(new SparseInstance(1.0, temp));
		return out;
	}
	//gets the contents of an email from a path and returns the contens as a string
	public String getEmail(Path path) throws IOException {
		String email="";
		Scanner in= new Scanner(path);
		while(in.hasNextLine()) {
			email=email+in.nextLine();
		}
		in.close();
		return email;
	}
}
