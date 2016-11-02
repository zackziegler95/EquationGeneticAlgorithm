package equationga;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class NFLParser {
    private final String[] teamNames = new String[]{"crd", "atl", "rav", "buf",
        "car", "chi", "cin", "cle", "dal", "den", "det", "gnb", "htx", "clt", "jax",
        "kan", "mia", "min", "nwe", "nor", "nyg", "nyj", "rai", "phi", "pit", "sdg",
        "sfo", "sea", "ram", "tam", "oti", "was"};
    // Issues: LAA, MIA, TBR
    
    private HashMap<String, double[]> teams = new HashMap<>(); // offense yards, defence yards
    public ArrayList<double[]> dataPoints; // {x1, x2, x3, x4, y}
    
    private void printTeamNames() {
        String url = "http://www.pro-football-reference.com/teams/";
        Document doc = null;
        try {
            doc = Jsoup.connect(url).get();
        } catch (IOException ex) {
            System.err.println(ex.getStackTrace());
        }
        
        Element table = doc.getElementById("teams_active").child(2);
        
        for (int i = 0; i < 32; i++) {
            Element a = table.child(i).child(0).child(0);
            String name = a.attr("href").substring(7, 10);
            System.out.print("\""+name+"\", ");
        }
    }
    
    private void printHomeRecords() {
        for (String teamName : teamNames) {
            String url = "http://www.pro-football-reference.com/teams/"+teamName+"/2013.htm";
            Document doc = null;
            try {
                doc = Jsoup.connect(url).get();
            } catch (IOException ex) {
                System.err.println(ex.getStackTrace());
            }

            Element table = doc.getElementById("team_gamelogs").child(2);
            
            int homeGames = 0;
            int homeWins = 0;
            int allGames = 0;
            int allWins = 0;
            
            for (int i = 0; i < 17; i++) { // 17 weeks in a regular season (1 bye week)
                Element row = table.child(i);
                if (row.child(4).text().equals("")) continue;
                
                String isHome = row.child(7).text();
                String isWin = row.child(4).text();
                
                if (isHome.equals("")) {
                    homeGames++;
                    if (isWin.equals("W")) {
                        homeWins++;
                    }
                }
                
                allGames++;
                if (isWin.equals("W")) {
                    allWins++;
                }
            }
            System.out.println(teamName+", Home: "+(1.0*homeWins/homeGames)+", Overall: "+(1.0*allWins/allGames));
        }
    }
    
    public NFLParser() {
        dataPoints = new ArrayList<>();
        
        //printData();
        //readData();
        readDataPoints();
        //printHomeRecords();
        
        /*for (double[] point : dataPoints) {
            System.out.print("{");
            for (int i = 0; i < point.length-1; i++) {
                System.out.print(point[i]+", ");
            }
            System.out.print(point[point.length-1]+"}, ");
        }*/
        /*
        for (String teamName : teamNames) {
            String url = "http://www.pro-football-reference.com/teams/"+teamName+"/2013.htm";
            Document doc = null;
            try {
                doc = Jsoup.connect(url).get();
            } catch (IOException ex) {
                System.err.println(ex.getStackTrace());
            }

            Element table = doc.getElementById("team_gamelogs").child(2);
            
            for (int i = 0; i < 17; i++) { // 17 weeks in a regular season (1 bye week)
                Element row = table.child(i);
                if (row.child(4).text().equals("")) continue;
                
                String team1 = teamName;
                String team2 = row.child(8).child(0).attr("href").substring(7, 10);
                int home = row.child(7).text().equals("@") ? -50 : 50;
                
                List<String> names = Arrays.asList(teamNames);
                if (names.indexOf(team1) > names.indexOf(team2)) continue;
                
                double[] team1Data = teams.get(team1);
                double[] team2Data = teams.get(team2);

                int diff = Integer.parseInt(row.child(9).text())-Integer.parseInt(row.child(10).text());
                
                dataPoints.add(new double[]{team1Data[0]-team1Data[1],
                    team2Data[0]-team2Data[1], home, diff});
            }
        }
        printDataPoints();//*/
    }
    
    private void readDataPoints() {
        try {
            BufferedReader br = new BufferedReader(new FileReader("NFLdatapoints.txt"));
            String line;
            
            while ((line = br.readLine()) != null) {
                Pattern p1 = Pattern.compile("(-?\\d+\\.\\d+), (-?\\d+\\.\\d+), (-?\\d+\\.\\d+), (-?\\d+\\.\\d+)");
                Matcher m = p1.matcher(line);

                if (m.find()) {
                    double[] values = new double[]{Double.parseDouble(m.group(1)),
                        Double.parseDouble(m.group(2)),
                        Double.parseDouble(m.group(3)),
                        Double.parseDouble(m.group(4))};

                    dataPoints.add(values);
                } else {
                    System.err.println("Error, unable to match regex: "+line);
                }
            }
        } catch (IOException ex) {
            System.err.println(ex.getStackTrace());
        }
    }
    
    private void printDataPoints() {
        PrintWriter w = null;
        try {
            w = new PrintWriter(new File("NFLdatapoints.txt"));
        } catch (FileNotFoundException ex) {
            System.err.println(ex.getStackTrace());
        }
        
        for (double[] point : dataPoints) {
            for (int i = 0; i < point.length-1; i++) {
                w.print(point[i]+", ");
            }
            w.println(point[point.length-1]);
        }
        w.close();
    }
    
    private void readData() {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader("NFLdata.txt"));
        } catch (FileNotFoundException ex) {
            System.err.println(ex.getStackTrace());
        }
        for (String teamName : teamNames) {
            try {
                String line = br.readLine();
                
                Pattern p1 = Pattern.compile("(\\d+), (\\d+)");
                Matcher m = p1.matcher(line);
                
                if (m.find()) {
                    double[] values = new double[]{Double.parseDouble(m.group(1)),
                        Double.parseDouble(m.group(2))};
                    
                    teams.put(teamName, values);
                } else {
                    System.err.println("Error, unable to match regex");
                }
                
            } catch (IOException ex) {
                System.err.println(ex.getStackTrace());
            }
        }
    }
    
    private void printData() {
        PrintWriter w = null;
        try {
            w = new PrintWriter(new File("NFLdata.txt"));
        } catch (FileNotFoundException ex) {
            System.err.println(ex.getStackTrace());
        }
        
        for (String teamName : teamNames) {
            String url = "http://www.pro-football-reference.com/teams/"+teamName+"/2013.htm";
            Document doc = null;
            try {
                doc = Jsoup.connect(url).get();
            } catch (IOException ex) {
                System.err.println(ex.getStackTrace());
            }
            Element stats = doc.getElementById("team_stats").child(2);
            Element offense = stats.child(0);
            Element defense = stats.child(1);
            //System.out.println(battingTotals+". "+battingTotals.childNodeSize());

            w.println(offense.child(1).text()+", "+defense.child(1).text());
        }
        w.close();
    }
}
