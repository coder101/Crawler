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


        WikiCrawler cr = new WikiCrawler("/wiki/tennis", keywords, 100, "D:\\tennis.txt");

        long startTime = System.currentTimeMillis();
        cr.crawl();
        long endTime = System.currentTimeMillis();
        long timeTaken = endTime - startTime;
        System.out.println("time taken "+timeTaken/1000);

    }
    /****************************************************
     * seedURL is the root page where we begin crawling
     * keywords are used to search for the pages relevant to search query or interest
     * maxGraphNodes is the max num of relevant web pages to be collected
     * fileName is the destination to which the webgraph will be saved
     */
    public   String m_seed,m_fileName;
    public  String[] m_keyWords;
    public static String BASE_URL = "https://en.wikipedia.org";
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
            URL url = new URL(BASE_URL + m_seed);
            String parent = m_seed,graph="";
            int isRoot =1;
            visited.add(parent);

            graphNode seed = new graphNode("",parent);

            if (!CrawlerForbiddenURL.containsKey(parent)) {
                seed.DownloadPage(BASE_URL);
                queue.add(seed);
            }
            int requestCount;

            //Get max nodes

            while(!queue.isEmpty()) {
                graphNode node = queue.remove();
                ProcessNode(node);

                if (visited.size() > m_maxNodes) {
                    break;
                }

            }
            while(!queue.isEmpty()) {
                graphNode node = queue.remove();

                int index = node.content.indexOf("<p>");
                Reader strReader= new StringReader(node.content);
                strReader.skip(index);
                extractEdges(strReader,node.child);
            }

            PrintWriter writer = new PrintWriter(m_fileName, "UTF-8");
            writer.println(m_maxNodes);
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
    void ProcessNode(graphNode node){

        try
        {
            int index = node.content.indexOf("<p>");
            String contentToCrawl = node.content.substring(index,node.content.length());
            //extractNodesAndEdges(strReader,node.child);
            regExExtractLink(contentToCrawl,node.child);
        } catch (Exception e) {

            e.printStackTrace();
        }

    }

    Boolean IsRelevant(graphNode node){


        for(String key:m_keyWords){
            if(!node.content.contains(key)){
                return Boolean.FALSE;
            }
        }
        return true;
    }

    void regExExtractLink(String content, String parent)throws  Exception{

        final List<graphNode> links = new LinkedList<graphNode>();
        final List<graphNode> edges = new LinkedList<graphNode>();
        final HashMap<String,graphNode> uniqueEdges = new HashMap<String,graphNode>();

        try{

            Matcher mTag, mLink;
            Pattern pTag=null, pLink=null;

            String HTML_TAG_PATTERN = "(?i)<a([^>]+)>(.+?)</a>";
            String HTML_HREF_TAG_PATTERN = "\\s*(?i)href\\s*=\\s*(\"([^\"]*\")|'[^']*'|([^'\">\\s]+))";
            pTag = Pattern.compile(HTML_TAG_PATTERN);
            pLink = Pattern.compile(HTML_HREF_TAG_PATTERN);

            mTag = pTag.matcher(content);
                while (mTag.find()) {
                    if(visited.size() == m_maxNodes) {

                        break;
                    }
                    String href = mTag.group(1);     // get the values of href
                    mLink = pLink.matcher(href);

                    while (mLink.find()) {

                        if(visited.size() == m_maxNodes) {

                            break;
                        }
                                String address = mLink.group(1);
                                address = address.replace("\"","");
                                if (address != null && (!CrawlerForbiddenURL.containsKey(address))) {
                                    System.out.println(address);
                                    if (address.startsWith("/wiki/") && !address.contains(":") && (!address.contains("#"))) {
                                        //System.out.println(parent + " " + address);
                                        graphNode node = new graphNode(parent, address);

                                        if (!parent.equalsIgnoreCase(address)) {
                                            if (!visited.contains(address) && !visitedIrrelevant.contains(address)) {


                                                System.out.println("Downloading " + countDownload + " " + address + "  Visited" + visited.size());
                                                countDownload++;
                                                if (node.DownloadPage(BASE_URL)) {
                                                    if (IsRelevant(node)) {
                                                        links.add(node);
                                                        if (!uniqueEdges.containsKey(address)) {
                                                            uniqueEdges.put(address, node);
                                                            edges.add(node);
                                                        }
                                                        visited.add(address);
                                                    } else {
                                                        visitedIrrelevant.add(address);
                                                    }
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

                }
            queue.addAll(links);
            crawled.addAll(edges);

        }
        catch (Exception e){

            e.printStackTrace();
        }
    }
    public  void extractEdges(final Reader reader, final String parent) throws IOException {


        final HashMap<String,graphNode> uniqueEdges = new HashMap<String,graphNode>();
        final ParserDelegator parserDelegator = new ParserDelegator();
        final ParserCallback parserCallback = new ParserCallback() {
            public void handleText(final char[] data, final int pos) {
            }
            public void handleStartTag(HTML.Tag tag, MutableAttributeSet attribute, int pos) {
                if (tag == HTML.Tag.A) {
                    String address = (String) attribute.getAttribute(HTML.Attribute.HREF);
                    if (address != null && address.compareToIgnoreCase(parent)!=0) {
                        if (address.startsWith("/wiki/") && (!address.contains(":")) && (!address.contains("#"))) {
                            graphNode node = new graphNode(parent, address);

                            if (visited.contains(address)) {
                                uniqueEdges.put(address, node);
                            }

                        }
                    }
                }
            }

        };

        parserDelegator.parse(reader, parserCallback, false);
        crawled.addAll(uniqueEdges.values());

    }
}




class graphNode {

    static int count=0;
    String parent, child, content;


    graphNode(String src, String link) {
        parent = src;
        child = link;
    }

    Boolean DownloadPage(String baseURL){
        try{
            if(count%100 == 1)
            {
                Thread.sleep(5000);
            }
            count++;

            URL urlStream = new URL(baseURL+this.child);

           // System.out.println("Downloading "+count);
            InputStream is = urlStream.openStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line = null,page=null;
            line = br.readLine();
            this.content = line.toLowerCase();
            while( (line = br.readLine())!= null)
            {
                this.content += line.toLowerCase();
            }

        } catch (Exception e) {

            System.out.println("Exception during file download "+e.getLocalizedMessage());
            return false;
        }
        return true;

    }
}