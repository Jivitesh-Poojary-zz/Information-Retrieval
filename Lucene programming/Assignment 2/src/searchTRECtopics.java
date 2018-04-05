
package assignment2;
import assignment2.easySearch;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Iterator;
import java.util.HashMap;

public class searchTRECtopics {

	private searchTRECtopics() {}

	public static void main(String[] args) throws Exception {

		// Update the corpus location here
		String docsPath = "F:\\IUB DS Masters Subjects\\ILS - Z534 Search\\Homework\\Homework 2\\assignment__export\\Data\\topics.51-100";

		final Path docDir = Paths.get(docsPath);
		if (!Files.isReadable(docDir)) {
			System.out.println("Document directory '" + docDir.toAbsolutePath() + "' does not exist or is not readable, please check the path");
			System.exit(1);
		}

		Date start = new Date();


		PrintWriter writer_TFIDF_S = new PrintWriter("F:\\IUB DS Masters Subjects\\ILS - Z534 Search\\Homework\\Homework 2\\Results\\TestSA\\TFIDFshortQuery.txt", "UTF-8");
		PrintWriter writer_TFIDF_L = new PrintWriter("F:\\IUB DS Masters Subjects\\ILS - Z534 Search\\Homework\\Homework 2\\Results\\TestSA\\TFIDFlongQuery.txt", "UTF-8");

		System.out.println("START ------------------");

		List<String> queryList = trecParser(docDir); 

		for (int l = 0; l < queryList.size(); l++) {
			String[] queryStrings = queryList.get(l).split("--");

			// System.out.println("!!!!!!!!!!!!!!!!!!" + queryStrings.length);

			for (int j = 0; j < queryStrings.length; j++) {

				String queryString = queryStrings[j];

				// System.out.println(queryString + "------------");

				int count = 1;
				easySearch.tfidfResult(queryString).iterator();	
				Iterator itr = easySearch.tfidfResult(queryString).iterator();

				while(itr.hasNext() && count <= 1000) {
					String [] Res = itr.next().toString().split("=");
					if(l == 0 ) {
						writer_TFIDF_S.println(51 + j + " Q0 "+ Res[0] + " " + count + " " + Double.parseDouble(Res[1]) + " TF_IDF_Short");
					}
					else {
						writer_TFIDF_L.println(51 + j + " Q0 "+ Res[0] + " " + count + " " + Double.parseDouble(Res[1]) + " TF_IDF_Short");
					}
					count++;
				}
			}
		}

		System.out.println("END ------------------");

		Date end = new Date();
		System.out.println(end.getTime() - start.getTime() + " total milliseconds");

		writer_TFIDF_S.close();
		writer_TFIDF_L.close();


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

		//System.out.println("One Doc: ------------------" + getTagData(totalText, "<top>", "</top>"));

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
		queryList.add(descText.trim().replace("\n", " "));

		return(queryList);
	}
}
