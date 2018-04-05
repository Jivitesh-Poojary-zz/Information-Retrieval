package assignment2;


import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.*;

import java.lang.Math.* ;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

public class easySearch {
	
	public static List tfidfResult(String queryString)throws Exception{
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths
				.get("F:\\IUB DS Masters Subjects\\ILS - Z534 Search\\Homework\\Homework 2\\assignment__export\\index")));
		
		IndexSearcher searcher = new IndexSearcher(reader);

		// F:\\IUB DS Masters Subjects\\ILS - Z534 Search\\Homework\\Homework 2\\assignment__export\\index
		
		Map<String, Float> IDF = new HashMap<String, Float>();
		Map<Integer, Float> docLen = new HashMap<Integer, Float>();
		
		HashMap<String, Double> SCORE = new HashMap<String, Double>();
		HashMap<String, Double> totalScore = new HashMap<String, Double>();

		/**
		 * Get query terms from the query string
		 */
		//String queryString = "frowning clown";

		// Get the pre-processed query terms
		Analyzer analyzer = new StandardAnalyzer();
		QueryParser parser = new QueryParser("TEXT", analyzer);
		Query query = parser.parse(queryString);
		Set<Term> queryTerms = new LinkedHashSet<Term>();
		
		searcher.createNormalizedWeight(query, false).extractTerms(queryTerms);
		//System.out.println("Terms in the query: ");
		for (Term t : queryTerms) {
			//System.out.println(t.text());
			IDF.put(t.text(), 0f);
		}
	
		
		/**
		 * Get normalized document length
		 */
		ClassicSimilarity dSimi = new ClassicSimilarity();

		// Get the segments of the index
		List<LeafReaderContext> leafContexts = reader.getContext().reader()
				.leaves();
		
		int numberOfDoc = 0;
		// Processing each segment
		for (int i = 0; i < leafContexts.size(); i++) {

			// Get document length
			LeafReaderContext leafContext = leafContexts.get(i);
			int startDocNo = leafContext.docBase;
			numberOfDoc = leafContext.reader().maxDoc();

			for (int docId = 0; docId < numberOfDoc; docId++) {

				// Get normalized length (1/sqrt(numOfTokens)) of the document
				float normDocLeng = dSimi.decodeNormValue(leafContext.reader()
						.getNormValues("TEXT").get(docId));

				// Get length of the document
				float docLeng = 1 / (normDocLeng * normDocLeng);
//				System.out.println("Length of doc(" + (docId + startDocNo)
//						+ ", " + searcher.doc(docId + startDocNo).get("DOCNO")
//						+ ") is " + docLeng);
				docLen.put(docId + startDocNo, docLeng);
			}
		}
		
		
		for(String term: IDF.keySet()) {

			/**
			 * Get document frequency
			 */
			int df=reader.docFreq(new Term("TEXT", term));
			//System.out.println("-----------------------" + df);
			//System.out.println(term);
			
			if(df==0) {df = 1;}
			
			float cal = 1 + (numberOfDoc/df);
			if(cal == 0) {
					cal = 0.0f;}
			else {
				cal = (float) Math.log(cal);
			}	
			
			IDF.put(term, cal);
			//System.out.println("^^^^^" + IDF.get(term));

			
			/**
			 * Get term frequency
			 */
			for (int i = 0; i < leafContexts.size(); i++) {

				// Get document length
				LeafReaderContext leafContext = leafContexts.get(i);
				int startDocNo = leafContext.docBase;

				// Get frequency of the term "police" from its postings
				PostingsEnum de = MultiFields.getTermDocsEnum(leafContext.reader(), "TEXT", new BytesRef(term));

				int doc;
				double TFIDF = 0.0f;

				if (de != null) {
					while ((doc = de.nextDoc()) != PostingsEnum.NO_MORE_DOCS) {
						float TF = de.freq()/docLen.get(doc);
						TFIDF = TF*IDF.get(term);
						
//						System.out.println(term + " occurs " + de.freq()
//						+ " time(s) in doc(" + (de.docID() + startDocNo)
//						+ ")" + "and its TF is " + TF + " with TFIDF as : " + TFIDF);
						
						int docid = de.docID() + startDocNo;
						SCORE.put(reader.document(docid).get("DOCNO"), TFIDF);
					}
				}
			}
			//break;
			SCORE.forEach((k, v) -> totalScore.merge(k, v, Double::sum));
		}
		
		return(entriesSortedByValues(totalScore));
	}
	
	
	
	public static <K,V extends Comparable<? super V>> 
	List<Entry<K, V>> entriesSortedByValues(Map<K,V> map) {

		List<Entry<K,V>> sortedEntries = new ArrayList<Entry<K,V>>(map.entrySet());

		Collections.sort(sortedEntries, 
				new Comparator<Entry<K,V>>() {
			@Override
			public int compare(Entry<K,V> e1, Entry<K,V> e2) {
				return e2.getValue().compareTo(e1.getValue());
			}
		}
				);

		return sortedEntries;
	}
}