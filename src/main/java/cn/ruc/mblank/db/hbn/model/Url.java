package cn.ruc.mblank.db.hbn.model;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

/**
 * Created by mblank on 14-4-9.
 */
@Entity
public class Url {
    private int id;
    private Date crawltime;
    private String url;
    private String website;

    @Id
    @Column(name = "id")
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Basic
    @Column(name = "crawltime")
    public Date getCrawltime() {
        return crawltime;
    }

    public void setCrawltime(Date crawltime) {
        this.crawltime = crawltime;
    }

    @Basic
    @Column(name = "url")
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Basic
    @Column(name = "website")
    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Url url1 = (Url) o;

        if (id != url1.id) return false;
        if (crawltime != null ? !crawltime.equals(url1.crawltime) : url1.crawltime != null) return false;
        if (url != null ? !url.equals(url1.url) : url1.url != null) return false;
        if (website != null ? !website.equals(url1.website) : url1.website != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (crawltime != null ? crawltime.hashCode() : 0);
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + (website != null ? website.hashCode() : 0);
        return result;
    }
}
