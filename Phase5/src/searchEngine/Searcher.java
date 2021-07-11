package searchEngine;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Paths;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.search.similarities.MultiSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.FSDirectory;

import txtparsing.Question;
import txtparsing.TXTParsing;

public class Searcher {
	String field = "content"; // define which field will be searched

	public Searcher() {
		try {
			String indexLocation = ("index"); // define where the index is stored (from IndexerDemo!!!)
			String queriesName = "../docs/CISI.QRY";
			String resultsName = "../docs/resultsCISIPhase5_BM+LM_";

			// Access the index using indexReaderFSDirectory.open(Paths.get(index))
			IndexReader indexReader = DirectoryReader.open(FSDirectory.open(Paths.get(indexLocation)));
			IndexSearcher indexSearcher = new IndexSearcher(indexReader);
			
			
			//Load Embeddings
//			String word2VecPath = "index/embeddings.txt";
//			String pretrainedModel = "index/pretrained/model.txt";
//			Embeddings.loadModel(pretrainedModel);
			
			Similarity sim1 = new ClassicSimilarity();
            Similarity sim2 = new BM25Similarity();
            Similarity sim3 = new LMJelinekMercerSimilarity(0.7f);
//            Similarity sim4 = new WordEmbeddingsSimilarity(Embeddings.embeddings, field, Smoothing.MEAN);
            Similarity[] sims = {sim2, sim3};
            Similarity multi_similarity = new MultiSimilarity(sims);
			indexSearcher.setSimilarity(multi_similarity);
	
			//Load Questions
			List<Question> questions = TXTParsing.parseQueries(queriesName);
			System.out.println("Questions: " + questions.size());
			

			
			int[] numOfDocs = { 20, 30, 50 };
			
			for (int i = 0; i < numOfDocs.length; i++) {
				//Find top j Docs
				int j = numOfDocs[i];
				File resultsFile = new File(resultsName + j + ".txt");
				resultsFile.createNewFile();
				FileWriter writer = new FileWriter(resultsFile);
				
				//For every question
				for (Question q : questions) {
					// Search the index using indexSearcher
					TopDocs results = search(indexSearcher, field, q.getQuery(), j);
					ScoreDoc[] hits = results.scoreDocs;
					System.out.println(hits.length);
					for (ScoreDoc hit : hits) {
						Document hitdoc = indexSearcher.doc(hit.doc);
						writer.write(q.getId() + " Q0 " + hitdoc.get("id") + " 0 " + hit.score + " STANDARD\n");
						System.out.print(q.getId() + " Q0 " + hitdoc.get("id") + " 0 " + hit.score + " STANDARD\n");
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
	

	/**
	 * Searches the index given a specific user query.
	 */
	private TopDocs search(IndexSearcher indexSearcher, String field, String question, int k) {
		try {
			// define which analyzer to use for the normalization of user's query
			Analyzer analyzer = new EnglishAnalyzer();

			// create a query parser on the field "content"
			QueryParser parser = new QueryParser(field, analyzer);
			parser.setAllowLeadingWildcard(true);

			Query query = parser.parse(QueryParser.escape(question));
			System.out.println("Searching for: " + query.toString(field));
			TopDocs results = indexSearcher.search(query, k);

			return results;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}


	/**
	 * Initialize a Searcher
	 */
	public static void main(String[] args) {
		Searcher searcher = new Searcher();
	}
}
