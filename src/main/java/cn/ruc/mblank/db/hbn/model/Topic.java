package cn.ruc.mblank.db.hbn.model;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Created by mblank on 14-4-9.
 */
@Entity
public class Topic {
    private int id;
    private String keyWords;
    private String summary;
    private Date startTime;
    private Date endTime;
    private Integer number;
    private String timeNumber;
    private String main;
    private String object;

    @Id
    @Column(name = "id")
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Basic
    @Column(name = "keyWords")
    public String getKeyWords() {
        return keyWords;
    }

    public void setKeyWords(String keyWords) {
        this.keyWords = keyWords;
    }

    @Basic
    @Column(name = "summary")
    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    @Basic
    @Column(name = "startTime")
    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    @Basic
    @Column(name = "endTime")
    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    @Basic
    @Column(name = "number")
    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    @Basic
    @Column(name = "timeNumber")
    public String getTimeNumber() {
        return timeNumber;
    }

    public void setTimeNumber(String timeNumber) {
        this.timeNumber = timeNumber;
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

        Topic topic = (Topic) o;

        if (id != topic.id) return false;
        if (endTime != null ? !endTime.equals(topic.endTime) : topic.endTime != null) return false;
        if (keyWords != null ? !keyWords.equals(topic.keyWords) : topic.keyWords != null) return false;
        if (main != null ? !main.equals(topic.main) : topic.main != null) return false;
        if (number != null ? !number.equals(topic.number) : topic.number != null) return false;
        if (object != null ? !object.equals(topic.object) : topic.object != null) return false;
        if (startTime != null ? !startTime.equals(topic.startTime) : topic.startTime != null) return false;
        if (summary != null ? !summary.equals(topic.summary) : topic.summary != null) return false;
        if (timeNumber != null ? !timeNumber.equals(topic.timeNumber) : topic.timeNumber != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (keyWords != null ? keyWords.hashCode() : 0);
        result = 31 * result + (summary != null ? summary.hashCode() : 0);
        result = 31 * result + (startTime != null ? startTime.hashCode() : 0);
        result = 31 * result + (endTime != null ? endTime.hashCode() : 0);
        result = 31 * result + (number != null ? number.hashCode() : 0);
        result = 31 * result + (timeNumber != null ? timeNumber.hashCode() : 0);
        result = 31 * result + (main != null ? main.hashCode() : 0);
        result = 31 * result + (object != null ? object.hashCode() : 0);
        return result;
    }
}
