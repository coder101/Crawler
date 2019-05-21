import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * Created By Pavithra & Kiana
 * The crawler starts from root node and collects link whose page content has keywords(input).
 * The graph is saved in a file int he format as srcURL[space]destURL with first line
 * denoting the number of nodes in the graph
 * Created by pavi on 3/25/2016.
 */

public class WikiCrawler {

    //initialise the queue and visited list
    Queue<GraphNode> queue = new LinkedList<GraphNode>();
    Set<String> visited = new HashSet<String>();
    Set<String> visitedIrrelevant = new HashSet<String>();
    List<GraphNode> crawled = new LinkedList<GraphNode>();
    HashMap<String, Integer> CrawlerForbiddenURL = new HashMap<String, Integer>();
    HashMap<String, GraphNode> uniqueEdges = new HashMap<String, GraphNode>();
    /****************************************************
     * seedUrlSubString is the root page where we begin crawling.
     * keywords are used to search for the pages relevant to search query or interest.
     * maxGraphNodes is the max num of relevant web pages to be collected.
     * fileName is the destination to which the webgraph will be saved
     */

    public String seedUrlSubString, filename;
    public String[] keywords;
    public static String BASE_URL = "https://en.wikipedia.org";//revert  "http://www.cs.iastate.edu/"
    public Integer maxGraphNodes;
    public static int downloadCount = 0;

    WikiCrawler(String subDomain, String[] queryWords, Integer nodeLimits,
                String outputFileName) throws Exception {

        this.seedUrlSubString = subDomain;
        this.keywords = queryWords;
        for (int i = 0; i < queryWords.length; i++) {
            this.keywords[i] = queryWords[i].toLowerCase();
        }
        this.maxGraphNodes = nodeLimits;
        this.filename = outputFileName;
    }

    /***************************************************************
     *
     */
    void crawl() {

        try {

            this.checkCrawlerAccess();
            this.crawlWebPages();
            this.printLog();

        } catch (Exception e) {

            //e.printStackTrace();
            System.out.println(" Exception " + e.getLocalizedMessage());
        }
    }

    void printLog() throws FileNotFoundException, UnsupportedEncodingException {
        PrintWriter writer = new PrintWriter(filename, "UTF-8");

        //total no of pages crawled
        writer.println((visited.size() - 1));

        for (GraphNode link : crawled) {
            writer.println(link.parent + " " + link.child);
        }
        writer.close();
    }

    void crawlWebPages() throws MalformedURLException {
        this.initializeQueue();
        this.downloadMaxGraphNodes();
        this.processGraph();
    }

    void downloadMaxGraphNodes() {
        while (!queue.isEmpty()) {
            GraphNode node = queue.remove();
            processLinks(node);

            if (visited.size() > maxGraphNodes) { //seed + max nodes so >
                break;
            }

        }
    }

    void processGraph() {
        while (!queue.isEmpty()) {
            GraphNode node = queue.remove();
            extractEdgesFromLinks(node);
        }
    }

    void initializeQueue() throws MalformedURLException {
        URL url = new URL(BASE_URL + seedUrlSubString);
        String parent = seedUrlSubString, graph = "";
        int isRoot = 1;
        visited.add(parent);

        GraphNode.keyWords = keywords.clone();
        GraphNode seed = new GraphNode("", parent);
        if (!CrawlerForbiddenURL.containsKey(parent)) {
            seed.DowloadPageAndLinks(BASE_URL);
            queue.add(seed);
        }
    }

    boolean checkCrawlerAccess() throws Exception {
        try {
            GraphNode node = new GraphNode(BASE_URL, "/robots.txt");
            node.DownloadPage(BASE_URL);
            int index = 0, i = 1;
            String[] lines = node.content.split("\n");
            for (String line : lines) {
                if (line.startsWith("disallow")) {

                    String forbiddenURL = "";
                    int start = line.indexOf(":") + 1;
                    int end = line.length();
                    forbiddenURL = line.substring(start, end).trim();
                    CrawlerForbiddenURL.put(forbiddenURL, i);
                    i++;
                }
            }
            if (CrawlerForbiddenURL.containsKey("*")) {
                throw new Exception("Access Denied in the Site");
            }
            return true;
        } catch (Exception e) {
            throw e;
        }
    }


    void processLinks(GraphNode node1) {
        String parent = node1.child;
        for (String neighbourLinkAddress : node1.links) {

            if (visited.size() > maxGraphNodes) {
                break;
            }

            neighbourLinkAddress = neighbourLinkAddress.replace("\"", "");

            if (validAddress(neighbourLinkAddress, parent)) {
                GraphNode node = new GraphNode(parent, neighbourLinkAddress);

                if (newLink(neighbourLinkAddress)) {
                    processNode(node, neighbourLinkAddress);
                } else {
                    processVisited(neighbourLinkAddress, node);
                }
            }
        }
    }

    void processVisited(String address, GraphNode node) {
       if (!visitedIrrelevant.contains(address)
                && !uniqueEdges.containsKey(address)) {
           crawled.add(node);
           uniqueEdges.put(address, node);
       }
    }

    boolean newLink(String address) {
        return !visited.contains(address) && !visitedIrrelevant.contains(address);
    }

    boolean validAddress(String address, String parent) {
        return address != null && (!CrawlerForbiddenURL.containsKey(address)) &&
                (!address.contains(":") && (!address.contains("#"))) &&
                (!parent.equalsIgnoreCase(address));
    }

    void processNode(GraphNode node, String address) {
        if (node.DowloadPageAndLinks(BASE_URL)) {
            queue.add(node);
            if (!this.uniqueEdges.containsKey(address)) {
                this.uniqueEdges.put(address, node);
                crawled.add(node);
            }
            visited.add(address);
        } else {
            visitedIrrelevant.add(address);
        }
    }


    void extractEdgesFromLinks(GraphNode node) {

        final HashMap<String, GraphNode> uniqueEdges = new HashMap<String, GraphNode>();
        for (String address : node.links) {
            address = address.replace("\"", "");
            if (address != null && address.compareToIgnoreCase(node.child) != 0) {// revert
                if ((!address.contains(":")) && (!address.contains("#"))) {
                    GraphNode node1 = new GraphNode(node.child, address);

                    if (visited.contains(address)) {
                        uniqueEdges.put(address, node1);
                    }

                }
            }
        }
        crawled.addAll(uniqueEdges.values());
    }

    public static void main(String[] arg) throws Exception {

        String[] keywords = {"tennis", "grand slam"};
        String urlSubString = "/wiki/tennis";
        String outputFilename = "D:\\tennisGraph.txt";
        Integer maxGraphNodes = 100;

        WikiCrawler cr = new WikiCrawler(urlSubString, keywords, maxGraphNodes, outputFilename);

        long startTime = System.currentTimeMillis();
        cr.crawl();
        long endTime = System.currentTimeMillis();
        long timeTaken = endTime - startTime;
//        System.out.println("Time taken " + timeTaken / 1000 + " download time" + graphNode.totalTime / 1000);

        System.out.println("Data Written to file : " + outputFilename);

    }
}




