package searchEngine;

//tested for lucene 7.7.3 and jdk13
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.TextField;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.Similarity;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.classification.utils.DocToDoubleVectorUtils; 


public class CreateTermDocMatrix {
	public static void main(String[] args) throws IOException, ParseException {
		try {
			//   Specify the analyzer for tokenizing text.
			//   The same analyzer should be used for indexing and searching
			//   Specify retrieval model (Vector Space Model)
			EnglishAnalyzer analyzer = new EnglishAnalyzer();
			Similarity similarity = new ClassicSimilarity();
			
			//   create the index
			Directory index = new RAMDirectory();
			
			IndexWriterConfig config = new IndexWriterConfig(analyzer);
			config.setSimilarity(similarity);

			FieldType type = new FieldType();
			type.setIndexOptions(IndexOptions.DOCS_AND_FREQS);
			type.setTokenized(true);
			type.setStored(true);
			type.setStoreTermVectors(true);
			
			IndexWriter writer = new IndexWriter(index, config);
			addDocWithTermVector(writer, "Lucene in Action", type);
			addDocWithTermVector(writer, "Lucene for Dummies", type);
			addDocWithTermVector(writer, "Managing Gigabytes", type);
			addDocWithTermVector(writer, "The Art of Computer Science", type);
			writer.close();
			
			IndexReader reader = DirectoryReader.open(index);

			testSparseFreqDoubleArrayConversion(reader);
			
			// searcher can only be closed when there
			// is no need to access the documents any more.
			reader.close();
			} 
			catch(Exception e){
				e.printStackTrace();
			}
		}
	
	private static void addDocWithTermVector(IndexWriter writer, String value, FieldType type) throws IOException {
		Document doc = new Document();
	    //TextField title = new TextField("title", value, Field.Store.YES);
		Field field = new Field("title", value, type);		
		doc.add(field);  //this field has term Vector enabled.
		writer.addDocument(doc);
	}
 
	private static void testSparseFreqDoubleArrayConversion(IndexReader reader) throws Exception {
		Terms fieldTerms = MultiFields.getTerms(reader, "title");   //the number of terms in the lexicon after analysis of the Field "title"
		System.out.println("Terms:" + fieldTerms.size());
		
		TermsEnum it = fieldTerms.iterator();						//iterates through the terms of the lexicon
		while(it.next() != null) {
			System.out.print(it.term().utf8ToString() + " "); 		//prints the terms
		}
		System.out.println();
		System.out.println();
		if (fieldTerms != null && fieldTerms.size() != -1) {
			IndexSearcher indexSearcher = new IndexSearcher(reader);
			for (ScoreDoc scoreDoc : indexSearcher.search(new MatchAllDocsQuery(), Integer.MAX_VALUE).scoreDocs) {   //retrieves all documents
				System.out.println("DocID: " + scoreDoc.doc);
				Terms docTerms = reader.getTermVector(scoreDoc.doc, "title");
				
				Double[] vector = DocToDoubleVectorUtils.toSparseLocalFreqDoubleArray(docTerms, fieldTerms); //creates document's vector
				
				NumberFormat nf = new DecimalFormat("0.#");
				for(int i = 0; i<=vector.length-1; i++ ) {
					System.out.print(nf.format(vector[i])+ " ");   //prints document's vector
				}
				System.out.println();
				System.out.println();
			}
		}
	}  

}

