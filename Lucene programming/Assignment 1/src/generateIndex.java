// References for this code:
// Zheng's Github Repo - https://github.iu.edu/chunguo/IR_demo_6_2/blob/master/src/IndexFiles.java
// Chirag Agarwal Lucene tutorial - https://github.com/chiragagrawal93/Lucene-Tutorials
// Lucene tutorial - https://www.youtube.com/watch?v=FixCCGjLWGg

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.io.InputStream;

import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

public class generateIndex {

	private generateIndex() {}

	public static void main(String[] args) {

		// Update the corpus location here
		String docsPath = "F:\\IUB DS Masters Subjects\\ILS - Z534 Search\\Homework\\Homework 1\\corpus";

		final Path docDir = Paths.get(docsPath);
		if (!Files.isReadable(docDir)) {
			System.out.println("Document directory '" + docDir.toAbsolutePath() + "' does not exist or is not readable, please check the path");
			System.exit(1);
		}

		Date start = new Date();
		try {

			// Index - KeywordAnalyzer - Update this location before using
			String indexPath = "F:\\IUB DS Masters Subjects\\ILS - Z534 Search\\Homework\\Homework 1\\Index - KeywordAnalyzer";
			System.out.println("Indexing to directory '" + indexPath + "'...");
			Directory dir = FSDirectory.open(Paths.get(indexPath));
			Analyzer analyzer_Keyword = new KeywordAnalyzer();
			IndexWriterConfig iwc = new IndexWriterConfig(analyzer_Keyword);
			iwc.setOpenMode(OpenMode.CREATE);
			IndexWriter writer = new IndexWriter(dir, iwc);
			indexDocs(writer, docDir);
			writer.forceMerge(1);
			writer.commit();
			writer.close();

			// Index - SimpleAnalyzer - Update this location before using
			indexPath = "F:\\IUB DS Masters Subjects\\ILS - Z534 Search\\Homework\\Homework 1\\Index - SimpleAnalyzer";
			System.out.println("Indexing to directory '" + indexPath + "'...");
			dir = FSDirectory.open(Paths.get(indexPath));
			Analyzer analyzer_Simple = new SimpleAnalyzer();
			iwc = new IndexWriterConfig(analyzer_Simple);
			iwc.setOpenMode(OpenMode.CREATE);
			writer = new IndexWriter(dir, iwc);
			indexDocs(writer, docDir);
			writer.forceMerge(1);
			writer.commit();
			writer.close();

			// Index - StopAnalyzer - Update this location before using
			indexPath = "F:\\IUB DS Masters Subjects\\ILS - Z534 Search\\Homework\\Homework 1\\Index - StopAnalyzer";
			System.out.println("Indexing to directory '" + indexPath + "'...");
			dir = FSDirectory.open(Paths.get(indexPath));
			Analyzer analyzer_stop = new StopAnalyzer();
			iwc = new IndexWriterConfig(analyzer_stop);
			iwc.setOpenMode(OpenMode.CREATE);
			writer = new IndexWriter(dir, iwc);
			indexDocs(writer, docDir);
			writer.forceMerge(1);
			writer.commit();
			writer.close();

			// Index - StandardAnalyzer - Update this location before using
			indexPath = "F:\\IUB DS Masters Subjects\\ILS - Z534 Search\\Homework\\Homework 1\\Index - StandardAnalyzer";
			System.out.println("Indexing to directory '" + indexPath + "'...");
			dir = FSDirectory.open(Paths.get(indexPath));
			Analyzer analyzer_Standard = new StandardAnalyzer();
			iwc = new IndexWriterConfig(analyzer_Standard);
			iwc.setOpenMode(OpenMode.CREATE);
			writer = new IndexWriter(dir, iwc);
			indexDocs(writer, docDir);
			writer.forceMerge(1);
			writer.commit();
			writer.close();


			Date end = new Date();
			System.out.println(end.getTime() - start.getTime() + " total milliseconds");


		} catch (IOException e) {
			System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
		}
	}

