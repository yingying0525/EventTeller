package cn.ruc.mblank.db.hbn.model;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by mblank on 14-4-9.
 */
@Entity
public class TopicInfo {
    private int id;
    private Integer startDay;
    private Integer endDay;
    private Integer number;
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
    @Column(name = "startDay")
    public Integer getStartDay() {
        return startDay;
    }

    public void setStartDay(Integer startDay) {
        this.startDay = startDay;
    }

    @Basic
    @Column(name = "endDay")
    public Integer getEndDay() {
        return endDay;
    }

    public void setEndDay(Integer endDay) {
        this.endDay = endDay;
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

        TopicInfo topicInfo = (TopicInfo) o;

        if (id != topicInfo.id) return false;
        if (endDay != null ? !endDay.equals(topicInfo.endDay) : topicInfo.endDay != null) return false;
        if (main != null ? !main.equals(topicInfo.main) : topicInfo.main != null) return false;
        if (number != null ? !number.equals(topicInfo.number) : topicInfo.number != null) return false;
        if (object != null ? !object.equals(topicInfo.object) : topicInfo.object != null) return false;
        if (startDay != null ? !startDay.equals(topicInfo.startDay) : topicInfo.startDay != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (startDay != null ? startDay.hashCode() : 0);
        result = 31 * result + (endDay != null ? endDay.hashCode() : 0);
        result = 31 * result + (number != null ? number.hashCode() : 0);
        result = 31 * result + (main != null ? main.hashCode() : 0);
        result = 31 * result + (object != null ? object.hashCode() : 0);
        return result;
    }
}
