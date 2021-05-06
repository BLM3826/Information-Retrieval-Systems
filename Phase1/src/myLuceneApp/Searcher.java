package myLuceneApp;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.FSDirectory;

import txtparsing.Question;
import txtparsing.TXTParsing;

public class Searcher {

	public Searcher() {
		try {
			String indexLocation = ("index"); // define where the index is stored (from IndexerDemo!!!)
			String queriesName = "docs/CISI.QRY";
			String resultsName = "docs/resultsCISI_";
			String field = "content"; // define which field will be searched

			// Access the index using indexReaderFSDirectory.open(Paths.get(index))
			IndexReader indexReader = DirectoryReader.open(FSDirectory.open(Paths.get(indexLocation)));
			IndexSearcher indexSearcher = new IndexSearcher(indexReader);
			indexSearcher.setSimilarity(new ClassicSimilarity());


			List<Question> questions = TXTParsing.parseQueries(queriesName);
			System.out.println("Questions " + questions.size());
			int[] numOfDocs = { 20, 30, 50 };

			for (int i = 0; i < numOfDocs.length; i++) {
				int j = numOfDocs[i];
				File resultsFile = new File(resultsName + j + ".txt");
				resultsFile.createNewFile();
				FileWriter writer = new FileWriter(resultsFile);
				
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
