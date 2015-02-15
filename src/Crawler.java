import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import com.sun.corba.se.impl.encoding.OSFCodeSetRegistry;

import java.io.*;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by sudheera on 2/15/15.
 */
public class Crawler {

    HashMap<String,Integer> grounds;
    HashMap<String,Integer> teams;

    TreeMap<String,Integer> poolA;
    TreeMap<String,Integer> poolB;

    public static void main(String[] args) throws InterruptedException, IOException {

        Crawler crawler = new Crawler();

        crawler.setupData();
        crawler.getFixtures();
        crawler.viewResults();


        //MatchResults results = crawler.getResults("Bangladesh", "Sri Lanka", "Berri Oval", "any");
        System.out.println("done...");


    }

    private void viewResults(){

        System.out.println("Pool A results");
        for (String s: poolA.keySet()){
            System.out.println(s+"  "+poolA.get(s));
        }

        System.out.println("Pool B results");
        for (String s: poolB.keySet()){
            System.out.println(s+"  "+poolB.get(s));
        }


    }

    private void getFixtures(){
        java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(java.util.logging.Level.OFF);

        String fixturesUrl="http://www.espncricinfo.com/icc-cricket-world-cup-2015/content/series/509587.html?template=fixtures";
        HtmlPage page = getPage(fixturesUrl);

        HtmlUnorderedList unorderedList = page.getFirstByXPath("//*[@id=\"page\"]/div[3]/div/div[3]/div/div[1]/div[3]/ul");
        DomNodeList<HtmlElement> li = unorderedList.getElementsByTagName("li");
        for (HtmlElement l:li){
            if((((HtmlDivision) l.getFirstByXPath("div[2]")).getTextContent()).contains("Quarter-Final")){
                break;
            }


            String[] fixtureDetails = ((HtmlDivision) l.getFirstByXPath("div[2]")).getTextContent().replace("\t", "").replace("LIVE", "").trim().replace("D/N","").trim().split("\n");


            String match = fixtureDetails[0].trim();
            String ground = fixtureDetails[fixtureDetails.length-1].trim();


            String[] matchNo = match.split(",");
            System.out.println("getting fixtures : "+matchNo[0]);

            String[] twoTeams = matchNo[1].trim().split("-");
            String poolName=twoTeams[0].trim();

            String[] team = twoTeams[1].trim().split(" v ");


            String teamA = team[0].trim();
            String teamB = team[1].trim();

            String teamWon="no";

            MatchResults results = getResults(teamA, teamB, ground, "any");

            if(results.getTeamAWins()>results.getTeamBWins()){
                teamWon=teamA;
            } else if(results.getTeamAWins()<results.getTeamBWins()){
                teamWon=teamB;
            }

            if(!teamWon.equals("no")) {
                if (poolName.equals("Pool A")) {
                    poolA.put(teamWon, (poolA.get(teamWon)+1));
                } else {
                    poolB.put(teamWon, (poolB.get(teamWon)+1));
                }
            }
        }


    }

