package cn.ruc.mblank.db.hbn.model;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * Created by mblank on 14-4-9.
 */
public class EventTopicRelationPK implements Serializable {
    private int eid;
    private int tid;

    @Column(name = "eid")
    @Id
    public int getEid() {
        return eid;
    }

    public void setEid(int eid) {
        this.eid = eid;
    }

    @Column(name = "tid")
    @Id
    public int getTid() {
        return tid;
    }

    public void setTid(int tid) {
        this.tid = tid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EventTopicRelationPK that = (EventTopicRelationPK) o;

        if (eid != that.eid) return false;
        if (tid != that.tid) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = eid;
        result = 31 * result + tid;
        return result;
    }
}
