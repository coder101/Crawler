/**
 * Created by Pavithra & Kiana on 4/2/2016.
 */
public class MyWikiCrawler {
    public static void main(String[] arg) {

        String[] keywords = {"music", "guitar", "vocals"};
        String urlSubString = "/wiki/Music";
        String outputFilename = "D:\\MyWikiGraph.txt";
        Integer maxGraphNodes = 1000;

        WikiCrawler cr = new WikiCrawler(urlSubString, keywords, maxGraphNodes, outputFilename);
        cr.crawl();
    }
}
