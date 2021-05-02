package myLuceneApp;

// tested for lucene 7.7.2 and jdk13
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Paths;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.FSDirectory;

import txtparsing.Doc;
import txtparsing.Question;
import txtparsing.TXTParsing;

public class Searcher {
    
    public Searcher(){
        try{
            String indexLocation = ("index"); //define where the index is stored (from IndexerDemo!!!)
            String queriesName = "docs/CISI.QRY";
            String resultsName = "docs/resultsCISI.txt";
            String field = "content"; //define which field will be searched            
            
            //Access the index using indexReaderFSDirectory.open(Paths.get(index))
            IndexReader indexReader = DirectoryReader.open(FSDirectory.open(Paths.get(indexLocation))); //IndexReader is an abstract class, providing an interface for accessing an index.
            IndexSearcher indexSearcher = new IndexSearcher(indexReader); //Creates a searcher searching the provided index, Implements search over a single IndexReader.
            indexSearcher.setSimilarity(new ClassicSimilarity());
            
            File resultsFile = new File(resultsName);
            resultsFile.createNewFile();
            FileWriter writer = new FileWriter(resultsFile);
            
            List<Question> questions = TXTParsing.parseQueries(queriesName);
        	System.out.println("Questions " + questions.size());


            for (Question q : questions){

            	//Search the index using indexSearcher
            	TopDocs results = search(indexSearcher, field, q.getQuery());
            	ScoreDoc[] hits = results.scoreDocs;
            	System.out.println(hits.length);
            	for(ScoreDoc hit: hits) {
            		Document hitdoc = indexSearcher.doc(hit.doc);
            		writer.write(q.getId() + " Q0 " + hitdoc.get("id") + " 0 " + hit.score + " STANDARD\n");
            		System.out.print(q.getId() + " Q0 " + hitdoc.get("id") + " 0 " + hit.score + " STANDARD\n");
            	}
            }
            TopDocs results = search(indexSearcher, "title", "Selective Dissemination of Information");
            ScoreDoc[] hits = results.scoreDocs;
            System.out.println(hits.length);
            System.out.println(results.totalHits);
            for(ScoreDoc hit: hits) {
            	Document hitdoc = indexSearcher.doc(hit.doc);
            	writer.write( "-1 Q0 " + hitdoc.get("id") + " 0 " + hit.score + " STANDARD\n");
            	System.out.print( "-1 Q0 " + hitdoc.get("id") + " 0 " + hit.score + " STANDARD\n");
            }
            writer.close();
            
            
            //Close indexReader
            indexReader.close();
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    
    /**
     * Searches the index given a specific user query.
     */
    private TopDocs search(IndexSearcher indexSearcher, String field, String question){
        try{
            // define which analyzer to use for the normalization of user's query
            Analyzer analyzer = new StandardAnalyzer();
            
            // create a query parser on the field "contents"
            QueryParser parser = new QueryParser(field, analyzer);
            parser.setAllowLeadingWildcard(true);
            
            Query query = parser.parse(QueryParser.escape(question));
            System.out.println("Searching for: " + query.toString(field));
            
            // search the index using the indexSearcher
            TopDocs results = indexSearcher.search(query, 100);
            
        	/*ScoreDoc[] hits = results.scoreDocs;
        long numTotalHits = results.totalHits;
        System.out.println(numTotalHits + " total matching documents");

        //display results
        for(int i=0; i<hits.length; i++){
            Document hitDoc = indexSearcher.doc(hits[i].doc);
            System.out.println("\tScore "+hits[i].score +"\ttitle="+hitDoc.get("title")+"\tcaption:"+hitDoc.get("caption")+"\tmesh:"+hitDoc.get("mesh"));
        }*/
            
            return results;

        } catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Initialize a SearcherDemo
     */
    public static void main(String[] args){
        Searcher searcherDemo = new Searcher();
    }
}
