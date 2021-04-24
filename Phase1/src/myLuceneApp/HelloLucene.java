package myLuceneApp;

// tested for lucene 7.7.3 and jdk13
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.Similarity;

import java.io.IOException;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;

public class HelloLucene {
  public static void main(String[] args) throws IOException, ParseException {
    // 0. Specify the analyzer for tokenizing text.
    //    The same analyzer should be used for indexing and searching
	//    Specify retrieval model (Vector Space Model)
    StandardAnalyzer analyzer = new StandardAnalyzer();
    Similarity similarity = new ClassicSimilarity();
    // 1. create the index
    Directory index = new RAMDirectory();

    IndexWriterConfig config = new IndexWriterConfig(analyzer);
    config.setSimilarity(similarity);
    
    IndexWriter writer = new IndexWriter(index, config);
    addDoc(writer, "Lucene in Action");
    addDoc(writer, "Lucene for Dummies");
    addDoc(writer, "Managing Gigabytes");
    addDoc(writer, "The Art of Computer Science");
    writer.close();

    // 2. query
    String querystr = args.length > 0 ? args[0] : "lucene";

    // the "title" arg specifies the default field to use
    // when no field is explicitly specified in the query.
    Query q = new QueryParser("title", analyzer).parse(querystr);

    // 3. search
    int hitsPerPage = 10;
    IndexReader reader = DirectoryReader.open(index);
    IndexSearcher searcher = new IndexSearcher(reader);
    TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage);
    searcher.search(q, collector);
    ScoreDoc[] hits = collector.topDocs().scoreDocs;
    
    // 4. display results
    System.out.println("Found " + hits.length + " hits.");
    for(int i=0;i<hits.length;++i) {
      int docId = hits[i].doc;
      Document d = searcher.doc(docId);
      System.out.println((i + 1) + ". " + d.get("title"));
    }

    // searcher can only be closed when there
    // is no need to access the documents any more. 
    reader.close();
    
  }

  private static void addDoc(IndexWriter writer, String value) throws IOException {
    Document doc = new Document();
    TextField title = new TextField("title", value, Field.Store.YES);
    doc.add(title);
    writer.addDocument(doc);
  }
}
