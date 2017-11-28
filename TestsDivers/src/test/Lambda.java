package test;

import java.util.HashMap;

public class Lambda {

	public static class CItem {
		int c;
		boolean b;
		CItem(int c, boolean b) { this.c = c; this.b= b; }
	}
	
	@FunctionalInterface interface OnCItem {
	    void doit(CItem ci);
	}

	int total;
	
	int browse(OnCItem onci) {
		for(CItem ci : sings.values())
			onci.doit(ci);
		for(HashMap<String,CItem> cis : colls.values())
			for(CItem ci : cis.values()) {
				onci.doit(ci);
		}
		return total;
	}
	
	void zarbi() {
		total = 0;
		browse((ci) -> {
			if (ci.b) this.total += ci.c; else this.total += 1;
		});
	}
	
	private HashMap<String,CItem> sings = new HashMap<String,CItem>();
	private HashMap<String,HashMap<String,CItem>> colls = new HashMap<String,HashMap<String,CItem>>();

	public static void main(String[] args) {
		try {
			Lambda l = new Lambda();
			l.colls.put("C1", new HashMap<String,CItem>());
			l.colls.put("C2", new HashMap<String,CItem>());
			l.sings.put("S1", new CItem(3, true));
			l.sings.put("S2", new CItem(8, false));
			l.colls.get("C1").put("k1", new CItem(3, true));
			l.colls.get("C1").put("k2", new CItem(8, true));
			l.colls.get("C2").put("k1", new CItem(9, true));
			
//			OnCItem dbl = (l, ci) -> ci.c * 2;
//			int res = dbl.doit(new CItem(3, true));
//			System.out.println(res);
						
			int res = l.browse((ci) -> {
				if (ci.b) l.total += ci.c; else l.total += 1;
			});
			System.out.println(res);
			
			l.zarbi();
			System.out.println(l.total);
			
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
}
