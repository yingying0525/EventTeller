package db.hbn.model;

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
	
	@Override
    public boolean equals(Object anObject) {
        if (this == anObject) {
            return true;
        }
        if (anObject instanceof Word) {
        	if(((Word) anObject).getName().equals(this.name) && 
        			((Word)anObject).getNature().equals(this.getNature())){
        		return true;
        	}
        }
        return false;
    }
	
	@Override
	public int hashCode(){
		return name.hashCode() * 37 + nature.hashCode();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		StringBuffer res = new StringBuffer();
		res.append(this.name + "\t" + this.nature);
		return res.toString();
	}

}
