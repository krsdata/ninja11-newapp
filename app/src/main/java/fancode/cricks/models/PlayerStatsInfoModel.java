package fancode.cricks.models;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.List;

public class PlayerStatsInfoModel implements Serializable {

    private String pid;
    private String name;
    private String role;
    private String rating;
    private String point;
    private String teamName;
    private String selection;
    private String cSelection;
    private String vcSelection;
    private String nationality;
    private List<JSONObject> matchPoints;

    public PlayerStatsInfoModel(String pid, String name, String role, String rating, String point, String teamName,
                                String selection, String cSelection, String vcSelection, String nationality,
                                List<JSONObject> matchPoints) {
        this.pid = pid;
        this.name = name;
        this.role = role;
        this.rating = rating;
        this.point = point;
        this.teamName = teamName;
        this.selection = selection;
        this.cSelection = cSelection;
        this.vcSelection = vcSelection;
        this.nationality = nationality;
        this.matchPoints = matchPoints;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getPoint() {
        return point;
    }

    public void setPoint(String point) {
        this.point = point;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getSelection() {
        return selection;
    }

    public void setSelection(String selection) {
        this.selection = selection;
    }

    public String getcSelection() {
        return cSelection;
    }

    public void setcSelection(String cSelection) {
        this.cSelection = cSelection;
    }

    public String getVcSelection() {
        return vcSelection;
    }

    public void setVcSelection(String vcSelection) {
        this.vcSelection = vcSelection;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public List<JSONObject> getMatchPoints() {
        return matchPoints;
    }

    public void setMatchPoints(List<JSONObject> matchPoints) {
        this.matchPoints = matchPoints;
    }
}