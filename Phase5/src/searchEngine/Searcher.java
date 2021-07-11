package searchEngine;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.search.similarities.MultiSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.FSDirectory;

import searchEngine.WordEmbeddingsSimilarity.Smoothing;
import txtparsing.DocSimilarity;
import txtparsing.Question;
import txtparsing.TXTParsing;

public class Searcher {
	private List<Document> docs;
	String field = "content"; // define which field will be searched

	public Searcher() {
		try {
			String indexLocation = ("index"); // define where the index is stored (from IndexerDemo!!!)
			String queriesName = "../docs/CISI.QRY";
			String resultsName = "../docs/resultsCISIPhase5_WV+LM_";

			// Access the index using indexReaderFSDirectory.open(Paths.get(index))
			IndexReader indexReader = DirectoryReader.open(FSDirectory.open(Paths.get(indexLocation)));
			IndexSearcher indexSearcher = new IndexSearcher(indexReader);
			
			
			//Load Embeddings
//			String word2VecPath = "index/embeddings.txt";
			String pretrainedModel = "index/pretrained/model.txt";
			Embeddings.loadModel(pretrainedModel);
			
			Similarity sim1 = new ClassicSimilarity();
            Similarity sim2 = new BM25Similarity();
            Similarity sim3 = new LMJelinekMercerSimilarity(0.7f);
            Similarity sim4 = new WordEmbeddingsSimilarity(Embeddings.embeddings, field, Smoothing.TF_IDF); //WV possibly W2V...
            Similarity[] sims = {sim3, sim4};
            Similarity multi_similarity = new MultiSimilarity(sims);
			indexSearcher.setSimilarity(multi_similarity);
	
			//Load Questions
			List<Question> questions = TXTParsing.parseQueries(queriesName);
			System.out.println("Questions: " + questions.size());
			
			//Load Documents
			
			docs = Reader.indexDocumentsList(indexReader);
			
			int[] numOfDocs = { 20, 30, 50 };
			
			for (int i = 0; i < numOfDocs.length; i++) {
				//Find top j Docs
				int j = numOfDocs[i];
				File resultsFile = new File(resultsName + j + ".txt");
				resultsFile.createNewFile();
				FileWriter writer = new FileWriter(resultsFile);
				
				//For every question
				for (Question q : questions) {
					String[] terms = analyse(q.getQuery()); //split to tokens
					double[] query_vector = Embeddings.toDenseAverageVector(terms); //query collective vector

					// Find documents with the most similarity
					List<DocSimilarity> similarity = search(query_vector, j);
					
					for (DocSimilarity hit : similarity) {
						writer.write(q.getId() + " Q0 " + hit.getId() + " 0 " + hit.getSimilarity() + " STANDARD\n");
						System.out.print(q.getId() + " Q0 " + hit.getId() + " 0 " + hit.getSimilarity() + " STANDARD\n");
					}
				}
				writer.close();
			}

			// Close indexReader
			indexReader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private String[] analyse(String question) {
		//Use EnglishAnalyser to transform query and extract text from lucene
		ArrayList<String> terms = new ArrayList<>();
		try {
			Analyzer analyzer = new EnglishAnalyzer();
			TokenStream stream = analyzer.tokenStream(null, question);
			CharTermAttribute charTermAttribute = stream.addAttribute(CharTermAttribute.class);
		
			stream.reset();
			while (stream.incrementToken()) {
			    terms.add(charTermAttribute.toString());
			}
			analyzer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return terms.toArray(new String[terms.size()]);
	}

	/**
	 * Searches the index given a specific user query.
	 */
	private List<DocSimilarity> search(double[] query_vector, int k) {
		List<DocSimilarity> similarity = new ArrayList<>();
		
		for(Document doc : docs) {
			//Get tokens of doc in String[]
			String[] tokens = StringUtils.split(doc.getField(field).stringValue()," ");
			double[] document_vector = Embeddings.toDenseAverageVector(tokens); //document collective vector
			DocSimilarity sm = new DocSimilarity(Integer.parseInt(doc.getField("id").stringValue()), Embeddings.cosineSimilarity(document_vector, query_vector));
			similarity.add(sm);
		}

		Collections.sort(similarity, new DocSimilarity.SimilarityComparator());
		similarity = similarity.subList(0, k); //keep only k numberOfDocs
		return similarity;
	}

	/**
	 * Initialize a Searcher
	 */
	public static void main(String[] args) {
		Searcher searcher = new Searcher();
	}
}
