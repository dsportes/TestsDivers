package serial;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.zip.GZIPOutputStream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class LivrC2 {

	public enum Flag {QMAX, FRUSTRATION, PAQUETSAC, PARITE, DISTRIB, EXCESTR, PERTETR, PAQUETSC, PAQUETSD, NONCHARGE};
	public static final int[] p2 = {1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048};
	private static final int[] hex0 = {
		0xfffe, 0xfffd, 0xfffb, 0xfff7,
		0xffef, 0xffdf, 0xffbf, 0xff7f,
		0xfeff, 0xfdff, 0xfbff, 0xf7ff
	};

	public static int setFlag(int flags, Flag f, boolean v){
		int i = f.ordinal();
		if (v)
			return flags | p2[i];
		else
			return flags & hex0[i];
	}

	Gac Gac;
	public static class Gac extends Noyau2 {
		@HT(id = 48) public int regltFait;
		@HT(id = 49) public int panierAtt;
		@HT(id = 52) public int dbj;
		@HT(id = 53) public int crj;
		@HT(id = 54) public int nbac;
		@HT(id = 64) public int remiseCheque;
	}
	
	ArrayList<Ac> Ac;
	public static class Ac extends Noyau {
		@HT(id = 2) public int ac;
		@HT(id = 42) public int payePar;
		@HT(id = 43) public int payePour;
		@HT(id = 40) public int cheque;
		@HT(id = 41) public int suppl;
		@HT(id = 48) public int regltFait;
		@HT(id = 49) public int panierAtt;
		@HT(id = 50) public int db;
		@HT(id = 51) public int cr;
	}

	ArrayList<Ap> Ap;
	public static class Ap extends Noyau2 {
		@HT(id = 3) public int ap;
		@HT(id = 47) public int prixPG;
		@HT(id = 48) public int regltFait;
		@HT(id = 49) public int panierAtt;
		@HT(id = 61) public String descr;
		@HT(id = 64) public int remiseCheque;
	}
	
	public static class Noyau2 extends Noyau1 {
		@HT(id = 40) public int cheque;
		@HT(id = 41) public int suppl;
		@HT(id = 50) public int db;
		@HT(id = 51) public int cr;
	}

	public static class Noyau1 extends Noyau {
		@HT(id = 16) public int poidsC;
		@HT(id = 17) public int poidsD;
		@HT(id = 20) public int prixC;
		@HT(id = 21) public int prixD;
	}

	public static class Noyau extends Base {
		@HT(id = 14) public int poids;
		@HT(id = 18) public int prix;
		@HT(id = 30) public int nblg;
		@HT(id = 31) public int flags;
	}
	
	public static class Base {
		public long version;
	}
	
	Recomp Recomp;
	@HTCN(id = 9, single = 'R') public static class Recomp extends Base  {
		@HT(id = 2) public int recomp;
	}
	
	ArrayList<Prix> Prix;
	@HTCN(id = 1) public static class Prix extends Base {
		@HT(id = 2) public int prod;
		@HT(id = 3) public int dispo;
		@HT(id = 4) public int pu;
		@HT(id = 5) public int poids;
		@HT(id = 8) public int qmax;
		@HT(id = 10) public int parite;
		@HT(id = 11) public long dhChange;
	}

	ExclC ExclC;
	@HTCN(id = 2, single = 'X') public static class ExclC extends Base {
		@HT(id = 2) public ArrayInt prods;
		@HT(id = 3) public int local;
	}

	ArrayList<AcApPr> AcApPr;
	@HTCN(id = 10) public class AcApPr extends Base {
		@HT(id = 2) public int ac;
		@HT(id = 3) public int ap;
		@HT(id = 4) public int pr;
		@HT(id = 9) public int qbl;
		@HT(id = 10) public int qte;
		@HT(id = 11) public int qteS;
		@HT(id = 14) public int poids;
		@HT(id = 18) public int prix;
		@HT(id = 19) public ArrayInt lprix;
		@HT(id = 30) public int nblg;
		@HT(id = 31) public int flags;
	}

	ArrayList<ApPr> ApPr;
	@HTCN(id = 11) public class ApPr extends Noyau1 {
		@HT(id = 3) public int ap;
		@HT(id = 4) public int pr;
		@HT(id = 10) public int qte;
		@HT(id = 11) public int qteS;
		@HT(id = 12) public int qteC;
		@HT(id = 13) public int qteD;
		@HT(id = 24) public int lprixByGac;
		@HT(id = 25) public int charge;
		@HT(id = 26) public int decharge;
		@HT(id = 22) public ArrayInt lprix;
		@HT(id = 23) public ArrayInt lprixC;
	}
	
	ArrayList<AcAp> AcAp;
	@HTCN(id = 12) public class AcAp extends Ac {
		@HT(id = 3) public int ap;
		@HT(id = 5) public int ami;
		@HT(id = 45) public ArrayInt amisPour;
		@HT(id = 46) public ArrayInt amisPar;
		@HT(id = 47) public int prixPG;
		@HT(id = 62) public String intitule;
	}
		
	public static String readFile(String column, String type) throws Exception {
		FileInputStream fis = new FileInputStream(column + "_" + type + ".json");
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] buf = new byte[8192];
		int l = 0;
		while((l = fis.read(buf)) != -1) bos.write(buf,0, l);
		fis.close();
		byte[] bytes = bos.toByteArray();
		String text = new String(bytes, "UTF-8");
		System.out.println("lg json1 = " + bytes.length);
		return text;
	}
	
	public static void writeFile(String column, String type, int idx, byte[] bytes)  throws Exception {
		FileOutputStream fos = new FileOutputStream(column + "_" + type + "_" + idx + ".json.gz");
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		int l = 0;
		byte[] buf = new byte[8192];
		while((l = bis.read(buf)) != -1) fos.write(buf,0, l);
		bis.close();
		fos.close();
	}
	
	public static void main(String[] args) {
		try {
			String type = "LivrC";
			// String line = "P.1.";
			String column = "408.10.";
			String text = readFile(column, type);
	        GsonBuilder builder = new GsonBuilder();
	        Gson gson = builder.create();
	        LivrC2 c = null;
			long t1 = System.currentTimeMillis();
			for(int i = 0; i < 100; i++) {
				c = gson.fromJson(text, LivrC2.class);
			}
			long t2 = System.currentTimeMillis();
			System.out.println("Parse JSON + build Cell : " + (t2 -t1) + "ms.");
	        
			long t3 = System.currentTimeMillis();
			int lg = 0;
			byte[] bytes = null;
			byte[] bytes2 = null;
			for(int i = 0; i < 100; i++) {
				String s = gson.toJson(c);
				bytes = s.getBytes("UTF-8");
				lg = bytes.length;
			}
			long t4 = System.currentTimeMillis();
			System.out.println("Stringify to JSON : " + (t4 - t3) + "ms. lg json2 = " + lg);

			long t5 = System.currentTimeMillis();
			for(int i = 0; i < 100; i++) {
				ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
				GZIPOutputStream zos = new GZIPOutputStream(bos2);
				zos.write(bytes);
				zos.close();
				bytes2 = bos2.toByteArray();
			}
			long t6 = System.currentTimeMillis();
			System.out.println("GZIP : " + (t6 - t5) + "ms. lg gz = " + bytes2.length);

			writeFile(column, type, 3, bytes2);
		
		} catch (Throwable t){
			t.printStackTrace();
		}
	}
	
}
