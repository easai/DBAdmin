import java.util.ArrayList;


public class RecordSet {

	ArrayList<String> headerList=new ArrayList<String>();
	
	ArrayList<ArrayList<Object>> value=new ArrayList<>();
	
	public Object[] getFirst(){
		Object dbList[]=null;
		
		if (0<value.size()) {
			int nValue=value.get(0).size();
			dbList=new String[nValue];
			ArrayList<Object> array=value.get(0);
			for(int i=0;i<nValue;i++){
				dbList[i]=array.get(i);
			}			
		}
		return dbList;
	}
}
