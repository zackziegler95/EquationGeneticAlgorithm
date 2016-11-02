package equationga;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class MLBParser {
    private final String[] teamNames = new String[]{"ARI", "ATL", "BAL", "BOS", // ANA vs LAA depending on year
        "CHC", "CHW", "CIN", "CLE", "COL", "DET", "HOU", "KCR", "LAA", "LAD", "MIA", "MIL",
        "MIN", "NYM", "NYY", "OAK", "PHI", "PIT", "SDP", "SFG", "SEA", "STL", "TBR", "TEX",
        "TOR", "WSN"};
    // Issues: LAA, MIA, TBR
    
    private HashMap<String, double[]> teams = new HashMap<>(); // Runs, hits, batting average
    public ArrayList<double[]> dataPoints; // {x1, x2, x3, x4, x5, x6, y}
    
    private void printHomeRecords() {
        for (String teamName : teamNames) {
            String url = "http://www.baseball-reference.com/teams/"+teamName+"/2013-schedule-scores.shtml";
            Document doc = null;
            try {
                doc = Jsoup.connect(url).get();
            } catch (IOException ex) {
                System.err.println(ex.getStackTrace());
            }

            Element table = doc.getElementById("team_schedule").child(2);
            
            int homeGames = 0;
            int homeWins = 0;
            int allGames = 162;
            int allWins = 0;
            for (int i = 0; i < allGames; i++) {
                Element row = table.child(i);
                String isHome = row.child(5).text();
                String isWin = row.child(7).text();
                
                if (isHome.equals("@")) {
                    homeGames++;
                    if (isWin.equals("W")) {
                        homeWins++;
                    }
                }
                
                if (isWin.equals("W")) {
                    allWins++;
                }
            }
            System.out.println(teamName+", Home: "+(1.0*homeWins/homeGames)+", Overall: "+(1.0*allWins/allGames));
        }
    }
    
    public MLBParser() {
        dataPoints = new ArrayList<>();
        printHomeRecords();
        
        //readData();
        //readDataPoints();
        /*for (double[] point : dataPoints) {
            System.out.print("{");
            for (int i = 0; i < point.length-1; i++) {
                System.out.print(point[i]+", ");
            }
            System.out.println(point[point.length-1]+"}, ");
        }*/
        /*
        for (String teamName : teamNames) {
            String url = "http://www.baseball-reference.com/teams/"+teamName+"/2013.shtml";
            Document doc = null;
            try {
                doc = Jsoup.connect(url).get();
            } catch (IOException ex) {
                System.err.println(ex.getStackTrace());
            }

            Elements poptips = doc.getElementsByClass("poptip");
            poptips.remove(0);
            
            for (Element e : poptips) {
                String tip = e.attr("tip");
                if (tip.equals("off day")) continue;
                
                Pattern p1 = Pattern.compile(".*\\d, ([A-Z]{3})[^A-Z]+([A-Z]{3}), (\\d+)-(\\d+)");
                Matcher m = p1.matcher(tip);
                
                if (m.find()) {
                    int diff = Integer.parseInt(m.group(3))-Integer.parseInt(m.group(4));
                    String team1 = m.group(1);
                    String team2 = m.group(2);
                    
                    //if (diff < 0) continue;
                    List<String> names = Arrays.asList(teamNames);
                    if (names.indexOf(team1) > names.indexOf(team2)) continue;
                    
                    double[] team1Data = teams.get(team1);
                    double[] team2Data = teams.get(team2);
                    
                    //dataPoints.add(new double[]{team1Data[0], team1Data[1], team1Data[2],
                    //    team2Data[0], team2Data[1], team2Data[2], diff});
                    
                    dataPoints.add(new double[]{team1Data[0], team2Data[0], diff});
                    //dataPoints.add(new double[]{team1Data[0], diff});
                    //totalDiff += diff;
                    
                    System.out.println(m.group(1)+", "+m.group(2)+": "+diff);
                } else {
                    System.err.println("Error, didn't find anything");
                }
            }
            System.out.println(dataPoints.size());
            //dataPoints.add(new double[]{teams.get(teamName)[0], 1.0*totalDiff/poptips.size()});
        }
        printDataPoints();
        /*for (double[] point : dataPoints) {
            //System.out.print("{"+point[0]+", "+point[1]+", "+point[2]+"}, ");
            System.out.print("{"+point[0]+", "+point[1]+"}, ");
        }*/
        
        /*for (Element e : totals) {
            System.out.println(e);
            System.out.println(e.child(0));
        }*/
    }
    
    private void readDataPoints() {
        try {
            BufferedReader br = new BufferedReader(new FileReader("datapoints.txt"));
            String line;
            
            while ((line = br.readLine()) != null) {
                //Pattern p1 = Pattern.compile("(\\d+\\.\\d+), (\\d+\\.\\d+), (\\d+\\.\\d+), (\\d+\\.\\d+), (\\d+\\.\\d+), (\\d+\\.\\d+), (\\d+\\.\\d+)");
                Pattern p1 = Pattern.compile("(\\d+\\.\\d+), (\\d+\\.\\d+), (\\d+\\.\\d+)");
                Matcher m = p1.matcher(line);

                if (m.find()) {
                    double[] values = new double[]{Double.parseDouble(m.group(1)),
                        Double.parseDouble(m.group(2)),
                        Double.parseDouble(m.group(3))};

                    dataPoints.add(values);
                } else {
                    System.err.println("Error, unable to match regex");
                }
            }
        } catch (IOException ex) {
            System.err.println(ex.getStackTrace());
        }
    }
    
    private void printDataPoints() {
        PrintWriter w = null;
        try {
            w = new PrintWriter(new File("datapoints.txt"));
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
            br = new BufferedReader(new FileReader("data.txt"));
        } catch (FileNotFoundException ex) {
            System.err.println(ex.getStackTrace());
        }
        for (String teamName : teamNames) {
            try {
                String line = br.readLine();
                
                Pattern p1 = Pattern.compile("(\\d+\\.\\d+), (\\d+\\.\\d+), (\\d+\\.\\d+)");
                Matcher m = p1.matcher(line);
                
                if (m.find()) {
                    double[] values = new double[]{Double.parseDouble(m.group(1)),
                        Double.parseDouble(m.group(2)),
                        Double.parseDouble(m.group(3))};
                    
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
            w = new PrintWriter(new File("data.txt"));
        } catch (FileNotFoundException ex) {
            System.err.println(ex.getStackTrace());
        }
        
        for (String teamName : teamNames) {
            String url = "http://www.baseball-reference.com/teams/"+teamName+"/2013.shtml";
            Document doc = null;
            try {
                doc = Jsoup.connect(url).get();
            } catch (IOException ex) {
                System.err.println(ex.getStackTrace());
            }
            Element battingTotals = doc.getElementsByClass("stat_total").get(0);
            //System.out.println(battingTotals+". "+battingTotals.childNodeSize());

            double[] values = new double[]{Double.parseDouble(battingTotals.child(7).text()),
                Double.parseDouble(battingTotals.child(8).text()),
                Double.parseDouble(battingTotals.child(17).text())};
            //teams.put("BOS", values);
            w.println(values[0]+", "+values[1]+", "+values[2]);
        }
        w.close();
    }
}
