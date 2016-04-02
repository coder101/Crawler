/**
 * Created by Pavithra & Kiana on 4/2/2016.
 */
public class MyWikiCrawler {
    public static void  main(String[] arg){

        String []keywords ={"music"};
        WikiCrawler cr = new WikiCrawler("/wiki/Music", keywords, 1000, "D:\\MyWikiGraph.txt");

        cr.crawl();
    }
}
