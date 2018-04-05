// References for this code:
// Zheng's Github Repo - https://github.iu.edu/chunguo/IR_demo_6_2/blob/master/src/Stats.java


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

import java.io.IOException;
import java.io.PrintWriter;

import java.nio.file.Paths;

import org.apache.lucene.util.BytesRef;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.index.MultiFields;

import org.apache.lucene.store.FSDirectory;

/** Simple command-line based search demo. */
public class indexComparison {

	private indexComparison() {}

	public static void main(String[] args) throws Exception {
		String usage =
				"Usage:\tjava org.apache.lucene.demo.SearchFiles [-index dir] [-field f] [-repeat n] [-queries file] [-query string] [-raw] [-paging hitsPerPage]\n\nSee http://lucene.apache.org/core/4_1_0/demo/ for details.";
		if (args.length > 0 && ("-h".equals(args[0]) || "-help".equals(args[0]))) {
			System.out.println(usage);
			System.exit(0);
		}

		try {
			giveResults("F:\\IUB DS Masters Subjects\\ILS - Z534 Search\\Homework\\Homework 1\\Index - KeywordAnalyzer");
			giveResults("F:\\IUB DS Masters Subjects\\ILS - Z534 Search\\Homework\\Homework 1\\Index - SimpleAnalyzer");
			giveResults("F:\\IUB DS Masters Subjects\\ILS - Z534 Search\\Homework\\Homework 1\\Index - StopAnalyzer");
			giveResults("F:\\IUB DS Masters Subjects\\ILS - Z534 Search\\Homework\\Homework 1\\Index - StandardAnalyzer");
		}
		catch (IOException e) {
			System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
		}
	}

	// Method Name 	: giveResults 
	// Use			: This method prints the required results and also the vocabulary to a text file
	public static void giveResults(String ind) throws Exception {


		String index = ind;
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));

		String[] name = index.split("-");
		PrintWriter writer = new PrintWriter(name[2].trim() + ".txt", "UTF-8");

		//Print the total number of documents in the corpus
		System.out.println("Total number of documents in the corpus: "+reader.maxDoc());                            

		//Print the number of documents containing the term "new" in <field>TEXT</field>.
		System.out.println("Number of documents containing the term \"new\" for field \"TEXT\": "+reader.docFreq(new Term("TEXT", "new")));

		//Print the total number of occurrences of the term "new" across all documents for <field>TEXT</field>.
		System.out.println("Number of occurrences of \"new\" in the field \"TEXT\": "+reader.totalTermFreq(new Term("TEXT","new")));                                                       

		Terms vocabulary = MultiFields.getTerms(reader, "TEXT");

		//Print the size of the vocabulary for <field>TEXT</field>, applicable when the index has only one segment.
		System.out.println("Size of the vocabulary for this field: "+vocabulary.size());

		//Print the total number of documents that have at least one term for <field>TEXT</field>
		System.out.println("Number of documents that have at least one term for this field: "+vocabulary.getDocCount());

		//Print the total number of tokens for <field>TEXT</field>
		System.out.println("Number of tokens for this field: "+vocabulary.getSumTotalTermFreq());

		//Print the total number of postings for <field>TEXT</field>
		System.out.println("Number of postings for this field: "+vocabulary.getSumDocFreq());      

		//Print the vocabulary for <field>TEXT</field>
		TermsEnum iterator = vocabulary.iterator();

		BytesRef byteRef = null;

		writer.println("\n*******Vocabulary-Start**********");

		Integer count = 0;

		while((byteRef = iterator.next()) != null) {
			String term = byteRef.utf8ToString();
			writer.println(term);
			//System.out.print(term+"\t");
			count+=1;

			// To get the partial list for KeyAnalyzer
			if(count == 1000) {
				//break;
			}
		}

		writer.println("\n*******Vocabulary-End**********");

		System.out.println("\n");  

		reader.close();
		writer.close();
	}
}