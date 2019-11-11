package emailClassifier;

import java.io.IOException;
import java.util.Scanner;

public class Main {
	/*
	 * The main creates instances and calls the other classes in the right order, it also grabs user input for 
	 * the training and testing directories to use. If no training directory is provided the contents of the 
	 * current arffarff.arff file is used
	 */
	public static void main(String[] args) {
		String fileName="ARFARF.txt";
		String trainPath="";
		trainPath=userInput("Please enter path to folder holding emails to train on. If null current .arff file will be used");
		String sortPath="";
		while(sortPath.isEmpty()) {
			sortPath=userInput("Please enter path to folder holding emails to sort.");
		}
		WekaAlgorithm weka=null;
		MakeTrainingData p=new MakeTrainingData(trainPath);
		try {
			p.setStopWords(p.getStopWords());
		} catch (IOException e1) {
			System.out.println("Something went wrong getting stop words: " +e1.getMessage());
			e1.printStackTrace();
		}
		if(!trainPath.isEmpty()) {
			try {
				p.preProcessEmails();
				
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Something went wrong getting files "+e.getMessage());
			}
		}
		try {
			weka= new WekaAlgorithm("arffarff.arff");
		}
		catch(Exception e) {
			System.out.println("Something went wrong executing the algorithem: "+e.getMessage());
		}
		try {
			EmailSorter sort=new EmailSorter(sortPath,weka,p);
		}
		catch(Exception e) {
			System.out.println("Something went wrong classifying the emails: "+e.getMessage());
		}
	}
	public static String userInput(String prompt) {
		String input=null;
		Scanner in=new Scanner(System.in);
		System.out.println(prompt);
		input=in.nextLine();
		return input;
	}
}
