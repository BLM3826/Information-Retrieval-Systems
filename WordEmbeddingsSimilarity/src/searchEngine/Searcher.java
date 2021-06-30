package searchEngine;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.store.FSDirectory;

import txtparsing.DocSimilarity;
import txtparsing.Question;
import txtparsing.TXTParsing;

public class Searcher {
	private Terms fieldTerms;
	private HashMap<String, Integer> terms;

	public Searcher() {
		try {
			String indexLocation = ("index"); // define where the index is stored (from IndexerDemo!!!)
			String queriesName = "../docs/CISI.QRY";
			String resultsName = "../docs/resultsCISIPhase2_";
			String field = "content"; // define which field will be searched

			// Access the index using indexReaderFSDirectory.open(Paths.get(index))
			IndexReader indexReader = DirectoryReader.open(FSDirectory.open(Paths.get(indexLocation)));
//			IndexSearcher indexSearcher = new IndexSearcher(indexReader);
//			indexSearcher.setSimilarity(new ClassicSimilarity());
			fieldTerms = MultiFields.getTerms(indexReader, "content");
			System.out.println("Terms:" + fieldTerms.size());
			terms = new HashMap<String, Integer>();
			TermsEnum it = fieldTerms.iterator(); // iterates through the terms of the lexicon
			int pos = 0;
			while (it.next() != null) {
//				System.out.println(it.term().utf8ToString() + " "); // prints the terms
				terms.put(it.term().utf8ToString(), pos);
				++pos;
			}
			
			int[] ranks = {50,100,150,300};
			for(int rank : ranks) {
				
				//Reload SVD from files
				SVD.reloadVk("index/V"+rank+".txt");
				SVD.reloadUk("index/U"+rank+".txt");
				SVD.reloadSk("index/S"+rank+".txt");
	
				List<Question> questions = TXTParsing.parseQueries(queriesName);
				System.out.println("Questions: " + questions.size());
				int[] numOfDocs = { 20, 30, 50 };
				
				for (int i = 0; i < numOfDocs.length; i++) {
					int j = numOfDocs[i];
					File resultsFile = new File(resultsName + "rank" + rank + "_" + j + ".txt");
					resultsFile.createNewFile();
					FileWriter writer = new FileWriter(resultsFile);
					
					for (Question q : questions) {
						// Find documents with the most similarity
						List<DocSimilarity> results = search(q.getQuery(), j);
						for (DocSimilarity hit : results) {
							writer.write(q.getId() + " Q0 " + hit.getId() + " 0 " + hit.getSimilarity() + " STANDARD\n");
							System.out.print(q.getId() + " Q0 " + hit.getId() + " 0 " + hit.getSimilarity() + " STANDARD\n");
						}
					}
					writer.close();
				}
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
	private List<DocSimilarity> search(String question, int k) {
		try {
			//Initialise with zeros
			double[] queryVector = new double[(int)fieldTerms.size()];
			for(int i=0; i<queryVector.length; ++i) {
				queryVector[i] = 0;
			}
			
			// define which analyzer to use for the normalization of user's query
			Analyzer analyzer = new EnglishAnalyzer();
			TokenStream stream = analyzer.tokenStream(null, question);
			CharTermAttribute attr = stream.addAttribute(CharTermAttribute.class);
			stream.reset();
			while (stream.incrementToken()) {
				if (terms.containsKey(attr.toString())){
					int pos = terms.get(attr.toString()); //Get position of token in terms
					++queryVector[pos]; //Increment frequency of token					
				}
			}
			stream.end();
			stream.close();
			
			queryVector = SVD.transformQuery(queryVector);

			DocSimilarity cos_sim[] = SVD.cosineSimilarity(queryVector);
			
			Arrays.sort(cos_sim);
			//1460
			return Arrays.asList(cos_sim).subList(0, k + 1);

			// create a query parser on the field "content"
//			QueryParser parser = new QueryParser(field, analyzer);
//			parser.setAllowLeadingWildcard(true);
//
//			Query query = parser.parse(QueryParser.escape(question));
//			System.out.println("Searching for: " + query.toString(field));
//			TopDocs results = indexSearcher.search(query, k);

//			return cos_sim;

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
