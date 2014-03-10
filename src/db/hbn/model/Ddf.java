package db.hbn.model;

import java.util.Date;

public class Ddf {
	
	private int id;
	private int docnum;
	private int wordnum;
	private String words;
	private String day;
	private Date time;
	
	
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getDocnum() {
		return docnum;
	}
	public void setDocnum(int docnum) {
		this.docnum = docnum;
	}
	public int getWordnum() {
		return wordnum;
	}
	public void setWordnum(int wordnum) {
		this.wordnum = wordnum;
	}
	public String getWords() {
		return words;
	}
	public void setWords(String words) {
		this.words = words;
	}
	public String getDay() {
		return day;
	}
	public void setDay(String day) {
		this.day = day;
	}
	public Date getTime() {
		return time;
	}
	public void setTime(Date time) {
		this.time = time;
	}
	
	

}
