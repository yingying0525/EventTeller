package cn.ruc.mblank.core.infoGenerator.model;

public class Word {
	
	private String name;
	private String nature;
	private int tf;
	private int df;
	private double score;
	
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getTf() {
		return tf;
	}
	public void setTf(int tf) {
		this.tf = tf;
	}
	public int getDf() {
		return df;
	}
	public void setDf(int df) {
		this.df = df;
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
	
	@Override
	public boolean equals(Object obj){
		if(obj instanceof Word){
			return ((Word)obj).getName().equals(this.name);
		}
		return false;
	}
	
	@Override
	public int hashCode(){
		return name.hashCode() * 257 + nature.hashCode();
	}
	
	

}
