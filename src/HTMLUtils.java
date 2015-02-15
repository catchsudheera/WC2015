/**
 * Created by sudheera on 2/15/15.
 */
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import com.gargoylesoftware.htmlunit.javascript.host.html.HTMLAnchorElement;

import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class HTMLUtils {
    public static void main(String[] args) throws Exception{
        java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(java.util.logging.Level.OFF);

        String[] author_url=new String[5];
        author_url[0] =  "http://www.baiscopelk.com/author/dxx397/page/";
        author_url[1] =  "http://www.baiscopelk.com/author/admin/page/";
        author_url[2] =  "http://www.baiscopelk.com/author/d533rx9/page/";
        author_url[3] =  "http://www.baiscopelk.com/author/r8kowiki/page/";
        author_url[4] =  "http://www.baiscopelk.com/author/hw85bek/page/";

        //PrintWriter writer = new PrintWriter("url_list.txt", "UTF-8");///////////////////////////////////////////////
        ArrayList urlListforAuthor=new ArrayList();

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        FileInputStream fis = new FileInputStream("url_list.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(fis));

        System.out.println("Reading File line by line using BufferedReader");

        String line = reader.readLine();
        int s=1;
        while(line != null){
            System.out.println(s++ +"  : " +line);
            urlListforAuthor.add(line);
            line = reader.readLine();
        }

        reader.close();
        fis.close();
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


        for(int t=0;t<author_url.length;t++){
            getDownloadURLList(urlListforAuthor);
            break;/////////////////////////////////////////////////////////////////////////////////////////////////////
        }
        //writer.close();/////////////////////////////////////////////////////////////////////////////////////////////
    }

    public static ArrayList getDownloadURLList(ArrayList urlList) throws IOException {

        PrintWriter writer_dw = new PrintWriter("dw_list.txt", "UTF-8");
        PrintWriter writer_dw_nt = new PrintWriter("dw_list_nt_found.txt", "UTF-8");

        ArrayList downList = new ArrayList();

        System.out.println("initiating the connection..");
        WebClient webClient = new WebClient(BrowserVersion.FIREFOX_24);
        int dw_count=1;
        Iterator iter = urlList.iterator();

        while(iter.hasNext()){
            System.out.println(dw_count++);
            String address = (String)iter.next();
            System.out.println("getting down page for " + address);
            HtmlPage page;
            try{
                page = webClient.getPage(address);
            }
            catch (FailingHttpStatusCodeException e){
                System.out.println("========================================================");
                System.out.println(e.getStatusCode() + e.getStatusMessage());
                System.out.println("========================================================");
                continue;
            }

            String dw_url="null";
            DomNode firstByXPath = (DomNode)page.getFirstByXPath("/html/body/div[3]/div[1]/article/div/div[2]");
            List divlist=new ArrayList();
            try{
                divlist = ((HtmlDivision) firstByXPath).getHtmlElementsByTagName("div");
            }catch (NullPointerException n){
                System.out.println("---------------------------------------------------------");
                System.out.println(n.toString());
                System.out.println("---------------------------------------------------------");
                continue;
            }
            Iterator it =divlist.iterator();

            while(it.hasNext()){
                HtmlDivision div = (HtmlDivision)it.next();
                Object isAnchor = div.getFirstByXPath("a");
                //System.out.println("printing anchor : "+isAnchor);
                if (isAnchor==null){
                    writer_dw_nt.println(page.getUrl());
                    writer_dw_nt.flush();
                    continue;
                }
                else if(isAnchor!=null){
                    dw_url=((HtmlAnchor)isAnchor).getHrefAttribute();
                    System.out.println("checking down link  : " + dw_url);
                    if(dw_url.contains("/download-monitor/download.php?id")){

                        System.out.println("down link fetched : " + dw_url);
                        downList.add(dw_url);
                        writer_dw.println(dw_url);
                        System.out.println("******************************************");
                        writer_dw.flush();
                        break;
                    }else{
                        if(!it.hasNext()){
                            writer_dw_nt.println(page.getUrl());
                            writer_dw_nt.flush();
                        }
                    }
                }
            }
        }

        webClient.closeAllWindows();
        writer_dw.close();
        writer_dw_nt.close();
        return downList;
    }


    public static ArrayList getUrlListforAuthor(String url_author) throws IOException {

        ArrayList url_all = new ArrayList();
        System.out.println("initiating the connection..");

        WebClient webClient = new WebClient(BrowserVersion.FIREFOX_24);
        System.out.println("getting first page " +url_author+"1");

        HtmlPage page = webClient.getPage(url_author+"1/");
        int pagecount = getPagecount(page);
        System.out.println("fetching from url"+url_author);
        System.out.println("page count : "+pagecount);


        for (int i=1;i<pagecount;i++){
            System.out.println("getting page " +url_author+i+"/");
            page = webClient.getPage(url_author+i+"/");
            System.out.println("got page "+url_author+i+"/");
            ArrayList subPageLinksList = getSubPageLinks(page);
            Iterator iterator = subPageLinksList.iterator();
            while(iterator.hasNext()){
                url_all.add(iterator.next());
            }
        }
        System.out.println("done for author " + url_author);
        webClient.closeAllWindows();
        return url_all;
    }


    public static ArrayList getSubPageLinks(HtmlPage page){
        ArrayList urls = new ArrayList();

        DomNode firstChild = (page.getHtmlElementById("main-content").getFirstChild()).getNextSibling().getFirstChild().getNextSibling().getNextSibling().getNextSibling().getFirstChild();

        DomNode node = firstChild.getNextSibling();

        int s=0;
        while (s<20){
            DomNode curr =  node.getFirstChild().getNextSibling().getFirstChild();
            String url = ((HtmlAnchor) curr).getHrefAttribute();
            urls.add(url);
            node = node.getNextSibling().getNextSibling();
            s++;
        }
        return urls;
    }

    public static int getPagecount(HtmlPage page){
        System.out.println("Getting pagecount...");
        int count=0;
        String page_l = (page.getHtmlElementById("main-content").getFirstChild()).getNextSibling().getFirstChild().getNextSibling().getNextSibling().getNextSibling().getNextSibling().getNextSibling().getFirstChild().getNextSibling().asText();
        String[] split = page_l.split("of ");
        count = Integer.parseInt(split[1]);
        return count;
    }
}
