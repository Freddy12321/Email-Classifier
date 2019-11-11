package emailClassifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SparseInstance;
import weka.core.converters.ArffSaver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MakeTrainingData{
	String folderAddress;
	PreProcessing p;
	ArrayList<String[]> emailText;
	ArrayList<Integer> emailsSpam;
	String[] stopWords;
	ArrayList<Attribute> attributes;
	StanfordLemmatizer lem= new StanfordLemmatizer();
	//class constructor, initialises class variables and sets folder containing training emails.
	public MakeTrainingData(String f){
		folderAddress=f;
		emailText= new ArrayList<String[]>();
		emailsSpam=new ArrayList<Integer>();
		attributes= new ArrayList<Attribute>();
	}
	/*
	 * iterates through the emails in the directory provided containing training emails. For each path PreProcess.getEmail is called
	 * to retrieve email contents and whether of not the email is spam. This information is stored in a temporary string array. The 
	 * contents of the email (subject and body) are then passed to processText, and the resulting string[] is then stored in emailsText.
	 * temp[1] is then used to determine if the the email is spam and this information is stored in emailsSpam. Once all emails have been 
	 * processed makeArff is called.
	 */
	public void preProcessEmails() throws IOException {
		Iterator<Path> it= Files.list(new File(folderAddress).toPath()).iterator();
		p=new PreProcessing();
		while(it.hasNext()) {
			Path path = it.next();
			String[] temp=p.getEmail(path);
			String emailTextString=temp[0];
			String[] emailTextSplit=processText(emailTextString);
			emailText.add(emailTextSplit);
			if(temp[1].contains("1")) {
				emailsSpam.add((Integer)(1));
			}
			else {
				emailsSpam.add((Integer)(0));
			}
		}
		makeArff("arffarff.arff");
		
	}
	/*
	 * processText takes in a string to process. first it lowercases the string and removes all non-alpha-numeric characters,
	 * it then removes duplicate words, stop words, and lemmatizes the words. The processed text is then returned as a String[].
	 */
	public String[] processText(String in) {
		String[] out;
		in.toLowerCase();
		in=in.replaceAll("[^a-zA-Z0-9 ]", "");
		out= in.split("\\s");
		out=removeDuplicate(out);
		out=removeStopWords(out);
		out=Lemmatize(out);
		return out;
	}
	//lemmtizes a String[] using the Stanford Lemmatizer.
	public String[] Lemmatize(String[] in) {
		String words="";
		for(int i=0; i<in.length;i++) {
			words=words.concat(in[i]+" ");
		}
		lem.lemmatize(words);
		return words.split("//s");
	}
	//sets the classwide stopWords.
	public void setStopWords(String[] sw) {
		stopWords=sw;
	}
	//imports a list of stopwords to be used by removeStopWords.
	public String[] getStopWords() throws IOException {
		String stW="";
		Scanner in = new Scanner(Paths.get("stopwords.txt"));
		while(in.hasNext()) {
			stW=stW+" "+in.nextLine();
		}
		in.close();
		return stW.split("//s");
	}
	//removes stopWords and compacts the String[] before returning it.
	public String[] removeStopWords(String[] init) {
		String[] noStop;
		int length = init.length;
		for(int i=0;i<init.length;i++) {
			for(int j=0; j<stopWords.length;j++) {
				if(init[i].equals(stopWords[j])) {
					init[i]=null;
					length--;
				}
			}
		}
		int j=0;
		noStop=new String[length];
		for(int i=0;i<init.length;i++) {
			if(init[i]!=null) {
				noStop[j]=init[i];
				j++;
			}
		}
		return noStop;
	}
	//removes duplicate words then compacts the String[] before returning it.
	public String[] removeDuplicate(String[] init) {
		String[] noDup;
		int length=init.length;
		for(int i=0;i<init.length;i++) {
			if(init[i]!=null) {
				for(int j=i+1;j<init.length;j++) {
					if(init[j]!=null) {
						if(init[i].contentEquals(init[j])) {
							init[j]=null;
							length--;
						}
					}
				}
			}
		}
		noDup=new String[length];
		int j=0;
		for(int i=0;i<init.length;i++) {
			if(init[i]!=null) {
				noDup[j]=init[i];
				j++;
			}
		}
		return noDup;
	}
	//makes a .arff file and calls writingArff to get the instances to put in it. these instances are
	//then put into the .arff using ArffSaver .setInstances and .writeBatch.
	public void makeArff(String fileName) throws IOException {
		File f =  new File(fileName);
		if(f.exists()) {
			f.delete();
			f.createNewFile();
		}
		else {
			f.createNewFile();
		}
		Instances output=writingArff();
		ArffSaver out = new ArffSaver();
		out.setInstances(output);
		out.setFile(f);
		out.writeBatch();
	}
	/*
	 * retruns the Instances that are to be written in a .arff file. Each Instance has 2 values, a String containing the text
	 * to be written and a nominal value about whether or not the email is spam. The nominal values contain a dummy value because 
	 * SpareInstance will not write anyting is a value of 0 is provided. One Instance is created for every index of emailsText/emailsSpam
	 * and once all of them are added to Instances out, out is returned. 
	 */
	public Instances writingArff() {
		attributes.add(new Attribute("Text", true));
		List<String> nominal= new ArrayList<String>();
		nominal.add("dummy");
		nominal.add("spam");
		nominal.add("notSpam");
		attributes.add(new Attribute("Spam", nominal));
		Instances out=new Instances("Email",attributes, 0);
		double[] temp = new double[2];
		for(int i=0;i<emailText.size();i++) {
			for(int j=0; j<emailText.get(i).length;j++) {
				temp[0]=out.attribute(0).addStringValue(emailText.get(i)[j]+" ");
			}
			if(emailsSpam.get(i)==1) {
				temp[1]=attributes.get(1).indexOfValue("spam");
			}
			else {
				temp[1]=attributes.get(1).indexOfValue("notSpam");
			}
			Instance inst= new SparseInstance(1.0, temp);
			out.add(inst);
		}
		return out;
	}
}
