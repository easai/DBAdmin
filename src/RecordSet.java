import java.util.ArrayList;


public class RecordSet {

	ArrayList<String> headerList=new ArrayList<String>();
	
	ArrayList<ArrayList<String>> value=new ArrayList<>();
	
	public String[] getFirst(){
		String dbList[]=null;
		
		if (0<value.size()) {
			int nValue=value.get(0).size();
			dbList=new String[nValue];
			ArrayList<String> array=value.get(0);
			for(int i=0;i<nValue;i++){
				dbList[i]=array.get(i);
			}			
		}
		return dbList;
	}
}
