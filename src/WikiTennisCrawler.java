/**
 * Created by Pavithra & Kiana on 4/2/2016.
 */
public class WikiTennisCrawler {
    public static void  main(String[] arg){

        String []keywords ={"tennis","grand slam"};

        WikiCrawler cr = new WikiCrawler("/wiki/tennis", keywords, 1000, "D:\\WikiTennisGraph.txt");
    }
}
