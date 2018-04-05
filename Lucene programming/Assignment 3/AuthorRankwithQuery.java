package pageRank;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.collections15.Transformer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.FSDirectory;

import edu.uci.ics.jung.algorithms.scoring.PageRankWithPriors;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;

//-----------------------------------------------------------------------------------
// Class : AuthorRankwithQuery 
//-----------------------------------------------------------------------------------
public class AuthorRankwithQuery {

	//-----------------------------------------------------------------------------------
	// Method : main
	//-----------------------------------------------------------------------------------
	public static void main(String[] args)throws IOException, ParseException {

		String graphPath = "F:\\IUB DS Masters Subjects\\ILS - Z534 Search\\Homework\\Homework 3\\Data\\author.net";
		String indexPath = "F:\\IUB DS Masters Subjects\\ILS - Z534 Search\\Homework\\Homework 3\\Data\\author_index";
		int max = 10;
		int maxResults = 300;
		float alpha = 0.1f;
		float dampingFactor = 0.85f;

		AuthorRankwithQuery aObj = new AuthorRankwithQuery();
		aObj.getResult(graphPath, indexPath, "Data Mining", max, maxResults, alpha, dampingFactor);
		aObj.getResult(graphPath, indexPath, "Information Retrieval", max, maxResults, alpha, dampingFactor);

	}

	//-----------------------------------------------------------------------------------
	// Method : normalizePrior - This method will calculate the normalized priors
	//-----------------------------------------------------------------------------------
	public Map<Integer, Double> normalizePrior(Map<Integer, Double> authorsPriors){

		HashMap<Integer, Double> normalizedAuthorsPriors = new HashMap<Integer, Double>();
		Double priorSum = 0.0;

		for (Double value : authorsPriors.values()) {
			priorSum += value;
		}
		for (Integer author : authorsPriors.keySet()) {
			normalizedAuthorsPriors.put(author, authorsPriors.get(author) / priorSum);
		}

		return normalizedAuthorsPriors;
	}

	//-----------------------------------------------------------------------------------
	// Method : calculatePrior - This method will calculate the non-normalized priors
	//-----------------------------------------------------------------------------------
	public Map<Integer, Double> calculatePrior(String indexPath, String queryString, int maxResults) throws IOException, ParseException {

		Map<Integer, Double> authorPrior = new HashMap<Integer, Double>();
		
		IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(indexPath)));
		IndexSearcher searcher = new IndexSearcher(reader);
		Analyzer analyzer = new StandardAnalyzer();
		searcher.setSimilarity(new BM25Similarity());

		QueryParser parser = new QueryParser("content", analyzer);
		Query query = parser.parse(queryString);

		TopDocs topResults = searcher.search(query, maxResults);
		ScoreDoc[] score = topResults.scoreDocs;

		for (ScoreDoc s : score) {
			Document doc = searcher.doc(s.doc);
			int authorId = Integer.valueOf(doc.get("authorid"));

			if (authorPrior.containsKey(authorId)) {
				double newScore = (double)authorPrior.get(authorId) + (double)s.score;
				authorPrior.put(authorId, newScore);	
			} 
			else {
				authorPrior.put(authorId, (double)s.score);
			}
		}

		Map<Integer, Double> normalizedAuthorsPriors = normalizePrior(authorPrior);

		reader.close();
		return(normalizedAuthorsPriors);
	}


	//-----------------------------------------------------------------------------------
	// Method : makeGraph - This method will create the graph from the .net file using the list of top authors only
	//-----------------------------------------------------------------------------------
	public DirectedSparseGraph<Integer, Integer> makeGraph(Map<Integer, Double> topAuthorPrior, String filePath, String indexPath, String queryString, int maxResults)throws IOException, ParseException {

		Map<Integer, Integer> mapVertices = new HashMap<Integer, Integer>();
		DirectedSparseGraph<Integer, Integer> graph = new DirectedSparseGraph<Integer, Integer>();

		File file = new File(filePath);								

		BufferedReader br = new BufferedReader(new FileReader(file));	

		int vertices = Integer.parseInt(br.readLine().split("\\s+")[1].trim());
		for(int i=1; i<=vertices ;i++) {
			String [] line = br.readLine().split(" ");	
			int vertex = Integer.parseInt(line[0].trim());
			int vertexVal = Integer.parseInt(line[1].trim().substring(1, line[1].trim().length()-1));
			if(topAuthorPrior.containsKey(vertexVal)) {
				mapVertices.put(vertex,vertexVal);
				graph.addVertex(vertexVal);
			}
		}

		int edges = Integer.parseInt(br.readLine().split("\\s+")[1].trim());
		for(int i=1; i<=edges ;i++) {								
			String [] line = br.readLine().split(" ");
			int from = Integer.parseInt(line[0].trim());
			int to = Integer.parseInt(line[1].trim());
			if(topAuthorPrior.containsKey(mapVertices.get(from)) && topAuthorPrior.containsKey(mapVertices.get(to))) {
				graph.addEdge(i,mapVertices.get(from),mapVertices.get(to), EdgeType.DIRECTED);	
			}
		}
		br.close();
		return(graph);
	}

	//-----------------------------------------------------------------------------------
	// Method : authorNameFromVertex - This method is the obtain the Author Names from the vertex
	//-----------------------------------------------------------------------------------
	public Map<Integer,String> authorNameFromVertex(String indexPath) throws IOException {

		IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(indexPath)));

		Map<Integer,String> authorNameMap = new HashMap<Integer,String>();

		for(int i=0;i<reader.maxDoc();i++){
			Document d = reader.document(i);
			authorNameMap.put(Integer.valueOf(d.get("authorid")), d.get("authorName"));
		}
		return(authorNameMap);
	}

	//-----------------------------------------------------------------------------------
	// Method : getResult - This method is the main method for our program
	//-----------------------------------------------------------------------------------
	public void getResult(String graphPath, String indexPath, String queryString, int max, int maxResults, float alpha, float dampingFactor) throws IOException, ParseException {

		Map<Integer,String> authorNameMap = authorNameFromVertex(indexPath); 

		Map<Integer, Double> normAuthorPrior = calculatePrior(indexPath, queryString, maxResults);
		Transformer<Integer, Double> prior = new Transformer<Integer, Double>() {
			@Override
			public Double transform(Integer x) {
				return (Double) normAuthorPrior.get(x);
			}
		};

		DirectedSparseGraph<Integer, Integer> G = makeGraph(normAuthorPrior, graphPath, indexPath, queryString, maxResults);
		PageRankWithPriors<Integer, Integer> prp = new PageRankWithPriors<Integer, Integer>(G, prior, alpha);
		prp.setTolerance(dampingFactor);
		prp.evaluate();

		Map<Integer, Double> authorRank = new HashMap<Integer, Double>();
		for (Integer v : G.getVertices()) {
			authorRank.put(v, prp.getVertexScore(v));
		}

		RankComparator ic = new RankComparator(authorRank);
		Map<Integer, Double> authorRankSort = new TreeMap<Integer, Double>(ic);
		authorRankSort.putAll(authorRank);

		System.out.printf("\nQuery - %s\n", queryString);
		System.out.printf("%-40s %-15s %s\n","Author","Vertex","Score");

		int breakCount = 0;
		for (Entry<Integer, Double> v : authorRankSort.entrySet()) {
			System.out.printf("%-40s %-15s %s\n", authorNameMap.get(v.getKey()).trim(), v.getKey(), v.getValue());
			breakCount++;
			if (breakCount >= max) {
				break;
			}
		}
	}
}