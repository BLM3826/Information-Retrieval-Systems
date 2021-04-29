package txtparsing;

import utils.IO;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TXTParsing {

    public static List<Doc> parse(String file) throws Exception {
    	List<Doc> parsed_docs= new ArrayList<Doc>();
        try{
            //Parse file
        	Scanner reader = new Scanner(new File(file));
        	String line;
            while (reader.hasNextLine()) {
            	line = reader.nextLine();
            	int id = -1;
            	String title="", author="", content="";
            	if(line.startsWith(".I")) {
            		id = Integer.parseInt(line.substring(3));
            	}
            	line = reader.nextLine();
            	if(line.startsWith(".T")){
            		title = reader.nextLine();
            		line = reader.nextLine();
            		while(!line.startsWith(".A")) {
            			title += "\n" + line;
            			line = reader.nextLine();
            		}
            	}
            	if(line.startsWith(".A")){
            		author = reader.nextLine();
            	}
            	line = reader.nextLine();
            	if(line.startsWith(".W")) {
            		content = reader.nextLine();
            		line = reader.nextLine();
            		while(!line.startsWith(".X")) {
            			content += " " + line;
            			line = reader.nextLine();
            		}
            		Doc doc = new Doc(id, title, author, content);
            		parsed_docs.add(doc);
            	}
            }
        	
        	
//            String txt_file = IO.ReadEntireFileIntoAString(file);
//            String[] docs = txt_file.split("\n");
//            System.out.println("Read: "+parsed_docs.length + " docs");

            //Parse each document from the txt file
//            List<MyDoc> parsed_docs= new ArrayList<MyDoc>();
//            for (String doc:docs){
//                String[] adoc = doc.split("/");
//                MyDoc mydoc = new MyDoc(adoc[0],adoc[1],adoc[2]);
//                parsed_docs.add(mydoc);
//            }

            return parsed_docs;
        } catch (Throwable err) {
            err.printStackTrace();
            return null;
        }
        
    }

    public static List<Question> parseQueries(String file){
    	System.out.println("Parsing Queries...");
    	List<Question> question = new ArrayList<Question>();
    	try {
        	Scanner reader = new Scanner(new File(file));
        	String line = reader.nextLine();
            while (reader.hasNextLine()) {
            	int id = -1;
            	String query="";
//            	List<String> queries = new ArrayList<String>();
            	
            	if(line.startsWith(".I")) {
            		id = Integer.parseInt(line.substring(3));
            		line = reader.nextLine();
                	
                	while(!line.startsWith(".W")) {
                		line = reader.nextLine();
                	}
	            	query = reader.nextLine();
//            		queries.add(query);
            		while(!line.startsWith(".I") && !line.startsWith(".B")) {
            			query += "\n" + line;
//            			queries.add(line);
            			line = reader.nextLine();
            		}
            		Question q = new Question(id, query);
            		question.add(q);
	            	
            	}else {
            		line = reader.nextLine();
            	}//endif
            	
            }//endwhile
            System.out.println("Finished Parsing!");
            return question;
    	}catch(FileNotFoundException e) {
    		e.printStackTrace();
    		return null;
    	}
    }
}
