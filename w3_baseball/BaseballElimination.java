import edu.princeton.cs.algs4.FlowEdge;
import edu.princeton.cs.algs4.FlowNetwork;
import edu.princeton.cs.algs4.FordFulkerson;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BaseballElimination {

    private final String[] teams;
    private final boolean[] status;
    private final int[] wins;
    private final int[] losses;
    private final int[] left;
    private final int[][] against;
    private final int numTeams;
    private int topTeam;
    private FordFulkerson fFulkerson;
    private int teamV;
    private int gameV;

    public BaseballElimination(String filename) {
        int topWins = 0;
        topTeam = 0;
        In in = new In(filename);
        numTeams = Integer.parseInt(in.readLine());
        teams = new String[numTeams];
        status = new boolean[numTeams];
        wins = new int[numTeams];
        losses = new int[numTeams];
        left = new int[numTeams];
        against = new int[numTeams][numTeams];
        int i = 0;
        while (in.hasNextLine()) {
            String line = in.readLine().replaceFirst("^\\s+", "");
            String[] words = line.split("\\s+");
            teams[i] = words[0];
            wins[i] = Integer.parseInt(words[1]);
            losses[i] = Integer.parseInt(words[2]);
            left[i] = Integer.parseInt(words[3]);
            for (int j = 0; j < numTeams; j++)
                against[i][j] = Integer.parseInt(words[j + 4]);
            if (wins[i] > topWins) {
                topTeam = i;
                topWins = wins[i];
            }
            i++;
        }
        for (int ii = 0; ii < numTeams; ii++) {
            status[ii] = !(((wins[ii] + left[ii]) >= wins[topTeam]) || i == topTeam);
        }
    } // create a baseball division from given filename in format specified below

    public int numberOfTeams() {
        return numTeams;
    } // number of teams

    public Iterable<String> teams() {
        return Arrays.asList(teams);
    } // all teams

    private int teamRow(String team) {
        for (int i = 0; i < numTeams; i++) {
            if (team.equals(teams[i])) {
                return i;
            }
        }
        throw new IllegalArgumentException();
    }

    public int wins(String team) {
        int x = teamRow(team);
        return wins[x];
    } // number of wins for given team

    public int losses(String team) {
        int x = teamRow(team);
        return losses[x];
    } // number of losses for given team

    public int remaining(String team) {
        int x = teamRow(team);
        return left[x];
    } // number of remaining games for given team

    public int against(String team1, String team2) {
        int x = teamRow(team1);
        int y = teamRow(team2);
        return against[x][y];
    } // number of remaining games between team1 and team2

    public boolean isEliminated(String team) {
        int x = teamRow(team);
        if (status[x]) {
            return true;
        }
        else {
            teamV = numTeams - 1;
            gameV = (teamV * (teamV - 1)) / 2;
            int v = 2 + teamV + gameV;
            FlowNetwork fNetwork = new FlowNetwork(v);
            int n = 1;
            int flow = 0;
            for (int i = 0; i < numTeams; i++) {
                if (x == i)
                    continue;
                for (int j = i + 1; j < numTeams; j++) {
                    if (j == x) {
                        continue;
                    }
                    fNetwork.addEdge(new FlowEdge(0, n, against[i][j]));
                    if (i < x)
                        fNetwork.addEdge(new FlowEdge(n, gameV + 1 + i, Double.POSITIVE_INFINITY));
                    else // i > x
                        fNetwork.addEdge(new FlowEdge(n, gameV + i, Double.POSITIVE_INFINITY));

                    if (j < x)
                        fNetwork.addEdge(new FlowEdge(n, gameV + 1 + j, Double.POSITIVE_INFINITY));
                    else // j > x
                        fNetwork.addEdge(new FlowEdge(n, gameV + j, Double.POSITIVE_INFINITY));

                    flow += against[i][j];
                    n++;
                }
            }
            for (int i = 0; i < numTeams; i++) {
                if (i < x)
                    fNetwork.addEdge(
                            new FlowEdge(i + gameV + 1, v - 1, wins[x] + left[x] - wins[i]));
                else if (i > x)
                    fNetwork.addEdge(new FlowEdge(i + gameV, v - 1, wins[x] + left[x] - wins[i]));
            }
            // System.out.println(fNetwork.toString());
            fFulkerson = new FordFulkerson(fNetwork, 0, v - 1);
            return (fFulkerson.value() < flow);
        }
    } // is given team eliminated?

    public Iterable<String> certificateOfElimination(String team) {
        int x = teamRow(team);
        List<String> cert = new ArrayList<>();
        if (status[x]) {
            cert.add(teams[topTeam]);
            return cert;
        }
        else if (isEliminated(team)) {
            for (int i = 0; i < teamV; i++) {
                if (fFulkerson.inCut(i + gameV + 1)) {
                    int idx = (i < x) ? i : i + 1;
                    cert.add(teams[idx]);
                }
            }
            return cert;
        }
        return null;
    } // subset R of teams that eliminates given team; null if not eliminated

    public static void main(String[] args) {
        BaseballElimination division = new BaseballElimination(args[0]);
        for (String team : division.teams()) {
            if (division.isEliminated(team)) {
                StdOut.print(team + " is eliminated by the subset R = { ");
                for (String t : division.certificateOfElimination(team)) {
                    StdOut.print(t + " ");
                }
                StdOut.println("}");
            }
            else {
                StdOut.println(team + " is not eliminated");
            }
        }
    }
}
