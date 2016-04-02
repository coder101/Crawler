import sun.misc.IOUtils;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.parser.ParserDelegator;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static javax.swing.text.html.HTMLEditorKit.*;

/**
 * Created By Pavithra & Kiana
 * The crawler starts from root node and collects link whose page content has keywords(input).
 * The graph is saved in a file int he format as srcURL[space]destURL with first line
 * denoting the number of nodes in the graph
 * Created by pavi on 3/25/2016.
 */

public class WikiCrawler {


    //initialise the queue and visited list
    Queue<graphNode> queue = new LinkedList<graphNode>();
    Set<String> visited = new HashSet<String>();
    Set<String> visitedIrrelevant = new HashSet<String>();
    List<graphNode> crawled= new LinkedList<graphNode>();
    HashMap <String,Integer> CrawlerForbiddenURL = new HashMap <String,Integer>();
    public static void  main(String[] arg){

        String []keywords ={"tennis","grand slam"};

        WikiCrawler cr = new WikiCrawler("/wiki/tennis", keywords, 100, "D:\\trialiastate.txt");

        long startTime = System.currentTimeMillis();
        cr.crawl();
        long endTime = System.currentTimeMillis();
        long timeTaken = endTime - startTime;
        //System.out.println("time taken "+timeTaken/1000+" download time"+graphNode.totalTime/1000);

        System.out.println("Data Written to file");

    }
    /****************************************************
     * seedURL is the root page where we begin crawling
     * keywords are used to search for the pages relevant to search query or interest
     * maxGraphNodes is the max num of relevant web pages to be collected
     * fileName is the destination to which the webgraph will be saved
     */
    public   String m_seed,m_fileName;
    public  String[] m_keyWords;
    public static String BASE_URL = "https://en.wikipedia.org";//revert  "http://www.cs.iastate.edu/"
    public  Integer m_maxNodes;
    public static int countDownload =0;
    WikiCrawler(String seedURL,String [] keyWords, Integer maxGraphNodes, String fileName){
        m_seed = seedURL;
        m_keyWords = keyWords;
        for(int i=0;i< keyWords.length;i++){
            m_keyWords[i] = m_keyWords[i].toLowerCase();
        }
        m_maxNodes = maxGraphNodes;
        m_fileName = fileName;
    }

    /***************************************************************
     *
     */
    void crawl() {

        try {

            //initialse page to seedURL

            InitialiseRobotExclusion();
            if(CrawlerForbiddenURL.containsKey("*"))
            {
                System.out.println("The site forbids download of all pages through crawlers...hence exiting");
            }
            URL url = new URL(BASE_URL + m_seed);
            String parent = m_seed,graph="";
            int isRoot =1;
            visited.add(parent);

            graphNode.keyWords = m_keyWords.clone();
            graphNode seed = new graphNode("",parent);
            if (!CrawlerForbiddenURL.containsKey(parent)) {
                seed.DowloadPageAndLinks(BASE_URL);
                queue.add(seed);
            }
            //Get max nodes
            while(!queue.isEmpty()) {
                graphNode node = queue.remove();
                ProcessLinks(node);

                if (visited.size() > m_maxNodes) { //seed + max nodes so >
                    break;
                }

            }
            while(!queue.isEmpty()) {
                graphNode node = queue.remove();

                extractEdgesFromLinks(node);
            }

            PrintWriter writer = new PrintWriter(m_fileName, "UTF-8");

            writer.println((visited.size()-1));

            for (graphNode link : crawled) {
                writer.println(link.parent + " " + link.child);
            }
            writer.close();

        } catch (Exception e) {

            //e.printStackTrace();
            System.out.println(" Exception "+e.getLocalizedMessage());
        }
    }
    void InitialiseRobotExclusion(){
    try{
        graphNode node = new graphNode(BASE_URL,"/robots.txt");
        node.DownloadPage(BASE_URL);
        int index = 0,i =1;
        String[] lines = node.content.split("\n");
        for(String line :lines){
                if (line.startsWith("disallow")) {

                    String forbiddenURL="";
                    int start = line.indexOf(":") + 1;
                    int end   = line.length();
                    forbiddenURL = line.substring(start, end).trim();
                    CrawlerForbiddenURL.put(forbiddenURL,i);
                    i++;
                }
            }
        } catch (Exception e) {

            e.printStackTrace();
        }
    }


