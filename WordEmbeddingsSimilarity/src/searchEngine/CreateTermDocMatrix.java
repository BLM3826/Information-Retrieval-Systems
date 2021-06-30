package searchEngine;

//tested for lucene 7.7.3 and jdk13
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import txtparsing.Doc;
import txtparsing.TXTParsing;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.classification.utils.DocToDoubleVectorUtils;

public class CreateTermDocMatrix {
	public static void main(String[] args) throws IOException, ParseException {
		try {
			String txtfile = "../docs/CISI.ALL";
			String indexLocation = ("index"); //define were to store the index 
			
			//................FROM INDEXER..............
			System.out.println("Indexing to directory '" + indexLocation + "'...");
            Directory dir = FSDirectory.open(Paths.get(indexLocation));
			// Specify the analyzer for tokenizing text.
			// The same analyzer should be used for indexing and searching
			// Specify retrieval model (Vector Space Model)
			EnglishAnalyzer analyzer = new EnglishAnalyzer();
			Similarity similarity = new ClassicSimilarity();
			
			
//			//---------------FROM INDEXER-----------------
//			IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
//            iwc.setSimilarity(similarity);
//            iwc.setOpenMode(OpenMode.CREATE);
//            IndexWriter indexWriter = new IndexWriter(dir, iwc);
			
			
			
			//---------------FROM HERE--------------------

			// create the index
//			Directory index = new RAMDirectory();

			IndexWriterConfig config = new IndexWriterConfig(analyzer);
			config.setSimilarity(similarity);
            config.setOpenMode(OpenMode.CREATE);

			FieldType type = new FieldType();
			type.setIndexOptions(IndexOptions.DOCS_AND_FREQS);
			type.setTokenized(true);
			type.setStored(true);
			type.setStoreTermVectors(true);

			IndexWriter writer = new IndexWriter(dir, config);
			List<Doc> docs = TXTParsing.parse(txtfile);
			for (Doc doc : docs) {
				indexDoc(writer, doc, type);
			}
			writer.close();

			IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexLocation))); 
			
			double[][] termXDoc = getSparseTermXDoc(reader);
//			for (int i = 0; i < termXDoc.length; ++i) {
//				for (int j = 0; j < termXDoc[0].length; ++j) {
//					System.out.print(termXDoc[i][j] + " ");
//				}
//				System.out.println(" ");
//			}

			int[] k = {50,100,150,300};
			SVD.computeSVD(termXDoc, k);

			//--------------------FOR SEARCHER-------------------
//			double query[] = {0,0,0,0,0,1,0,0}; //Search for Lucene
//			double query[] = { 1, 0, 0, 0, 0, 1, 0, 0 };
//			query = SVD.transformQuery(query);
//			for (double q : query) {
//				System.out.print(q + " ");
//			}
//			System.out.println("\n");
//
//			double cos_sim[] = SVD.cosineSimilarity(query);
//			for (int i = 0; i < cos_sim.length; ++i) {
//				System.out.println("Doc" + i + " = " + cos_sim[i]);
//			}

			// searcher can only be closed when there
			// is no need to access the documents any more.
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

//	private static void addDocWithTermVector(IndexWriter writer, String value, FieldType type) throws IOException {
//		Document doc = new Document();
//		// TextField title = new TextField("title", value, Field.Store.YES);
//		Field field = new Field("title", value, type);
//		doc.add(field); // this field has term Vector enabled.
//		writer.addDocument(doc);
//	}

	private static void indexDoc(IndexWriter indexWriter, Doc mydoc, FieldType type) throws IOException {
		// make a new, empty document
		Document doc = new Document();

		// create the fields of the document and add them to the document

		TextField id = new TextField("id", mydoc.getId() + "", Field.Store.YES);
		doc.add(id);
		TextField title = new TextField("title", mydoc.getTitle(), Field.Store.YES);
		doc.add(title);
		TextField author = new TextField("author", mydoc.getAuthor(), Field.Store.YES);
		doc.add(author);
		Field content = new Field("content", mydoc.getContent(), type);
		doc.add(content);

		if (indexWriter.getConfig().getOpenMode() == OpenMode.CREATE) {
			// New index, so we just add the document (no old document can be there):
			System.out.println("adding " + mydoc);
			indexWriter.addDocument(doc);
		}

	}

	private static double[][] getSparseTermXDoc(IndexReader reader) throws Exception {
		Terms fieldTerms = MultiFields.getTerms(reader, "content"); // the number of terms in the lexicon after analysis
																	// of the Field "title"
		System.out.println("Terms:" + fieldTerms.size());

		TermsEnum it = fieldTerms.iterator(); // iterates through the terms of the lexicon
		while (it.next() != null) {
			System.out.println(it.term().utf8ToString() + " "); // prints the terms
		}
		System.out.println();
		System.out.println();
		if (fieldTerms != null && fieldTerms.size() != -1) {
			ArrayList<double[]> tempList = new ArrayList<double[]>();

			IndexSearcher indexSearcher = new IndexSearcher(reader);
			for (ScoreDoc scoreDoc : indexSearcher.search(new MatchAllDocsQuery(), Integer.MAX_VALUE).scoreDocs) { // retrieves
																													// all
																													// documents
//				System.out.println("DocID: " + scoreDoc.doc);
				Terms docTerms = reader.getTermVector(scoreDoc.doc, "content");

				Double[] vector = DocToDoubleVectorUtils.toSparseLocalFreqDoubleArray(docTerms, fieldTerms); // creates
																												// document's
																												// vector
				// Double sad
				double v[] = new double[vector.length];
				for (int i = 0; i < vector.length; ++i) {
					v[i] = vector[i];
				}
				tempList.add(v);

//				NumberFormat nf = new DecimalFormat("0.#");
//				for (int i = 0; i <= vector.length - 1; i++) {
//					System.out.print(nf.format(vector[i]) + " "); // prints document's vector
//				}
//				System.out.println();
//				System.out.println();
			}

			double termXDoc[][] = new double[tempList.size()][];
			termXDoc = tempList.toArray(termXDoc);
			return termXDoc;
		}
		return null;
	}

}
