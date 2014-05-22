package cn.ruc.mblank.db.hbn.model;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by mblank on 14-4-9.
 */
@Entity
public class EventSim {
    private int fid;
    private Integer sid;
    private Double score;
    private String main;
    private String object;

    @Id
    @Column(name = "fid")
    public int getFid() {
        return fid;
    }

    public void setFid(int fid) {
        this.fid = fid;
    }

    @Basic
    @Column(name = "sid")
    public Integer getSid() {
        return sid;
    }

    public void setSid(Integer sid) {
        this.sid = sid;
    }

    @Basic
    @Column(name = "score")
    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    @Basic
    @Column(name = "main")
    public String getMain() {
        return main;
    }

    public void setMain(String main) {
        this.main = main;
    }

    @Basic
    @Column(name = "object")
    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EventSim eventSim = (EventSim) o;

        if (fid != eventSim.fid) return false;
        if (main != null ? !main.equals(eventSim.main) : eventSim.main != null) return false;
        if (object != null ? !object.equals(eventSim.object) : eventSim.object != null) return false;
        if (score != null ? !score.equals(eventSim.score) : eventSim.score != null) return false;
        if (sid != null ? !sid.equals(eventSim.sid) : eventSim.sid != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = fid;
        result = 31 * result + (sid != null ? sid.hashCode() : 0);
        result = 31 * result + (score != null ? score.hashCode() : 0);
        result = 31 * result + (main != null ? main.hashCode() : 0);
        result = 31 * result + (object != null ? object.hashCode() : 0);
        return result;
    }
}