    void ProcessLinks(graphNode node1){
        String parent = node1.child;
        final List<graphNode> links = new LinkedList<graphNode>();
        final List<graphNode> edges = new LinkedList<graphNode>();
        final HashMap<String,graphNode> uniqueEdges = new HashMap<String,graphNode>();
        for(String address : node1.links) {

            if (visited.size() > m_maxNodes) {
                break;
            }

            address = address.replace("\"", "");
            if (address != null && (!CrawlerForbiddenURL.containsKey(address))) {
               // System.out.println(address);  //revert
                if (address.startsWith("/wiki/") && !address.contains(":") && (!address.contains("#"))) {
                    //System.out.println(parent + " " + address);
                    graphNode node = new graphNode(parent, address);

                    if (!parent.equalsIgnoreCase(address)) {
                        if (!visited.contains(address) && !visitedIrrelevant.contains(address)) {


                            System.out.println("Downloading and processing " + countDownload++ + " " + address + "  No of Visited nodes -" + visited.size());

                            if (node.DowloadPageAndLinks(BASE_URL)) {
                                    links.add(node);
                                    if (!uniqueEdges.containsKey(address)) {
                                        uniqueEdges.put(address, node);
                                        edges.add(node);
                                    }
                                    visited.add(address);
                            }
                            else {
                                visitedIrrelevant.add(address);
                            }
                        } else {
                            if (!uniqueEdges.containsKey(address)) {
                                edges.add(node);
                                uniqueEdges.put(address, node);
                            }
                        }
                    }
                }
            }
        }
        queue.addAll(links);
        crawled.addAll(edges);
    }



    void extractEdgesFromLinks(graphNode node) {

        final HashMap<String,graphNode> uniqueEdges = new HashMap<String,graphNode>();
        for(String address: node.links){
            address = address.replace("\"","");
            if (address.startsWith("/wiki/") &&  address != null && address.compareToIgnoreCase(node.child)!=0) {// revert
                if ( (!address.contains(":")) && (!address.contains("#"))) {
                    graphNode node1 = new graphNode(node.child, address);

                    if (visited.contains(address)) {
                        uniqueEdges.put(address, node1);
                    }

                }
            }
        }
        crawled.addAll(uniqueEdges.values());
    }
}




class graphNode {

    private static final int BUFFER_SIZE = 1024;
    public static String[] keyWords;
    static int count=1;
    String parent, child, content;

    List<String > links = new LinkedList<String >();
    static double totalTime=0;

    graphNode(String src, String link) {
        parent = src;
        child = link;
    }

    Boolean DowloadPageAndLinks(String baseURL) {

        try{
        Matcher mLink;
        Pattern pLink = null;

        String HTML_HREF_TAG_PATTERN = "\\s*(?i)href\\s*=\\s*(\"([^\"]*\")|'[^']*'|([^'\">\\s]+))";
        pLink = Pattern.compile(HTML_HREF_TAG_PATTERN);



        if(count%100 == 0)
        {
            Thread.sleep(5000);
        }
        count++;

        long startTime = System.currentTimeMillis();

        URL urlStream = new URL(baseURL+this.child);

        Set<String> keys = new HashSet<String>();
        for(String str: keyWords){
            keys.add(str);
        }
        // System.out.println("Downloading "+count);
        InputStream is = urlStream.openStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line ="";
        content ="";
        Boolean foundp=false;
        while( (line = br.readLine())!= null)
         {

             line = line.toLowerCase();

             if(line.contains("<p>")){ //revert
                 foundp = true;
             }


             for(String str: keyWords){
                 if(line.contains(str))
                 keys.remove(str);
             }
             if(foundp )
             {
                mLink = pLink.matcher(line);

                while(mLink.find())
                {
                    String address = mLink.group(1);
                    links.add(address);
                }
            }
         }
        long endTime = System.currentTimeMillis();
        long timeTaken = endTime - startTime;
        totalTime += timeTaken;
        is.close();

        if(!keys.isEmpty()){
            return false;
        }
        } catch (Exception e) {

            //e.printStackTrace();
            System.out.println("Exception during file download "+e.getLocalizedMessage());
            return false;
        }

        return true;
    }

    Boolean DownloadPage(String baseURL){
        try{


            if(count%100 == 0)
            {
                Thread.sleep(5000);
            }
            count++;

            long startTime = System.currentTimeMillis();

            URL urlStream = new URL(baseURL+this.child);

            // System.out.println("Downloading "+count);
            InputStream is = urlStream.openStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line ="";
            content=getString(is);
            content = content.toLowerCase();
            long endTime = System.currentTimeMillis();
            long timeTaken = endTime - startTime;
            totalTime += timeTaken;


            is.close();

        } catch (Exception e) {

            System.out.println("Exception during file download "+e.getLocalizedMessage());
            return false;
        }


        return true;

    }
    public static String getString(InputStream inputStream) throws IOException
    {
        if (inputStream == null)
            return null;
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream), BUFFER_SIZE);
        int charsRead;
        char[] copyBuffer = new char[BUFFER_SIZE];
        StringBuffer sb = new StringBuffer();
        while ((charsRead = in.read(copyBuffer, 0, BUFFER_SIZE)) != -1)
            sb.append(copyBuffer, 0, charsRead);
        in.close();
        return sb.toString();
    }
}
