/**
 * Created by sudheera on 2/15/15.
 */
public class MatchResults {
    private int teamAWins=0;
    private int teamBWins=0;

    public void UpTeamAWins() {
        teamAWins++;
    }

    public void UpTeamBWins() {
        teamBWins++;
    }

    public int getTeamAWins() {
        return teamAWins;
    }

    public int getTeamBWins() {
        return teamBWins;
    }
}
