import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import java.lang.Math.* ;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

import org.apache.lucene.document.Document;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.ScoreDoc;

import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;

import org.apache.lucene.store.FSDirectory;

import org.apache.lucene.util.BytesRef;


public class compareAlgorithms {

	public static void main(String[] args) throws ParseException, IOException {

		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths
				.get("F:\\IUB DS Masters Subjects\\ILS - Z534 Search\\Homework\\Homework 2\\assignment__export\\index")));

		// Update the corpus location here
		String docsPath = "F:\\IUB DS Masters Subjects\\ILS - Z534 Search\\Homework\\Homework 2\\assignment__export\\Data\\topics.51-100";
		final Path docDir = Paths.get(docsPath);
		if (!Files.isReadable(docDir)) {
			System.out.println("Document directory '" + docDir.toAbsolutePath() + "' does not exist or is not readable, please check the path");
			System.exit(1);
		}

		System.out.println("------------");

		PrintWriter writer_VSM_S = new PrintWriter("F:\\IUB DS Masters Subjects\\ILS - Z534 Search\\Homework\\Homework 2\\Results\\TestSA\\VSMshortQuery.txt", "UTF-8");
		PrintWriter writer_VSM_L = new PrintWriter("F:\\IUB DS Masters Subjects\\ILS - Z534 Search\\Homework\\Homework 2\\Results\\TestSA\\VSMlongQuery.txt", "UTF-8");
		PrintWriter writer_BM25_S = new PrintWriter("F:\\IUB DS Masters Subjects\\ILS - Z534 Search\\Homework\\Homework 2\\Results\\TestSA\\BM25shortQuery.txt", "UTF-8");
		PrintWriter writer_BM25_L = new PrintWriter("F:\\IUB DS Masters Subjects\\ILS - Z534 Search\\Homework\\Homework 2\\Results\\TestSA\\BM25longQuery.txt", "UTF-8");
		PrintWriter writer_LMDS_S = new PrintWriter("F:\\IUB DS Masters Subjects\\ILS - Z534 Search\\Homework\\Homework 2\\Results\\TestSA\\LMDSshortQuery.txt", "UTF-8");
		PrintWriter writer_LMDS_L = new PrintWriter("F:\\IUB DS Masters Subjects\\ILS - Z534 Search\\Homework\\Homework 2\\Results\\TestSA\\LMDSlongQuery.txt", "UTF-8");
		PrintWriter writer_LMJM_S = new PrintWriter("F:\\IUB DS Masters Subjects\\ILS - Z534 Search\\Homework\\Homework 2\\Results\\TestSA\\LMJMshortQuery.txt", "UTF-8");
		PrintWriter writer_LMJM_L = new PrintWriter("F:\\IUB DS Masters Subjects\\ILS - Z534 Search\\Homework\\Homework 2\\Results\\TestSA\\LMJMlongQuery.txt", "UTF-8");

		List<String> queryList = trecParser(docDir); 
		System.out.println(queryList);
		

		for (int l = 0; l < queryList.size(); l++) {
			String[] queryStrings = queryList.get(l).split("--");

			System.out.println("!!!!!!!!!!!!!!!!!!" + queryStrings.length);
			
			for (int j = 0; j < queryStrings.length; j++) {
				
				String queryString = queryStrings[j];

				System.out.println(queryString + "------------");

				IndexSearcher searcher = new IndexSearcher(reader);

				// Vector Space Model Similarity
				searcher.setSimilarity(new ClassicSimilarity()); 		
				Analyzer analyzer = new StandardAnalyzer();
				QueryParser parser = new QueryParser("TEXT", analyzer);
				Query query = parser.parse(queryString);
				TopDocs topDocs = searcher.search(query, 1000);
				ScoreDoc[] hits = topDocs.scoreDocs;
				
				//System.out.println("-" + topDocs.getMaxScore());
				//System.out.println("-" + hits.length);
				//System.exit(1);
				

				System.out.println("----------------Vector Space Model Similarity-----------------");
				for (int i = 0; i < hits.length; i++) {
					Document doc = searcher.doc(hits[i].doc);
					if(l == 0 ) {
						writer_VSM_S.println(51 + j + " Q0 " + doc.get("DOCNO") + " " + i + " " + hits[i].score + " VSM_short");
					}
					else {
						writer_VSM_L.println(51 + j + " Q0 " + doc.get("DOCNO") + " " + i + " " + hits[i].score + " VSM_long");
					}

				}

				// BM25 Similarity
				searcher.setSimilarity(new BM25Similarity()); 		
				analyzer = new StandardAnalyzer();
				parser = new QueryParser("TEXT", analyzer);
				query = parser.parse(queryString);
				topDocs = searcher.search(query, 1000);
				hits = topDocs.scoreDocs;

				System.out.println("----------------BM25 Similarity-----------------");
				for (int i = 0; i < hits.length; i++) {
					Document doc = searcher.doc(hits[i].doc);
					if(l == 0 ) {
						writer_BM25_S.println(51 + j + " Q0 " + doc.get("DOCNO") + " " + i + " " + hits[i].score + " BM25_short");
					}
					else {
						writer_BM25_L.println(51 + j + " Q0 " + doc.get("DOCNO") + " " + i + " " + hits[i].score + " BM25_long");
					}
				}

				// Language Model with Dirichlet Smoothing
				searcher.setSimilarity(new LMDirichletSimilarity()); 		
				analyzer = new StandardAnalyzer();
				parser = new QueryParser("TEXT", analyzer);
				query = parser.parse(queryString);
				topDocs = searcher.search(query, 1000);
				hits = topDocs.scoreDocs;

				System.out.println("----------------Language Model with Dirichlet Smoothing-----------------");
				for (int i = 0; i < hits.length; i++) {
					Document doc = searcher.doc(hits[i].doc);
					if(l == 0 ) {
						writer_LMDS_S.println(51 + j + " Q0 " + doc.get("DOCNO") + " " + i + " " + hits[i].score + " LMDS_short");
					}
					else {
						writer_LMDS_L.println(51 + j + " Q0 " + doc.get("DOCNO") + " " + i + " " + hits[i].score + " LMDS_long");
					}
				}

				// Language Model with Jelinek Mercer Smoothing
				float lambda = 0.7f;
				searcher.setSimilarity(new LMJelinekMercerSimilarity(lambda)); 		
				analyzer = new StandardAnalyzer();
				parser = new QueryParser("TEXT", analyzer);
				query = parser.parse(queryString);
				topDocs = searcher.search(query, 1000);
				hits = topDocs.scoreDocs;

				System.out.println("----------------Language Model with Jelinek Mercer Smoothing-----------------");
				for (int i = 0; i < hits.length; i++) {
					Document doc = searcher.doc(hits[i].doc);
					if(l == 0 ) {
						writer_LMJM_S.println(51 + j + " Q0 " + doc.get("DOCNO") + " " + i + " " + hits[i].score + " LMJM_short");
					}
					else {
						writer_LMJM_L.println(51 + j + " Q0 " + doc.get("DOCNO") + " " + i + " " + hits[i].score + " LMJM_long");
					}
				}
			}	
		}

		writer_VSM_S.close();
		writer_VSM_L.close();
		writer_BM25_S.close();
		writer_BM25_L.close();
		writer_LMDS_S.close();
		writer_LMDS_L.close();
		writer_LMJM_S.close();
		writer_LMJM_L.close();
		reader.close();
	}


	//----------------------------------------------------------------------------------------------
	// Method Name 	: getTagData 
	// Use			: This method returns the document contents present within the tags
	public static String getTagData(String tagText, String startTag, String endTag){

		Integer startIndex = tagText.indexOf(startTag);
		Integer endIndex = tagText.indexOf(endTag);
		Integer tagOffset = (startTag).length();

		if(startIndex == endIndex) {
			return "EMPTY";
		}
		else {		
			startIndex = startIndex + tagOffset;
			return(tagText.substring(startIndex, endIndex).trim());
		}
	}


	//----------------------------------------------------------------------------------------------
	// Method Name 	: trecParser 
	// Use			: This method reads the document as they come from IndexDocs and adds each of them to the index
	//				  according to the analyzers
	static List<String> trecParser(Path file) throws IOException {

		String totalText = new String(Files.readAllBytes(Paths.get(file.toString())), StandardCharsets.UTF_8);

		// Initialize the variables to empty strings
		String titleText, descText, Content = "";
		titleText = descText = "";

		List<String> queryList = new ArrayList<String>();

		// Initializing an Array List with all the tags
		List<String> tagNames = new ArrayList<String>();
		tagNames.addAll(Arrays.asList("<title> Topic:", "<desc> Description:", "<smry> Summary:"));
		String tagName = "";

		int textBegin, textEnd, offset = 0;

		// System.out.println("One Doc: ------------------" + getTagData(totalText, "<top>", "</top>"));

		while(!getTagData(totalText, "<top>", "</top>").equals("EMPTY") && totalText.indexOf("top") != -1) {

			// Initializing to empty string
			//titleText = descText = "";

			Content = getTagData(totalText, "<top>","</top>");
			Content = Content.trim();

			for (int i = 0; i < tagNames.size() - 1; i++) {
				tagName = tagNames.get(i);

				//System.out.println("Tag Name ------------------" + tagName);
				//System.out.println("Tag Content: ------------------" + getTagData(Content, tagNames.get(i), tagNames.get(i + 1)));

				if(!getTagData(Content, tagNames.get(i), tagNames.get(i + 1)).equals("EMPTY") && Content.indexOf(tagName) != -1){
					if (tagName == "<title> Topic:"){
						titleText += getTagData(Content, tagNames.get(i), tagNames.get(i + 1)).trim().replace("/", " or ") + "--";
					}
					else if (tagName == "<desc> Description:"){
						descText += getTagData(Content, tagNames.get(i), tagNames.get(i + 1)).trim().replace("/", " or ") + "--";
					}
				}
			}

			totalText = totalText.substring(0, totalText.indexOf("<top>")) + totalText.substring(totalText.indexOf("</top>") + "</top>".length(),totalText.length());
			totalText = totalText.trim();
		}

		queryList.add(titleText.trim());
		queryList.add(descText.trim().replace("\n", " ").replace("\r", " "));

		return(queryList);
	}
}
