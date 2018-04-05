package pageRank;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

import edu.uci.ics.jung.algorithms.scoring.PageRank;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;

//-----------------------------------------------------------------------------------
//Class : AuthorRank 
//-----------------------------------------------------------------------------------
public class AuthorRank {

	//-----------------------------------------------------------------------------------
	// Method : main
	//-----------------------------------------------------------------------------------
	public static void main(String args[])throws IOException {

		String graphPath = "F:\\IUB DS Masters Subjects\\ILS - Z534 Search\\Homework\\Homework 3\\Data\\author.net";
		DirectedSparseGraph<Integer, Integer> graph = makeGraph(graphPath);
		
		String indexPath = "F:\\IUB DS Masters Subjects\\ILS - Z534 Search\\Homework\\Homework 3\\Data\\author_index";
		HashMap<Integer,String> authorNameMap = new HashMap<Integer,String>();
		IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(indexPath)));
		for(int i=0;i<reader.maxDoc();i++){
			Document d = reader.document(i);
			authorNameMap.put(Integer.valueOf(d.get("authorid")), d.get("authorName"));
		}
		

		int max = 10;
		float alpha = 0.15f;
		float dampingFactor = 0.85f;


		PageRank<Integer, Integer> pr = new PageRank<Integer, Integer>(graph, alpha);
		pr.setTolerance(dampingFactor);
		pr.evaluate();
		Map<Integer, Double> finalPageRank = new HashMap<Integer, Double>();

		for (Integer vertex : graph.getVertices()) {
			finalPageRank.put(vertex, pr.getVertexScore(vertex));
		}

		RankComparator ic = new RankComparator(finalPageRank);
		TreeMap<Integer, Double> pageRankSort = new TreeMap<Integer, Double>(ic);

		pageRankSort.putAll(finalPageRank);
		int breakCount = 0;

		System.out.printf("%-40s %-15s %s\n", "Author", "VertexId", "Pagerank");
		for (Entry<Integer, Double> v : pageRankSort.entrySet()) {
			System.out.printf("%-40s %-15s %s\n", authorNameMap.get(v.getKey()).trim(), v.getKey(), v.getValue());
			breakCount++;
			if (breakCount >= max) {
				break;
			}
		}
	}

	//-----------------------------------------------------------------------------------
	// Method : makeGraph - This method will create the directed graph from the .net method
	//-----------------------------------------------------------------------------------
	public static DirectedSparseGraph<Integer, Integer> makeGraph(String graphPath)throws IOException {

		DirectedSparseGraph<Integer, Integer> graph = new DirectedSparseGraph<Integer, Integer>();
		HashMap<Integer, Integer> mapVertices = new HashMap<Integer, Integer>();
		
		File file = new File(graphPath);

		BufferedReader br = new BufferedReader(new FileReader(file));	

		int vertices = Integer.parseInt(br.readLine().split("\\s+")[1].trim());
		for(int i=1; i<=vertices; i++) {
			String [] line = br.readLine().split(" ");	
			int vertex = Integer.parseInt(line[0].trim());
			int vertexVal = Integer.parseInt(line[1].trim().substring(1, line[1].trim().length()-1));
			mapVertices.put(vertex,vertexVal);
			graph.addVertex(vertexVal);
		}

		int edges = Integer.parseInt(br.readLine().split("\\s+")[1].trim());
		for(int i=1; i<=edges; i++) {								
			String [] line = br.readLine().split(" ");
			int from = Integer.parseInt(line[0].trim());
			int to = Integer.parseInt(line[1].trim());
			graph.addEdge(i,mapVertices.get(from), mapVertices.get(to), EdgeType.DIRECTED);
		}

		br.close();
		return(graph);
	}
}

class RankComparator implements Comparator<Integer> {
	Map<Integer, Double> map;
	public RankComparator(Map<Integer, Double> base) {this.map = base;}
	public int compare(Integer a, Integer b) {
		if (map.get(a) >= map.get(b)) {
			return -1;
		} 
		else {
			return 1;
		}
	}
}
