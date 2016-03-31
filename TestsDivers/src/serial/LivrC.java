package serial;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class LivrC extends Livr {
	public static final CellDescr cellDescr = new CellDescr(LivrC.class);

	@Override public CellDescr cellDescr() {
		return cellDescr;
	}

	public static String[] as(String... args) {
		return args;
	}

	public static void sort(Collection<Integer> src, Collection<Integer> dest) {
		if (dest == null) return;
		if (src == null || src.size() == 0) {
			dest.clear();
			return;
		}
		Integer[] values = src.toArray(new Integer[src.size()]);
		Arrays.sort(values);
		if (dest.equals(values)) return;
		dest.clear();
		for (int i : values)
			dest.add(i);
	}
	
	public static boolean copyIf(ArrayInt dest, List<Integer> src) {
		Integer[] a1 = src.toArray(new Integer[src.size()]);
		Integer[] a2 = dest.toArray(new Integer[dest.size()]);
		Arrays.sort(a1);
		Arrays.sort(a2);
		if (!Arrays.equals(a1, a2)) {
			dest.clear();
			dest.addAll(src);
			return true;
		}
		return false;
	}

	ExclC ex = null;
	
	private Prix getPrix(int ap, int pr) throws Exception {
		if (ex == null) {
			ex = (ExclC) nodeByKey(keyOfExclC());
			if (ex == null) {
				ex = (ExclC) newCellNode("ExclC");
				ex.insert();
			}
		}
		int prod = (ap * 10000) + pr;
		Prix prix = (Prix) nodeByKey(keyOfPrix(prod));
		if (!ex.isLocal() || prix == null || prix.isFake()) {
			// setupPrix();
			prix = (Prix) nodeByKey(keyOfPrix(prod));
		}
		if (prix == null) {
			try {
				prix = (Prix) newCellNode("Prix");
			} catch (Exception e) {}
			prix.prod = prod;
			prix.insert();
		}
		return prix;
	}

	public static String keyOfExclC() {
		return "X";
	}

	public static String keyOfPrix(int prod) {
		return "M." + + (prod / 10000) + "." + (prod % 10000) + ".";
	}

	public static String keyOfAcApPr1(int ac, int ap, int pr) {
		return "A." + ac + "." + ap + "." + pr + ".";
	}

	public static String keyOfAcApPr1p(int ac, int ap) {
		return "A." + ac + "." + ap + ".";
	}

	public static String keyOfAcApPr1ac(int ac) {
		return "A." + ac + ".";
	}

	public static String keyOfAcApPr2(int ac, int ap, int pr) {
		return "B." + ap + "." + pr + "." + ac + ".";
	}

	public static String keyOfAcApPr2p(int ap, int pr) {
		return "B." + ap + "." + pr + ".";
	}

	public static String keyOfApPr1(int ap, int pr) {
		return pr != 0 ? "C." + ap + "." + pr + "." : null;
	}

	public static String keyOfApPr1p(int ap) {
		return "C." + ap + ".";
	}

	public static String keyOfApPr2p() {
		return "C.";
	}

	public static String keyOfAcAp1(int ac, int ap) {
		return "D." + ac + "." + ap + ".";
	}

	public static String keyOfAcAp1p(int ac) {
		return "D." + ac + ".";
	}

	public static String keyOfAcAp2(int ac, int ap) {
		return "E." + ap + "." + ac + ".";
	}

	public static String keyOfAcAp2p(int ap) {
		return "E." + ap + ".";
	}

	public static String keyOfAc1(int ac) {
		return "G." + ac + ".";
	}

	public static String keyOfAc1p() {
		return "G.";
	}

	public static String keyOfAp1(int ap) {
		return "H." + ap + ".";
	}

	public static String keyOfAp1p() {
		return "H.";
	}

	public static String keyOfGac() {
		return "I";
	}

	private int codeLivrx = 0;
	
	public int codeLivr() {
		return codeLivrx;
	}

	@HTCN(id = 9, single = 'R') public class Recomp extends CellNode {
		@Override public String[] keys() { return new String[0];}
		@HT(id = 2) private int recomp;

		public void recomp(int value) {
			if (w(this.recomp, value)) this.recomp = value;
		}

	}
	
	@HTCN(id = 1) public class Prix extends CellNode implements IPrix {
		
		public int poidsDePrix(int prix) {
			if (pu == 0)
				return 0;
			// prix * 1000 / pu
			double temp = ((double) prix * 1000.0 / (double) pu) + 0.5;
			return (int) temp;
		}

		public int prixDePoids(int poids) {
			if (poids == 0)
				return 0;
			// poids * pu / 1000
			double temp = ((double) poids * (double) pu / 1000.0) + 0.5;
			return (int) temp;
		}

		public int poidsDeQte(int qte){
			return dispo == 0 ? 0 : qte * poids;
		}

		public int prixDeQte(int qte){
			return dispo == 0 ? 0 : qte * pu;
		}

		public int qteDePoids(int p){
			if (poids == 0)
				return 1;
			if (p == 0)
				return 0;
			int q = (int) Math.round(((double) p) / ((double) poids));
			return q == 0 ? 1 : q;
		}

		@Override public String[] keys() {
			return as(keyOfPrix(prod));
		}

		@HT(id = 2) public int prod;

		public int prod() {
			return prod;
		}

		public int ap() {
			return prod / 10000;
		}

		public int pr() {
			return prod % 10000;
		}

		public int typePrix() {
			return prod % 10;
		}

		@HT(id = 3) private int dispo;

		public int dispo() {
			return dispo;
		}

		public boolean dispo(int value) {
			if (w(this.dispo, value)) {
				this.dispo = value;
				return true;
			}
			return false;
		}

		boolean isFake() {
			return pu == 0 || poids == 0;
		}
		
		@HT(id = 4) private int pu;

		public int pu() {
			return pu;
		}

		public boolean pu(int value) {
			if (w(this.pu, value)) {
				this.pu = value;
				return true;
			}
			return false;
		}

		@HT(id = 5) private int poids;

		public int poids() {
			return poids;
		}

		public boolean poids(int value) {
			if (w(this.poids, value)) {
				this.poids = value;
				return true;
			}
			return false;
		}

		@HT(id = 8) private int qmax;

		public int qmax() {
			return qmax;
		}

		public boolean qmax(int value) {
			if (w(this.qmax, value)) {
				this.qmax = value;
				return true;
			}
			return false;
		}

		@HT(id = 10) private int parite;

		public int parite() {
			return parite;
		}

		public boolean parite(int value) {
			if (w(this.parite, value)) {
				this.parite = value;
				return true;
			}
			return false;
		}

		@HT(id = 11) private long dhChange;

		public long dhChange() {
			return dhChange;
		}

		public boolean dhChange(long value) {
			if (w(this.dhChange, value)) {
				this.dhChange = value;
				return true;
			}
			return false;
		}

	}

	@HTCN(id = 2, single = 'X') public class ExclC extends CellNode {
		@Override public String[] keys() { return new String[0];}

		@HT(id = 2) private ArrayInt prods;
		
		public ArrayInt ArrayInt() {
			return prods;
		}

		@HT(id = 3) private int local;

		public boolean isLocal(){
			return this.local == 1;
		}
		
		public void setLocal() {
			if (w(this.local, 1)) this.local = 1;
		}

	}

	@HTCN(id = 10) public class AcApPr extends CellNode {

		@Override public String[] keys() {
			return as(keyOfAcApPr1(ac, ap, pr), keyOfAcApPr2(ac, ap, pr));
		};

		@HT(id = 2) public int ac;

		@HT(id = 3) public int ap;

		public boolean is1() {
			return ap == 1;
		}

		public boolean isPaiementDirect() {
			return ap == 1 || ap >= 100;
		}

		public boolean isPaiementGAP() {
			return ap > 1 && ap < 100;
		}

		@HT(id = 4) public int pr;

		public int pr() {
			return pr;
		}

		public int prod(){
			return ap * 10000 + pr;
		}
		
		public int typePrix() {
			return pr % 10;
		}

		@HT(id = 9) public int qbl;

		public boolean qbl() {
			return qbl == 2;
		}

		public void qbl(boolean value) {
			if (w(this.qbl, value ? 2 : 0)) this.qbl = value ? 2 : 0;
		}

		@HT(id = 10) public int qte;

		public int qte() {
			return qte;
		}

		public void qte(int value) {
			if (w(this.qte, value)) this.qte = value;
			if (typePrix() == 2 && value == 0 && !lprix.isEmpty()) lprix.clear();
		}

		@HT(id = 11) public int qteS;

		public int qteS() {
			return qteS;
		}

		public void qteS(int value) {
			if (w(this.qteS, value)) this.qteS = value;
			setNblg();
		}

		@HT(id = 14) public int poids;

		public int poids() {
			return poids;
		}

		public void poids(int value) {
			if (w(this.poids, value)) this.poids = value;
		}

		@HT(id = 18) public int prix;

		public int prix() {
			return prix;
		}

		public void prix(int value) {
			if (w(this.prix, value)) this.prix = value;
		}

		@HT(id = 19) private ArrayInt lprix;

		public ArrayInt lprix() {
			return lprix;
		}

		@HT(id = 30) public int nblg;

		public int nblg() {
			return nblg;
		}

		public void setNblg() {
			int value = qteS == 0 && qte == 0 ? 0 : 1;
			if (w(this.nblg, value)) this.nblg = value;
		}

		@HT(id = 31) public int flags;

		public int flags() {
			return flags;
		}

		public void setFlags(Flag f, boolean value) {
			int nv = setFlag(flags, f, value);
			if (w(this.flags, nv)) this.flags = nv;
		}

	}

	@HTCN(id = 11) public class ApPr extends Noyau1 {

		private Prix locPrix = null;

		private Prix locPrix() throws Exception {
			if (locPrix == null) ;
			locPrix = getPrix(ap, pr);
			return locPrix;
		}

		@Override public String[] keys() {
			return as(keyOfApPr1(ap, pr));
		};

		@HT(id = 3) public int ap;

		public boolean is1() {
			return ap == 1;
		}

		public boolean isPaiementDirect() {
			return ap == 1 || ap >= 100;
		}

		public boolean isPaiementGAP() {
			return ap > 1 && ap < 100;
		}

		@HT(id = 4) public int pr;

		public int pr() {
			return pr;
		}

		public int typePrix() {
			return pr % 10;
		}

		public int prod(){
			return (ap * 10000) + pr;
		}
		
		@HT(id = 10) public int qte;

		public int qte() {
			return qte;
		}

		public void qte(int value) {
			if (w(this.qte, value)) this.qte = value;
		}

		@HT(id = 11) public int qteS;

		public int qteS() {
			return qteS;
		}

		public void qteS(int value) {
			if (w(this.qteS, value)) this.qteS = value;
		}

		@HT(id = 12) public int qteC;

		public int qteC() {
			return qteC == -1 ? 0 : qteC;
		}
		
		public void qteC(int value) {
			if (w(this.qteC, value)) this.qteC = value;
		}

		@HT(id = 13) public int qteD;

		public int qteD() {
			return qteD == -1 ? 0 : qteD;
		}

		public boolean nonCharge(){
			return qte != 0 && qteC == 0 && qteD == 0 && lprixC.size() == 0 && lprix.size() == 0;
		}
		
		public int qteT() {
			if (qteD != 0) return qteD == -1 ? 0 : qteD;
			if (qteC != 0) return qteC == -1 ? 0 : qteC;
			return qte;
		}

		public void qteD(int value) {
			if (w(this.qteD, value)) this.qteD = value;
		}

		public int poidsT(){
			if (poidsD != 0)
				return poidsD > 0 ? poidsD : -poidsD;
			if (poidsC != 0)
				return poidsC > 0 ? poidsC : -poidsC;
			return poids > 0 ? poids : -poids;
		}
		
		@HT(id = 24) public int lprixByGac;

		public int lprixByGac() {
			return lprixByGac;
		}

		public void lprixByGac(int value) {
			if (w(this.lprixByGac, value)) this.lprixByGac = value;
		}

		@HT(id = 25) public int charge;

		public int charge() {
			return charge;
		}

		public void charge(int value) {
			if (w(this.charge, value)) this.charge = value;
		}

		@HT(id = 26) public int decharge;

		public int decharge() {
			return decharge;
		}

		public void decharge(int value) {
			if (w(this.decharge, value)) this.decharge = value;
		}

		@HT(id = 22) private ArrayInt lprix;

		public ArrayInt lprix() {
			return lprix;
		}

		public void lprix(Collection<Integer> value) {
			sort(value, lprix);
		}

		public int prixExact() {
			int pr = 0;
			for (int p : lprix)
				pr = pr + (p / 1000);
			return pr;
		}

		public int poidsExact() throws Exception{
			Prix y = locPrix();
			int po = 0;
			for (int p : lprix)
				po += y.poidsDePrix(p / 1000);
			return po;
		}
		
		public void copyLprixCLprix(){
			lprix.clear();
			for (int i : lprixC)
				lprix.add(i);
		}
		
		@HT(id = 23) private ArrayInt lprixC;

		public void setLprixC() {
			if (copyIf(lprixC, lprix)) w();
		}

		public int prixCExact() {
			int pr = 0;
			for (int p : lprixC)
				pr = pr + (p / 1000);
			return pr;
		}

		public int poidsCExact() throws Exception{
			Prix y = locPrix();
			int po = 0;
			for (int p : lprixC)
				po += y.poidsDePrix(p / 1000);
			return po;
		}

		public int prixDExact() {
			int pr = 0;
			for (int p : lprix)
				pr = pr + (p / 1000);
			return pr;
		}

		public int poidsDExact() throws Exception{
			Prix y = locPrix();
			int po = 0;
			for (int p : lprix)
				po += y.poidsDePrix(p / 1000);
			return po;
		}
		
	}
	
	@HTCN(id = 12) public class AcAp extends Livr.Ac {

		@Override public String[] keys() {
			return as(keyOfAcAp1(ac, ap), keyOfAcAp2(ac, ap));
		};

		@HT(id = 3) public int ap;

		public boolean is1() {
			return ap == 1;
		}

		public boolean isPaiementDirect() {
			return ap == 1 || ap >= 100;
		}

		public boolean isPaiementGAP() {
			return ap > 1 && ap < 100;
		}

		@HT(id = 5) public int ami;

		@HT(id = 45) private ArrayInt amisPour;

		public ArrayInt amisPour() {
			return amisPour;
		}

		@HT(id = 46) private ArrayInt amisPar;

		@HT(id = 47) public int prixPG;

		public int prixPG() {
			return prixPG;
		}

		public void prixPG(int value) {
			if (w(this.prixPG, value)) this.prixPG = value;
		}

		@HT(id = 61) public String descr;

		public String descr() {
			return descr;
		}

		public void descr(String value) {
			if (w(this.descr, value)) this.descr = value;
		}

		@HT(id = 62) public String intitule;

		public String intitule() {
			return intitule;
		}

		public void intitule(String value) {
			if (w(this.intitule, value)) this.intitule = value;
		}

	}

	@HTCN(id = 13) public class Ac extends Livr.Ac {
		@Override public String[] keys() {
			return as(keyOfAc1(ac));
		};

	}

	@HTCN(id = 14) public class Ap extends Livr.Ap {
		@Override public String[] keys() {
			return as(keyOfAp1(ap));
		};

	}

	@HTCN(id = 15, single = 'I') public class Gac extends Livr.Gac {
		@Override public String[] keys() { return new String[0];}

	}
	
	
	private static JSONObject parse(String text){
		try{
			JSONParser parser = new JSONParser();
			return (JSONObject)parser.parse(text);
		}
		catch (ParseException e) {
			return new JSONObject();
		}
	}

	
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		try {
			String type = "LivrC";
			String line = "P.1.";
			String column = "408.10.";
			FileInputStream fis = new FileInputStream(column + "_" + type + ".json");
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			byte[] buf = new byte[8192];
			int l = 0;
			while((l = fis.read(buf)) != -1) bos.write(buf,0, l);
			fis.close();
			byte[] bytes = bos.toByteArray();
			String text = new String(bytes, "UTF-8");
			System.out.println("lg json1 = " + bytes.length);
			
			Serial serial = new Serial();
			Cell c = null;
			long t1 = System.currentTimeMillis();
			for(int i = 0; i < 100; i++) {
				JSONObject json = parse(text);
				cellDescr.serial(json, serial);			
				c = (Cell) cellDescr.newCell();
				c.init(line, column, serial.version, serial.bytes);
			}
			long t2 = System.currentTimeMillis();
			System.out.println("Parse JSON + build Cell : " + (t2 -t1) + "ms. lg serial1 = " + serial.bytes.length);
			System.out.println("version = " + c.version());
			
			StringBuffer sbx = new StringBuffer();
			String[] filterKeys = { "**" };
			
			long t3 = System.currentTimeMillis();
			for(int i = 0; i < 100; i++) {
				sbx.setLength(0);
				cellDescr.toJSON(sbx, c, filterKeys, -0, null, true);
				bytes = sbx.toString().getBytes("UTF-8");
			}
			long t4 = System.currentTimeMillis();
			System.out.println("Stringify to JSON : " + (t4 - t3) + "ms. lg json2 = " + bytes.length);

			FileOutputStream fos = new FileOutputStream(column + "_" + type + "_2.json");
			ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
			l = 0;
			while((l = bis.read(buf)) != -1) fos.write(buf,0, l);
			bis.close();
			fos.close();

			long t5 = System.currentTimeMillis();
			for(int i = 0; i < 100; i++) {
				bytes = cellDescr.serialize(c);
			}
			long t6 = System.currentTimeMillis();
			System.out.println("Serial binaire : " + (t6 - t5) + "ms. lg serial2 = " + bytes.length);
			
			Cell c2 = null;
			long t7 = System.currentTimeMillis();
			for(int i = 0; i < 100; i++) {
				c2 = (Cell) cellDescr.newCell();
				c2.init(line, column, 0, bytes);
			}
			long t8 = System.currentTimeMillis();
			System.out.println("Deserialize depuis binaire : " + (t8 - t7) + "ms");
			System.out.println("version = " + c2.version());
		
		} catch (Throwable t){
			t.printStackTrace();
		}
	}
}
