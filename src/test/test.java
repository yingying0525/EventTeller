package test;

import db.hbn.model.EventStatus;





public class Test {
	
	
	
	

	
	
	
	public static void main(String[] args){
		
		
		
		
		EventStatus es = new EventStatus();
		es.setId(1);
		es.setStatus(2);
		
		util.Util.updateDB(es);
		
	}
}
