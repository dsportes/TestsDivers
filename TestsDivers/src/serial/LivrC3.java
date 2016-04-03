package serial;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.GZIPOutputStream;

import com.google.gson.Gson;

public class LivrC3 extends Document {
	static {
		Document.register(LivrC3.class, LivrC3.Ac.class, LivrC3.AcApPr.class, LivrC3.Prix.class, LivrC3.Ap.class);
	}

	public int recomp;	
	public ArrayList<Integer> prods;
	public int local;
	public Gac gac;
	
	public static class Gac {
		public int regltFait;
		public int panierAtt;
		public int dbj;
		public int crj;
		public int nbac;
		public int remiseCheque;
		public int cheque;
		public int suppl;
		public int db;
		public int cr;
		public int poidsC;
		public int poidsD;
		public int prixC;
		public int prixD;
		public int poids;
		public int prix;
		public int nblg;
		public int flags;
	}
	
	public static class Ac extends Document.Item {
		public int ac;
		public int poids;
		public int prix;
		public int nblg;
		public int flags;
		public int payePar;
		public int payePour;
		public int cheque;
		public int suppl;
		public int regltFait;
		public int panierAtt;
		public int db;
		public int cr;
		public HashMap<String,AcAp> acAp;
	}
	
	public static class AcAp {
		public int ac;
		public int ap;
		public int poids;
		public int prix;
		public int nblg;
		public int flags;
		public int payePar;
		public int payePour;
		public int cheque;
		public int suppl;
		public int regltFait;
		public int panierAtt;
		public int db;
		public int cr;
		public int ami;
		public ArrayList<Integer> amisPour;
		public ArrayList<Integer> amisPar;
		public int prixPG;
		public String intitule;
	}
	
	public static class AcApPr extends Document.Item {
		public int ac;
		public int ap;
		public int pr;
		public int qbl;
		public int qte;
		public int qteS;
		public int poids;
		public int prix;
		public int nblg;
		public int flags;
		public ArrayList<Integer> lprix;
	}
	
	public static class Prix extends Document.Item {
		public int prod;
		public int dispo;
		public int pu;
		public int poids;
		public int qmax;
		public int parite;
		public long dhChange;
	}

	public static class Ap extends Document.Item {
		public int ap;
		public int prixPG;
		public int regltFait;
		public int panierAtt;
		public String descr;
		public int remiseCheque;
		public int cheque;
		public int suppl;
		public int db;
		public int cr;
		public int poidsC;
		public int poidsD;
		public int prixC;
		public int prixD;
		public int poids;
		public int prix;
		public int nblg;
		public int flags;
		public HashMap<String,ApPr> apPr;
	}
	
	public static class ApPr {
		public int ap;
		public int pr;
		public int qte;
		public int qteS;
		public int qteC;
		public int qteD;
		public int lprixByGac;
		public int charge;
		public int decharge;
		public int poidsC;
		public int poidsD;
		public int prixC;
		public int prixD;
		public int poids;
		public int prix;
		public int nblg;
		public int flags;
		public ArrayList<AcPrix> lprix;
		public ArrayList<Integer> lprixC;
	}

	public static class AcPrix {
		public int ac;
		public int prix;
	}