    private HtmlPage getPage(String url) {

        WebClient webClient = new WebClient(BrowserVersion.FIREFOX_24);
        HtmlPage page=null;
        try{
            System.out.println("loading page : "+url);
            page = webClient.getPage(url);
            System.out.println("page loaded : "+url);
        }
        catch (FailingHttpStatusCodeException e){
            System.out.println("========================================================");
            System.out.println(e.getStatusCode() + e.getStatusMessage());
            System.out.println("========================================================");
            System.out.println("wait for 10 seconds..");
            Thread.sleep(10000);
            System.out.println("retrying....");
            page = getPage(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            return page;
        }
    }

    private void setupData() throws IOException {
        java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(java.util.logging.Level.OFF);

        String groundsFileLocation="grounds.map";
        String teamFileLocation="teams.map";

        File groundsFile = new File(groundsFileLocation);
        if(groundsFile.exists() && !groundsFile.isDirectory()) {
            System.out.println("Loading serialized file : " + groundsFileLocation);
            grounds = (HashMap<String, Integer>)deSerializeObj(groundsFileLocation);
        }else {
            grounds = new HashMap<String, Integer>();

            String groundsURL="http://www.espncricinfo.com/ci/content/ground/index.html";
            HtmlPage groundsPage = getPage(groundsURL);
            HtmlSelect select = groundsPage.getFirstByXPath("//*[@id=\"gnd_searchGuru\"]/div/div[1]/form/select");

            for (HtmlOption option : select.getOptions()){
                String groundName = option.getText();
                int groundNo = Integer.parseInt(option.getValueAttribute());

                grounds.put(groundName.trim(),groundNo);
            }

            serializeObj(grounds,groundsFileLocation);
        }

        File teamsFile = new File(teamFileLocation);
        if(teamsFile.exists() && !teamsFile.isDirectory()) {
            System.out.println("Loading serialized file : " + teamFileLocation);
            teams = (HashMap<String, Integer>)deSerializeObj(teamFileLocation);
        }else {
            teams = new HashMap<String, Integer>();

            String teamURL= "http://www.espncricinfo.com/ci/content/site/cricket_squads_teams/index.html";

            HtmlPage teamsPage = getPage(teamURL);

            HtmlTable table = (HtmlTable)teamsPage.getFirstByXPath("//*[@id=\"ciHomeContentlhs\"]/div[3]/table[1]");
            for (HtmlTableRow row : table.getRows()) {

                for (HtmlTableCell cell : row.getCells()) {

                    if (cell.getFirstChild().hasAttributes()) {
                        String hrefNo = cell.getFirstChild().getAttributes().getNamedItem("href").getTextContent().replaceAll("[\\W]|_", "").replaceAll("[a-zA-Z]", "");
                        String countryName = cell.getTextContent();
                        teams.put(countryName.trim(), Integer.parseInt(hrefNo));
                    }
                }
            }
            serializeObj(teams,teamFileLocation);
        }


        poolA=new TreeMap<String, Integer>();
        poolB=new TreeMap<String, Integer>();

        poolA.put("England",0);
        poolA.put("Australia",0);
        poolA.put("Sri Lanka",0);
        poolA.put("Bangladesh",0);
        poolA.put("New Zealand",0);
        poolA.put("Afghanistan",0);
        poolA.put("Scotland",0);


        poolB.put("South Africa",0);
        poolB.put("India",0);
        poolB.put("Pakistan",0);
        poolB.put("West Indies",0);
        poolB.put("Zimbabwe",0);
        poolB.put("Ireland",0);
        poolB.put("United Arab Emirates",0);

    }

    private MatchResults getResults(String teamAName, String teamBName, String groundName, String country) {


        int teamA = teams.get(teamAName);
        int teamB = teams.get(teamBName);
        int ground=-100;

        if(grounds.containsKey(groundName)) {
            ground = grounds.get(groundName);
        }else if(!groundName.equals("any")){
            boolean hasGround=false;
            for (String s : grounds.keySet()) {
                if(s.contains(groundName)){
                    ground=grounds.get(s);
                    hasGround=true;
                    break;
                }
            }
            if (!hasGround){
                for (String partName : groundName.split(",")) {
                    if (hasGround){
                        break;
                    }
                    for (String s : grounds.keySet()) {
                        if(s.contains(partName)){
                            ground=grounds.get(s);
                            hasGround=true;
                            break;
                        }
                    }

                }
            }


        }

        System.out.println("fetching results for : "+teamAName+" vs " +teamBName+" at "+groundName);

        MatchResults results = new MatchResults();
        String matchResultUrl= "http://stats.espncricinfo.com/ci/engine/stats/index.html?class=2;result=1;result=2;spanmin1=15+Feb+2011;spanval1=span;template=results;type=team;view=results";
        String teamUrl = ";team="+teamA;
        String oppositionUrl = ";opposition="+teamB;
        String groundUrl="";
        if(ground>0) {
            groundUrl = ";ground="+ground;
        }
        String hostUrl="";
        if (country.equalsIgnoreCase("Australia")){
            hostUrl=";host=2";
        }else if(country.equalsIgnoreCase("New Zealand")){
            hostUrl=";host=5";
        }

        String queryString=matchResultUrl+teamUrl+oppositionUrl+groundUrl;
        int currentPageCount=0;
        int allPagesCount;

        do {
            currentPageCount++;
            HtmlPage page = getPage(queryString+";page="+currentPageCount);

            HtmlTableCell noResultCell = page.getFirstByXPath("//*[@id=\"ciHomeContentlhs\"]/div[3]/table[3]/tbody/tr/td");
            if (noResultCell.getTextContent().equalsIgnoreCase("No records available to match this query") && !groundName.equals("any")){
                return getResults(teamAName,teamBName,"any",country);
            }else if (noResultCell.getTextContent().equalsIgnoreCase("No records available to match this query")){
                return results;
            }

            HtmlTableCell pageCountCell = page.getFirstByXPath("//*[@id=\"ciHomeContentlhs\"]/div[3]/table[4]/tbody/tr/td[1]");
            String textContent = pageCountCell.getTextContent();
            System.out.println("    getting results for "+textContent.trim());
            String[] split = textContent.replace("Page", "").trim().split("of");
            allPagesCount = Integer.parseInt(split[1].trim());


            HtmlTableBody resultsTable = (HtmlTableBody) page.getFirstByXPath("//*[@id=\"ciHomeContentlhs\"]/div[3]/table[3]/tbody");

            for (HtmlTableRow row : resultsTable.getRows()) {

                List<HtmlTableCell> cells = row.getCells();

                if(cells.get(1).getTextContent().trim().equalsIgnoreCase("won")){
                    results.UpTeamAWins();
                }else if(cells.get(1).getTextContent().trim().equalsIgnoreCase("lost")){
                    results.UpTeamBWins();
                }

            }
        }while(currentPageCount<allPagesCount);






        return results;
    }

    private void serializeObj(Object obj,String location) throws IOException {

//        OutputStream file = new FileOutputStream(location);
//        OutputStream buffer = new BufferedOutputStream(file);
//        ObjectOutput output = new ObjectOutputStream(buffer);
//        try {
//              output.writeObject(obj);
//        }
//        catch(IOException ex){
//            System.out.println("Cannot perform output." + ex);
//        }
    }

    private Object deSerializeObj(String location) throws IOException {

        InputStream file = new FileInputStream(location);
        InputStream buffer = new BufferedInputStream(file);
        ObjectInput input = new ObjectInputStream (buffer);

        try{

            Object recoveredObj = input.readObject();
            return recoveredObj;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }

    }

}
