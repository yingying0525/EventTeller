package db.hbn.model;

public class TopicInfo {
	
	private int id;
	private int startDay;
	private int endDay;
	private int number;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getStartDay() {
		return startDay;
	}
	public void setStartDay(int startDay) {
		this.startDay = startDay;
	}
	public int getEndDay() {
		return endDay;
	}
	public void setEndDay(int endDay) {
		this.endDay = endDay;
	}
	public int getNumber() {
		return number;
	}
	public void setNumber(int number) {
		this.number = number;
	}
	
	
	@Override
	public boolean equals(Object obj){
		if(obj instanceof TopicInfo){
			return ((TopicInfo)obj).getId() == this.id;
		}
		return false;
	}
	
	@Override
	public int hashCode(){
		return id;
	}

}
