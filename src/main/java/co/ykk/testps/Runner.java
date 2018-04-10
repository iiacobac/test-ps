package co.ykk.testps;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.json.JSONArray;
import org.json.JSONObject;

public class Runner {

	public static void main(String[] args) throws IOException, ParseException {
		Directory dir = indexLoader();

		IndexReader reader = DirectoryReader.open(dir);
		IndexSearcher searcher = new IndexSearcher(reader);
		Analyzer analyzer = new StandardAnalyzer();

		MultiFieldQueryParser parser = new MultiFieldQueryParser(new String[] { "title", "merchant", "description" }, analyzer);
		
		System.out.println("--------------------------------------------------------------------------------------");
		System.out.println("Input a free text query, or with format <field>:<query term>  i.e. \"title:Happy\"");
		System.out.println("Write \"--EXIT--\" to finish ");
		System.out.println("--------------------------------------------------------------------------------------");
		
		Scanner in = new Scanner(System.in);
		while (in.hasNextLine()) {
			String str = in.nextLine();
			if (str.equals("--EXIT--"))
				break;
			try {
				Query query = parser.parse(str);
				TopDocs hits = searcher.search(query, 10);

				for (ScoreDoc sd : hits.scoreDocs) {
					Document d = searcher.doc(sd.doc);
					System.out.println(sd.score);
					System.out.println(d.get("title"));
					System.out.println(d.get("description"));
					System.out.println(d.get("merchant"));
				}
			} catch (Exception e) {
				continue;
			}
		}
	}

	/**
	 * Document indexer 
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private static Directory indexLoader() throws FileNotFoundException, IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream("products.json.gz"))));

		// The file is a single line with a JSONArray.
		String str = reader.readLine();

		reader.close();

		JSONArray jsonArray = new JSONArray(str);

		Directory dir = new RAMDirectory();
		Analyzer analyzer = new StandardAnalyzer();
		IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

		iwc.setOpenMode(OpenMode.CREATE);

		IndexWriter writer = new IndexWriter(dir, iwc);

		// For each object in the JSONArray Lucene creates and index a document analyzing the field. 
		for (Object obj : jsonArray) {
			
			
			JSONObject json = (JSONObject) obj;
			Document product = new Document();

			Field title = new TextField("title", json.getString("title"), Field.Store.YES);
			product.add(title);
			Field description = new TextField("description", json.getString("description"), Field.Store.YES);
			product.add(description);
			Field merchant = new TextField("merchant", json.getString("merchant"), Field.Store.YES);
			product.add(merchant);
			writer.addDocument(product);
		}

		writer.close();

		return dir;
	}

}
