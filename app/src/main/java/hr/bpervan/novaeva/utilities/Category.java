package hr.bpervan.novaeva.utilities;

public enum Category {
	IZREKE (1),
	PROPOVJEDI (7),
	AKTUALNO (9),
	POZIV (8),
	ODGOVORI (11),
	PJESMARICA (355),
	MULTIMEDIJA (10),
	DUHOVNOST (354),
	EVANDJELJE (4);
	
	private final int categoryIntValue;
	
	Category(int categoryIntValue){
		this.categoryIntValue = categoryIntValue;
	}
	
	private int categoryIntValue(){
		return categoryIntValue;
	}
	
	public int getIntValue(){
		return categoryIntValue();
	}
}
