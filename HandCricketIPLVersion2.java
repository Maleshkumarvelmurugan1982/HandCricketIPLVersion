import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

public class HandCricketIPLVersion2 extends JFrame {
    private JTextArea output;
    private JButton[] numberButtons;
    private JButton nextMatchButton;
    private List<Team> teams;
    private List<Match> fullSchedule;
    private int currentMatchIndex = 0;
    private Match currentMatch;
    private int teamAScore = 0, teamBScore = 0, dockBalls = 0;
    private boolean isBatting, inningsSwitched = false;
    private boolean tossDone = false, teamABatsFirst = false;
    private Random rand = new Random();
    private int matchesPlayed = 0;

    public HandCricketIPLVersion2() {
        setTitle("IPL Hand Cricket League");
        setSize(700, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        output = new JTextArea();
        output.setEditable(false);
        output.setFont(new Font("Monospaced", Font.PLAIN, 14));
        output.setMargin(new Insets(10, 10, 10, 10));
        add(new JScrollPane(output), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new GridLayout(2, 5, 10, 10));
        numberButtons = new JButton[10];
        for (int i = 0; i < 10; i++) {
            int finalI = i;
            numberButtons[i] = new JButton(String.valueOf(i));
            numberButtons[i].setFont(new Font("Arial", Font.BOLD, 18));
            numberButtons[i].addActionListener(e -> handleTurn(finalI));
            buttonPanel.add(numberButtons[i]);
        }
        add(buttonPanel, BorderLayout.SOUTH);

        JPanel topPanel = new JPanel();
        nextMatchButton = new JButton("Start League");
        nextMatchButton.addActionListener(e -> nextMatch());
        topPanel.add(nextMatchButton);
        add(topPanel, BorderLayout.NORTH);

        disableNumberButtons();
        setupLeague();
        fullSchedule = generateSchedule();
        setVisible(true);
    }

    private void setupLeague() {
        teams = new ArrayList<>(java.util.Arrays.asList(
            new Team("MI"), new Team("CSK"), new Team("RCB"), new Team("KKR"),
            new Team("SRH"), new Team("DC"), new Team("PBKS"), new Team("RR"),
            new Team("LSG"), new Team("GT")
        ));
    }

    private void nextMatch() {
        if (currentMatchIndex >= fullSchedule.size()) {
            output.append("\nLeague Over. Showcasing Table:\n");
            displayStandings();
            return;
        }

        currentMatch = fullSchedule.get(currentMatchIndex);
        output.setText("Match: " + currentMatch.teamA.name + " vs " + currentMatch.teamB.name + "\n");

        teamAScore = 0;
        teamBScore = 0;
        dockBalls = 0;
        tossDone = false;
        inningsSwitched = false;

        promptToss();
        currentMatchIndex++;
    }

    private void promptToss() {
        String choice = JOptionPane.showInputDialog(this, "Toss Time! Enter 'odd' or 'even':");
        if (choice == null || (!choice.equalsIgnoreCase("odd") && !choice.equalsIgnoreCase("even"))) {
            promptToss();
            return;
        }

        int teamACall = Integer.parseInt(JOptionPane.showInputDialog(this, "Enter your toss number (0-9):"));
        int teamBCall = rand.nextInt(10);
        int total = teamACall + teamBCall;
        boolean teamAWinsToss = (total % 2 == 0 && choice.equalsIgnoreCase("even")) ||
                               (total % 2 == 1 && choice.equalsIgnoreCase("odd"));

        output.append("Toss: " + currentMatch.teamA.name + " chose " + choice + ". " + currentMatch.teamA.name + ": " + teamACall + " | " + currentMatch.teamB.name + ": " + teamBCall + " | Total: " + total + "\n");

        if (teamAWinsToss) {
            String decision = JOptionPane.showInputDialog(this, currentMatch.teamA.name + " won the toss! Type 'bat' or 'bowl':");
            teamABatsFirst = decision.equalsIgnoreCase("bat");
            currentMatch.teamAWinsToss = true;
        } else {
            teamABatsFirst = rand.nextBoolean();
            output.append(currentMatch.teamB.name + " won the toss and chose to " + (teamABatsFirst ? "bat" : "bowl") + " first.\n");
            currentMatch.teamAWinsToss = false;
        }

        isBatting = teamABatsFirst;
        tossDone = true;
        enableNumberButtons();
        output.append("Starting with the first innings...\n");
    }

    private List<Match> generateSchedule() {
        List<Match> matches = new ArrayList<>();
        for (int i = 0; i < teams.size(); i++) {
            for (int j = i + 1; j < teams.size(); j++) {
                matches.add(new Match(teams.get(i), teams.get(j)));
            }
        }
        return matches;
    }

    private void handleTurn(int userNum) {
        int compNum = rand.nextInt(10);

        if (userNum == 0 || compNum == 0) {
            dockBalls++;
            output.append("DOCK BALL! Both tried zero! Docks: " + dockBalls + "\n");
            if (dockBalls >= 3) {
                output.append("Too many DOCKS! OUT!\n");
                switchInnings();
            }
            return;
        }

        if (isBatting) {
            output.append("User played: " + userNum + " | Comp bowled: " + compNum + "\n");
            if (userNum == compNum) {
                output.append("OUT at " + teamAScore + "\n");
                switchInnings();
            } else {
                teamAScore += userNum;
                output.append(currentMatch.teamA.name + " Score: " + teamAScore + "\n");
            }
        } else {
            output.append("Comp played: " + compNum + " | User bowled: " + userNum + "\n");
            if (userNum == compNum) {
                output.append("OUT at " + teamBScore + "\n");
                endMatch();
            } else {
                teamBScore += compNum;
                output.append(currentMatch.teamB.name + " Score: " + teamBScore + "\n");
            }
        }
    }

    private void switchInnings() {
        isBatting = !isBatting;
        dockBalls = 0;
        if (inningsSwitched) {
            endMatch();
        } else {
            inningsSwitched = true;
            output.append("Switching Innings.\n");
        }
    }

    private void endMatch() {
        disableNumberButtons();
        matchesPlayed++;
        currentMatch.teamA.matchesPlayed++;
        currentMatch.teamB.matchesPlayed++;

        output.append("Match Over. Final Score: " + currentMatch.teamA.name + ": " + teamAScore + " | " + currentMatch.teamB.name + ": " + teamBScore + "\n");

        if (teamAScore > teamBScore) {
            output.append(currentMatch.teamA.name + " wins!\n");
            currentMatch.teamA.wins++;
            currentMatch.teamB.losses++;
            currentMatch.teamA.points += 2;
        } else if (teamBScore > teamAScore) {
            output.append(currentMatch.teamB.name + " wins!\n");
            currentMatch.teamB.wins++;
            currentMatch.teamA.losses++;
            currentMatch.teamB.points += 2;
        } else {
            output.append("It's a tie!\n");
        }

        displayStandings();
    }

    private void displayStandings() {
        teams.sort((a, b) -> b.points - a.points);
        output.append("\nStandings after match " + matchesPlayed + ":\n");
        for (int i = 0; i < teams.size(); i++) {
            Team team = teams.get(i);
            output.append((i + 1) + ". " + team.name + " - Matches: " + team.matchesPlayed + " | Wins: " + team.wins + " | Losses: " + team.losses + " | Points: " + team.points + "\n");
        }

        output.append("\nRemaining Matches: \n");
        for (int i = currentMatchIndex; i < fullSchedule.size(); i++) {
            output.append(fullSchedule.get(i).teamA.name + " vs " + fullSchedule.get(i).teamB.name + "\n");
        }

        JOptionPane.showMessageDialog(this, "Click OK to continue to the next match.");
    }

    private void enableNumberButtons() {
        for (JButton btn : numberButtons) btn.setEnabled(true);
    }

    private void disableNumberButtons() {
        for (JButton btn : numberButtons) btn.setEnabled(false);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(HandCricketIPLVersion2::new);
    }

    class Team {
        String name;
        int points = 0;
        int wins = 0;
        int losses = 0;
        int matchesPlayed = 0;

        Team(String name) {
            this.name = name;
        }
    }

    class Match {
        Team teamA, teamB;
        boolean teamAWinsToss;

        Match(Team a, Team b) {
            this.teamA = a;
            this.teamB = b;
        }
    }
}
