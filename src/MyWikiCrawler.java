/**
 * Created by Pavithra & Kiana on 4/2/2016.
 */
public class MyWikiCrawler {
    public static void  main(String[] arg){

        String []keywords ={"facebook","founder"};
        WikiCrawler cr = new WikiCrawler("/wiki/facebook", keywords, 1000, "D:\\MyWikiGraph.txt");

    }
}
