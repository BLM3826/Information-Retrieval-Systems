package searchEngine;

import java.io.FileNotFoundException;
import java.util.Collection;

import org.deeplearning4j.models.embeddings.learning.impl.elements.CBOW;
//import org.deeplearning4j.spark.models.embeddings.word2vec.Word2Vec;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.text.sentenceiterator.BasicLineIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.parallelism.AsyncIterator;

public class Embeddings {
	public static Word2Vec embeddings;
	
	public static void createWord2Vec(SentenceIterator iterator) {
		embeddings = new Word2Vec.Builder()
				.layerSize(100).windowSize(5).iterate(iterator)
				.elementsLearningAlgorithm(new CBOW<>()).build();
		
		embeddings.fit();
	}
	
	public static void main(String[] args) {
		String txtfile =  "../docs/CISI.ALL";
		SentenceIterator iter;
		try {
//			String txtfile =  new ClassPathResource("../docs/CISI.ALL").getFile().getAbsolutePath();
			iter = new BasicLineIterator(txtfile);
			createWord2Vec(iter);
			String[] words = new String[] { "hikking", "Greece","Rome","queen","doctor"};
			for (String w : words) {
				Collection<String> lst = embeddings.wordsNearest(w,5);
				System.out.println("5 words closest to "+w+": "+lst);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
