package searchEngine;

// tested for lucene 7.7.2 and jdk13
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.search.similarities.MultiSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

import txtparsing.*;


public class Indexer {
    
    /**
     * Configures IndexWriter.
     * Creates a lucene's inverted index.
     *
     */
    public Indexer() throws Exception{
        
        String txtfile =  "../docs/CISI.ALL";
        String indexLocation = ("index"); //define were to store the index        
        
        Date start = new Date();
        try {
            System.out.println("Indexing to directory '" + indexLocation + "'...");
            
            Directory dir = FSDirectory.open(Paths.get(indexLocation));
            // define which analyzer to use for the normalization of documents
            Analyzer analyzer = new EnglishAnalyzer();
            // define retrieval model 
            Similarity sim1 = new ClassicSimilarity();
            Similarity sim2 = new BM25Similarity();
            Similarity sim3 = new LMJelinekMercerSimilarity(0.7f);
//            Similarity sim4 = new Similarity(); //WV possibly W2V...
            Similarity[] sims = {sim1, sim2};
            Similarity similarity = new MultiSimilarity(sims);
            
            // configure IndexWriter
            IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
            iwc.setSimilarity(similarity);

            // Create a new index in the directory, removing any
            // previously indexed documents:
            iwc.setOpenMode(OpenMode.CREATE);

            // create the IndexWriter with the configuration as above 
            IndexWriter indexWriter = new IndexWriter(dir, iwc);
            
            // parse txt document using TXT parser and index it
            List<Doc> docs = TXTParsing.parse(txtfile);
            for (Doc doc : docs){
                indexDoc(indexWriter, doc);
            }
            
            indexWriter.close();
            
            Date end = new Date();
            System.out.println(end.getTime() - start.getTime() + " total milliseconds");
            
        } catch (IOException e) {
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
        }
        
        
    }
    
    /**
     * Creates a Document by adding Fields in it and 
     * indexes the Document with the IndexWriter
     *
     * @param indexWriter the indexWriter that will index Documents
     * @param mydoc the document to be indexed
     *
     */
    private void indexDoc(IndexWriter indexWriter, Doc mydoc){
        
        try {
            
            // make a new, empty document
            Document doc = new Document();
            
            // create the fields of the document and add them to the document
            
            TextField id = new TextField("id", mydoc.getId()+"", Field.Store.YES);
            doc.add(id);
            TextField title = new TextField("title", mydoc.getTitle(), Field.Store.YES);
            doc.add(title);
            TextField author = new TextField("author", mydoc.getAuthor(), Field.Store.YES);
            doc.add(author);
            TextField content = new TextField("content", mydoc.getContent(), Field.Store.YES);
            doc.add(content);

            
            if (indexWriter.getConfig().getOpenMode() == OpenMode.CREATE) {
                // New index, so we just add the document (no old document can be there):
                System.out.println("adding " + mydoc);
                indexWriter.addDocument(doc);
            } 
        } catch(Exception e){
            e.printStackTrace();
        }
        
    }
    
    /**
     * Initializes an IndexerDemo
     */
    public static void main(String[] args) {
        try {
            new Indexer();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
}
