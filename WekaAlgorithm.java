package emailClassifier;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.Ranker;
import weka.classifiers.functions.SMO;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.core.stemmers.NullStemmer;
import weka.core.stopwords.Null;
import weka.core.tokenizers.WordTokenizer;
import weka.filters.unsupervised.attribute.StringToWordVector;

public class WekaAlgorithm {
	//class variables
	String input;
	Instances data;
	SMO classifier;
	StringToWordVector filter1;
	AttributeSelection filter2;
	/*
	 * class constructor, uses the name of a .arff file and then calls the relevant methods
	 * in the correct order in order to create an SMO classifier and filters
	 */
	public WekaAlgorithm(String in)throws Exception {
		input=in;
		getData();
		preProcess();
		runAlgorithem();
	}
	//grabs the data from the .arff file 
	public void getData() throws Exception {
		data= new Instances(new DataSource(input).getDataSet());
		if(data.classIndex()==-1) {
			data.setClassIndex(data.numAttributes()-1);
		}
	}
	/*
	 * initialises filters and makes sure that the settings are correct. then filters Instances data
	 * and sets data to equal the newly filtered Instances, this happens twice, once foe each filter.
	 */
	public void preProcess() throws Exception {
		filter1= new StringToWordVector();
		filter2= new AttributeSelection();
		filter1.setAttributeIndices("first-last");
		filter1.setWordsToKeep(10000);
		filter1.setPeriodicPruning(-1.0);
		filter1.setOutputWordCounts(true);
		filter1.setTFTransform(true);
		filter1.setIDFTransform(true);
		filter1.setStemmer(new NullStemmer());
		filter1.setStopwordsHandler(new Null());
		filter1.setMinTermFreq(1);
		WordTokenizer tokenizer=new WordTokenizer();
		tokenizer.setDelimiters("\\\" \\\\r\\\\n\\\\t.,;:\\\\\\'\\\\\\\"()?!\\\"");
		filter1.setTokenizer(tokenizer);
		filter1.setInputFormat(data);
		data=Filter.useFilter(data, filter1);
		InfoGainAttributeEval ev = new InfoGainAttributeEval();
		Ranker rank = new Ranker();
		rank.setNumToSelect(-1);
		rank.setThreshold(0);
		filter2.setEvaluator(ev);
		filter2.setSearch(rank);
		filter2.setInputFormat(data);
		data=Filter.useFilter(data, filter2);
	}
	/*
	 * used to create the SMO classifier using the filtered Instances of data
	 */
	public void runAlgorithem() throws Exception{
		classifier = new SMO();
		classifier.setNumFolds(5);
		classifier.buildClassifier(data);
	}
	//returns the SMO classifier
	public SMO getClassifier() {
		return classifier;
	}
	//returns the StringToWordVector filter
	public StringToWordVector getSTWVFilter() {
		return filter1;
	}
	//returns the AttributeSelection filter
	public AttributeSelection getASFilter() {
		return filter2;
	}
}