	public static void main(String[] args) {
		int max = 1;
		try {
			String type = "LivrC";
			String line = "P.1.";
			String column = "408.10.";
			String text = LivrC2.readFile(column, type);
			LivrC2 c = new Gson().fromJson(text, LivrC2.class);
			LivrC3 d = (LivrC3)Document.newDocument("LivrC3", line + "@" + column + "_" + type);
			
			d.recomp = c.Recomp.recomp;
			if (c.ExclC.prods != null && c.ExclC.prods.size() != 0) {
				d.prods = new ArrayList<Integer>();
				d.prods.addAll(c.ExclC.prods);
			}
			
			d.gac = new Gac();
			d.gac.regltFait = c.Gac.regltFait ;
			d.gac.panierAtt = c.Gac.panierAtt ;
			d.gac.dbj = c.Gac.dbj ;
			d.gac.crj = c.Gac.crj ;
			d.gac.nbac = c.Gac.nbac ;
			d.gac.remiseCheque = c.Gac.remiseCheque ;
			d.gac.cheque = c.Gac.cheque ;
			d.gac.suppl = c.Gac.suppl ;
			d.gac.db = c.Gac.db ;
			d.gac.cr = c.Gac.cr ;
			d.gac.poidsC = c.Gac.poidsC ;
			d.gac.poidsD = c.Gac.poidsD ;
			d.gac.prixC = c.Gac.prixC ;
			d.gac.prixD = c.Gac.prixD ;
			d.gac.poids = c.Gac.poids ;
			d.gac.prix = c.Gac.prix ;
			d.gac.nblg = c.Gac.nblg ;
			d.gac.flags = c.Gac.flags ;
			
			for(LivrC2.Ac ac1 : c.Ac){
				Ac ac = (Ac)d.getItem("Ac", "" + ac1.ac);
				ac.ac = ac1.ac;
				ac.poids = ac1.poids;
				ac.prix = ac1.prix;
				ac.nblg = ac1.nblg;
				ac.flags = ac1.flags;
				ac.payePar = ac1.payePar;
				ac.payePour = ac1.payePour;
				ac.cheque = ac1.cheque;
				ac.suppl = ac1.suppl;
				ac.regltFait = ac1.regltFait;
				ac.panierAtt = ac1.panierAtt;
				ac.db = ac1.db;
				ac.cr = ac1.cr;
				for(LivrC2.AcAp acAp1 : c.AcAp) {
					if (acAp1.ac != ac.ac) continue;
					if (ac.acAp == null)
						ac.acAp = new HashMap<String,AcAp>();
					AcAp acAp = new AcAp();
					ac.acAp.put("" + acAp1.ap, acAp);
					acAp.ac = acAp1.ac;
					acAp.ap = acAp1.ap;
					acAp.poids = acAp1.poids;
					acAp.prix = acAp1.prix;
					acAp.nblg = acAp1.nblg;
					acAp.flags = acAp1.flags;
					acAp.payePar = acAp1.payePar;
					acAp.payePour = acAp1.payePour;
					acAp.cheque = acAp1.cheque;
					acAp.suppl = acAp1.suppl;
					acAp.regltFait = acAp1.regltFait;
					acAp.panierAtt = acAp1.panierAtt;
					acAp.db = acAp1.db;
					acAp.cr = acAp1.cr;
					acAp.ami = acAp1.ami;
					acAp.prixPG = acAp1.prixPG;
					acAp.intitule = acAp1.intitule;
					if (acAp1.amisPour != null && acAp1.amisPour.size() != 0){
						acAp.amisPour = new ArrayList<Integer>();
						acAp.amisPour.addAll(acAp1.amisPour);
					}
					if (acAp1.amisPar != null && acAp1.amisPar.size() != 0){
						acAp.amisPar = new ArrayList<Integer>();
						acAp.amisPar.addAll(acAp1.amisPar);
					}					
				}
				ac.save();
			}
			
			for(LivrC2.AcApPr acApPr1 : c.AcApPr) {
				AcApPr acApPr = (AcApPr)d.getItem("AcApPr", acApPr1.ac + "." + acApPr1.ap + "." + acApPr1.pr);
				acApPr.ac = acApPr1.ac;
				acApPr.ap = acApPr1.ap;
				acApPr.pr = acApPr1.pr;
				acApPr.qbl = acApPr1.qbl;
				acApPr.qte = acApPr1.qte;
				acApPr.qteS = acApPr1.qteS;
				acApPr.poids = acApPr1.poids;
				acApPr.prix = acApPr1.prix;
				acApPr.nblg = acApPr1.nblg;
				acApPr.flags = acApPr1.flags;
				if (acApPr1.lprix != null && acApPr1.lprix.size() != 0){
					acApPr.lprix = new ArrayList<Integer>();
					acApPr.lprix.addAll(acApPr1.lprix);
				}
				acApPr.save();
			}

			for(LivrC2.Prix p1 : c.Prix){
				Prix p = (Prix)d.getItem("Prix", "" + p1.prod);
				p.prod = p1.prod ;
				p.dispo = p1.dispo ;
				p.pu = p1.pu ;
				p.poids = p1.poids ;
				p.qmax = p1.qmax ;
				p.parite = p1.parite ;
				p.dhChange = p1.dhChange ;
				p.save();
			}
			
			for(LivrC2.Ap ap1 : c.Ap){
				Ap ap = (Ap)d.getItem("Ap", "" + ap1.ap);
				ap.ap = ap1.ap;
				ap.prixPG = ap1.prixPG;
				ap.regltFait = ap1.regltFait;
				ap.panierAtt = ap1.panierAtt;
				ap.descr = ap1.descr;
				ap.remiseCheque = ap1.remiseCheque;
				ap.cheque = ap1.cheque;
				ap.suppl = ap1.suppl;
				ap.db = ap1.db;
				ap.cr = ap1.cr;
				ap.poidsC = ap1.poidsC;
				ap.poidsD = ap1.poidsD;
				ap.prixC = ap1.prixC;
				ap.prixD = ap1.prixD;
				ap.poids = ap1.poids;
				ap.prix = ap1.prix;
				ap.nblg = ap1.nblg;
				ap.flags = ap1.flags;
				for(LivrC2.ApPr apPr1 : c.ApPr) {
					if (apPr1.ap != ap.ap) continue;
					if (ap.apPr == null)
						ap.apPr = new HashMap<String,ApPr>();
					ApPr apPr = new ApPr();
					ap.apPr.put("" + apPr1.pr, apPr);
					apPr.ap = apPr1.ap;
					apPr.pr = apPr1.pr;
					apPr.qte = apPr1.qte;
					apPr.qteS = apPr1.qteS;
					apPr.qteC = apPr1.qteC;
					apPr.qteD = apPr1.qteD;
					apPr.lprixByGac = apPr1.lprixByGac;
					apPr.charge = apPr1.charge;
					apPr.decharge = apPr1.decharge;
					apPr.poidsC = apPr1.poidsC;
					apPr.poidsD = apPr1.poidsD;
					apPr.prixC = apPr1.prixC;
					apPr.prixD = apPr1.prixD;
					apPr.poids = apPr1.poids;
					apPr.prix = apPr1.prix;
					apPr.nblg = apPr1.nblg;
					apPr.flags = apPr1.flags;
					if (apPr1.lprix != null && apPr1.lprix.size() != 0){
						apPr.lprix = new ArrayList<AcPrix>();
						for(int x : apPr1.lprix){
							AcPrix y = new AcPrix();
							y.ac = x % 1000;
							y.prix = x / 1000;
							apPr.lprix.add(y);
						}
					}
					if (apPr1.lprixC != null && apPr1.lprixC.size() != 0){
						apPr.lprixC = new ArrayList<Integer>();
						for(int x : apPr1.lprixC)
							apPr.lprixC.add(x / 1000);
					}
				}
				ap.save();
			}
			
			long t3 = System.currentTimeMillis();
			int lg = 0;
			byte[] bytes = null;
			byte[] bytes2 = null;
			String jsonAll = "";
			for(int i = 0; i < max; i++) {
				jsonAll = d.serialize(0);
				bytes = jsonAll.getBytes("UTF-8");
				lg = bytes.length;
			}
			long t4 = System.currentTimeMillis();
			System.out.println("Stringify to JSON : " + (t4 - t3) + "ms. lg json2 = " + lg);

			long t5 = System.currentTimeMillis();
			for(int i = 0; i < max; i++) {
				ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
				GZIPOutputStream zos = new GZIPOutputStream(bos2, 128 * 1024);
				zos.write(bytes);
				zos.close();
				bytes2 = bos2.toByteArray();
			}
			long t6 = System.currentTimeMillis();
			System.out.println("GZIP : " + (t6 - t5) + "ms. lg gz = " + bytes2.length);

			LivrC2.writeFile(column, type, 4, bytes2);

			
			Document d2 = null;
			long t7 = System.currentTimeMillis();
			for(int i = 0; i < max; i++) {
				d2 = newDocument("LivrC3", d.id(), jsonAll);
			}
			long t8 = System.currentTimeMillis();
			System.out.println("Parse All : " + (t8 - t7) + "ms.");
			
			
			long t9 = System.currentTimeMillis();
			lg = 0;
			bytes = null;
			bytes2 = null;
			jsonAll = "";
			for(int i = 0; i < max; i++) {
				jsonAll = d2.serialize(0);
				bytes = jsonAll.getBytes("UTF-8");
				lg = bytes.length;
			}
			long t10 = System.currentTimeMillis();
			System.out.println("Re Stringify to JSON : " + (t10 - t9) + "ms. lg json2 = " + lg);

			
			
			Ac x = (Ac)d2.getItem("Ac", "45");
			x.poids = 9999;
			x.save();
			jsonAll = d2.serialize(1);
			System.out.println("Stringify Incr to JSON : lg json2 = " + jsonAll.length());
			
			for(String id : d2.getIds("AcApPr")){
				if (!id.startsWith("45.")) continue;
				AcApPr acApPr = (AcApPr)d2.getItem("AcApPr", id);
				Ac ac = (Ac)d2.getItem("Ac", ""+acApPr.ac);
				AcAp acAp = ac.acAp.get(""+acApPr.ap);
				Ap ap = (Ap)d2.getItem("Ap", ""+acApPr.ap);
				ApPr apPr = ap.apPr.get(""+acApPr.pr);
				int p = acApPr.prix;
				acApPr.prix += p;
				acAp.prix += p;
				apPr.prix += p;
				ac.save();
				ap.save();
				acApPr.save();
			}
			jsonAll = d2.serialize(1);
			System.out.println("Stringify Incr to JSON : lg json3 = " + jsonAll.length());

		} catch (Throwable t){
			t.printStackTrace();
		}
	}
}
