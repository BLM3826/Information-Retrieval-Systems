package searchEngine;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Collection;

import org.deeplearning4j.models.embeddings.learning.impl.elements.CBOW;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.text.sentenceiterator.BasicLineIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;

public class Embeddings {
	public static Word2Vec embeddings;
	
	public static void createWord2Vec(SentenceIterator iterator) {
		embeddings = new Word2Vec.Builder()
				.layerSize(100).windowSize(5).iterate(iterator)
				.elementsLearningAlgorithm(new CBOW<>()).build();
		
		embeddings.fit();
	}
	
	public static double[] toDenseAverageVector(String[] terms) {
		return embeddings.getWordVectorsMean(Arrays.asList(terms)).toDoubleVector();
	}
	
	public static double cosineSimilarity(double[] s1, double[] s2) {
		double dotProduct = 0.0;
	    double normA = 0.0;
	    double normB = 0.0;
	    for (int i = 0; i < s1.length; i++) { //s1.length = 100
	        dotProduct += s1[i] * s2[i];
	        normA += Math.pow(s1[i], 2);
	        normB += Math.pow(s2[i], 2);
	    }   
	    return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
		
	}
	
	public static void saveModel(String path) {
		WordVectorSerializer.writeWord2VecModel(embeddings, path);
	}
	
	public static void loadModel(String path) {
		embeddings = WordVectorSerializer.readWord2VecModel(path);
	}
	
	public static void main(String[] args) {
		String txtfile =  "../docs/CISI.ALL";
		SentenceIterator iter;
		try {
//			String txtfile =  new ClassPathResource("../docs/CISI.ALL").getFile().getAbsolutePath();
			iter = new BasicLineIterator(txtfile);
			createWord2Vec(iter);
			String[] words = new String[] { "literature", "technology","scientists","academic","universities"};
			for (String w : words) {
				Collection<String> lst = embeddings.wordsNearest(w,5);
				System.out.println("5 words closest to "+w+": "+lst);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
