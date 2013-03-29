package db.data;

public class Word {
	
	private Integer id;
	private String  name;
	private String nature;
	private Integer  tf;
	private double    score;
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Integer getTf() {
		return tf;
	}
	public void setTf(Integer tf) {
		this.tf = tf;
	}
	public double getScore() {
		return score;
	}
	public void setScore(double score) {
		this.score = score;
	}
	public String getNature() {
		return nature;
	}
	public void setNature(String nature) {
		this.nature = nature;
	}
	
	

}
