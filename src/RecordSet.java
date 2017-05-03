import java.util.ArrayList;


public class RecordSet {

	ArrayList<String> headerList=new ArrayList<String>();
	
	ArrayList<ArrayList<Object>> value=new ArrayList<>();
	
	public Object[] getFirst(){
		Object objList[]=null;
		
		if (0<value.size()) {
			int nValue=value.get(0).size();
			objList=new Object[nValue];
			ArrayList<Object> array=value.get(0);
			for(int i=0;i<nValue;i++){
				objList[i]=array.get(i);
			}			
		}
		return objList;
	}
}
