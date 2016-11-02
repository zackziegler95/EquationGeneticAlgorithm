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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class NFLParser2 {
    private final String[] teamNames = new String[]{"crd", "atl", "rav", "buf",
        "car", "chi", "cin", "cle", "dal", "den", "det", "gnb", "htx", "clt", "jax",
        "kan", "mia", "min", "nwe", "nor", "nyg", "nyj", "rai", "phi", "pit", "sdg",
        "sfo", "sea", "ram", "tam", "oti", "was"};
    // Issues: LAA, MIA, TBR
    
    private HashMap<String, double[]> teams = new HashMap<>(); // offense yards, defence yards
    public ArrayList<double[]> dataPoints = new ArrayList<>(); // {x1, x2, x3, x4, y}
    
    public NFLParser2() {
        //printGameDatapoints();
        readGameDatapoints();
    }
    
    private void readGameDatapoints() {
        int n = 8;
        String pattern = "(-?\\d+\\.\\d+)";
        for (int i = 0; i < n-1; i++) {
            pattern = "(-?\\d+\\.\\d+), "+pattern;
        }
        
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader("NFL2GameDatapoints.txt"));
        } catch (FileNotFoundException ex) {
            System.err.println("Error: file not found in readGameDatapoints");
            System.exit(1);
        }
        String line;
        
        try {
            while ((line = br.readLine()) != null) {
                Pattern p1 = Pattern.compile(pattern);
                Matcher m = p1.matcher(line);
                if (m.find()) {
                    double[] values = new double[n];
                    for (int i = 0; i < n; i++) {
                        values[i] = Double.parseDouble(m.group(i+1));
                    }
                    dataPoints.add(values);
                } else {
                    System.err.println("Error, unable to match regex");
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(NFLParser2.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void printGameDatapoints() {
        int n = 0;
        PrintWriter w = null;
        try {
            w = new PrintWriter(new File("NFL2GameDatapoints.txt"));
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

            Element table = doc.getElementById("team_gamelogs").child(2);
            
            for (int i = 0; i < 17; i++) { // 17 weeks in a regular season (1 bye week)
                Element row = table.child(i);
                if (row.child(4).text().equals("")) continue; // Deal with the bye week
                
                String team1 = teamName;
                String team2 = row.child(8).child(0).attr("href").substring(7, 10);
                List<String> names = Arrays.asList(teamNames);
                
                if (names.indexOf(team2) < 0) {
                    System.err.println("Error: name not found");
                    System.exit(1);
                }
                
                if (names.indexOf(team1) > names.indexOf(team2)) continue;
                n++;
                
                double isHome = row.child(7).text().equals("") ? 50 : -50;
                double team1PassY = Double.parseDouble(row.child(13).text());
                double team1RushY = Double.parseDouble(row.child(14).text());
                String team1ToString = row.child(15).text();
                double team1TO = team1ToString.equals("") ? 0 : Double.parseDouble(team1ToString);
                double team2PassY = Double.parseDouble(row.child(18).text());
                double team2RushY = Double.parseDouble(row.child(19).text());
                String team2ToString = row.child(20).text();
                double team2TO = team2ToString.equals("") ? 0 : Double.parseDouble(team2ToString);
                
                double diff = Double.parseDouble(row.child(9).text())- Double.parseDouble(row.child(10).text());
                
                w.println(isHome+", "+team1PassY+", "+team1RushY+
                        ", "+team1TO+", "+team2PassY+", "+team2RushY+", "+team2TO+", "+diff);
            }
        }
        w.close();
        System.out.println("Total games: "+n);
    }
}
