package searchEngine;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;


public class Reader {
    
    public Reader(){
        try{

            String indexLocation = ("index"); //define where the index is stored            
            
            //Access the index using indexReader
            IndexReader indexReader = DirectoryReader.open(FSDirectory.open(Paths.get(indexLocation))); //IndexReader is an abstract class, providing an interface for accessing an index.
            
            //Retrieve all docs in the index using the indexReader
//            IndexDocumentsList(indexReader);
            
            //Close indexReader
            indexReader.close();
            
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    
    /**
     * Retrieves all documents in the index using indexReader
     * @return 
     */
    static List<Document> indexDocumentsList(IndexReader indexReader){
    	List<Document> documents = new ArrayList<Document>();
        try {
            System.out.println("--------------------------");
            System.out.println("Documents in the index...");
            
            for (int i=0; i<indexReader.maxDoc(); i++) {
                Document doc = indexReader.document(i);
                documents.add(doc);
//                System.out.println("\tid="+doc.getField("id")+"\ttitle="+doc.getField("title")+"\tauthor:"+doc.get("author")+"\tcontent:"+doc.get("content"));
            }
        } catch (CorruptIndexException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return documents;
    }

    /**
     * Initialize a Reader
     */    
    public static void main(String[] args){
        Reader reader = new Reader();
    }
}