	// Method Name 	: indexDocs 
	// Use			: This method is important in reading the entire directory and making the index
	static void indexDocs(final IndexWriter writer, Path path) throws IOException {
		if (Files.isDirectory(path)) {
			Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					try {
						indexDoc(writer, file);
					} catch (IOException ignore) {
						// don't index files that can't be read.
					}
					return FileVisitResult.CONTINUE;
				}
			});
		} else {
			indexDoc(writer, path);
		}
	}

	// Method Name 	: getTagData 
	// Use			: This method returns the document contents present within the tags
	public static String getTagData(String tagText, String tag){
		
		Integer startIndex = tagText.indexOf("<" + tag + ">");
		Integer endIndex = tagText.indexOf("</" + tag + ">");
		Integer tagOffset = ("<" + tag + ">").length();
		
		if(startIndex == endIndex) {
			return "EMPTY";
		}
		else {		
			startIndex = startIndex + tagOffset;
			return(tagText.substring(startIndex, endIndex).trim());
		}
	}

	// Method Name 	: indexDoc 
	// Use			: This method reads the document as they come from IndexDocs and adds each of them to the index
	//				  according to the analyzers
	static void indexDoc(IndexWriter writer, Path file) throws IOException {
		try (InputStream stream = Files.newInputStream(file)) {

			String totalText = new String(Files.readAllBytes(Paths.get(file.toString())), StandardCharsets.UTF_8);

			// Initialize the variables to empty strings
			String docnoString, headText, bylineText, datelineText, textText, Content = "";

			// Initializing an Array List with all the tags
			List<String> tagNames = new ArrayList<String>();
			tagNames.addAll(Arrays.asList("DOCNO","HEAD","BYLINE","DATELINE","TEXT"));
			String tagName = "";
			
			int textBegin, textEnd, offset = 0;

			while(!getTagData(totalText, "DOC").equals("EMPTY") && totalText.indexOf("DOC") != -1) {
				
				// Initializing to empty string
				docnoString = headText = bylineText = datelineText = textText = "";
				
				Content = getTagData(totalText, "DOC");
				Content = Content.trim();

				for (int i = 0; i < tagNames.size(); i++) {
					tagName = tagNames.get(i);
					
					while(!getTagData(Content, tagName).equals("EMPTY") && Content.indexOf(tagName) != -1){
						if (tagName == "DOCNO"){
							docnoString += getTagData(Content, tagName) + " ";
						}
						else if (tagName == "HEAD"){
							headText += getTagData(Content, tagName) + " ";
						}
						else if (tagName == "BYLINE"){
							bylineText += getTagData(Content, tagName) + " ";
						}
						else if (tagName == "DATELINE"){
							datelineText += getTagData(Content, tagName) + " ";
						}
						else if (tagName == "TEXT"){
							textText += getTagData(Content, tagName) + " ";
						}
						
						textBegin = Content.indexOf("<" + tagName + ">");
						textEnd = Content.indexOf("</"+ tagName +">");
						offset = ("</"+ tagName +">").length();		
						
						Content = Content.substring(0,textBegin) + Content.substring(textEnd + offset,Content.length());
						Content = Content.trim();
					}
				}

				totalText = totalText.substring(0, totalText.indexOf("<DOC>")) + totalText.substring(totalText.indexOf("</DOC>") + "</DOC>".length(),totalText.length());
				totalText = totalText.trim();

				// make a new, empty document for Index
				Document doc = new Document();

				doc.add(new StringField("DOCNO", docnoString, Field.Store.YES));
				doc.add(new TextField("HEAD", headText.trim(), Field.Store.YES ));
				doc.add(new TextField("BYLINE", bylineText, Field.Store.YES));				
				doc.add(new TextField("DATELINE", datelineText.trim(), Field.Store.YES));				
				doc.add(new TextField("TEXT", textText.trim(), Field.Store.YES));

				if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
					System.out.println("adding " + file);
					writer.addDocument(doc);
				} else {
					System.out.println("updating " + file);
					writer.updateDocument(new Term("path", file.toString()), doc);
				}
			}
		}
	}
}