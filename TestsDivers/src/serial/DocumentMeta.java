package serial;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import com.google.gson.Gson;

public class DocumentMeta {
	public static final Logger log = Logger.getAnonymousLogger();
	
	public static final int MAXINCRMODEINHOURS = 24;
	
	public enum Status {NODATA, UNCHANGED, MODIFIED, EMPTY, CREATED, DELETED}
	
	@SuppressWarnings("serial")
	public static class DMException extends Exception {
		public DMException(String msg){
			super(msg);
			log.severe(msg);
		}
		public DMException(String msg, Exception e){
			super(msg, e);
			log.severe(msg + " [" + e.getMessage() + "]");
		}
	}

	static void register(Class<?>... documentClasses) throws DMException {
		if (documentClasses == null) return;
		for (Class<?> documentClass : documentClasses) {
			if (documentClass == null) return;
			String cn = documentClass.getSimpleName();
			if (!Document.class.isAssignableFrom(documentClass)) {
				throw new DMException("Le Document " + cn + "] doit étendre " + Document.class.getCanonicalName());
			}
			for(DocumentDescriptor dx : DocumentDescriptor.ddList)
				if (dx.documentClass == documentClass) continue;
			DocumentDescriptor dd = new DocumentDescriptor();
			dd.documentClassName = cn;
			dd.documentClass = documentClass;
			dd.json = dd.toJson(dd.newDocument(), new StringBuffer()).toString();
			DocumentDescriptor.ddList.add(dd);
	
			Class<?>[] innerClasses = dd.documentClass.getDeclaredClasses();
			for(int i = 0; i < innerClasses.length; i++) {
				Class<?> c = innerClasses[i];
				if (Document.Item.class.isAssignableFrom(c)) {
					DocumentDescriptor.ItemDescriptor id = dd.new ItemDescriptor();
					id.index = dd.idList.size();
					id.itemClass = c;
					id.itemClassName = c.getSimpleName();
					id.json = id.toJson(id.newItem(), new StringBuffer()).toString();
					dd.idList.add(id);
				}
			}
		}
	}
	
	@SuppressWarnings("serial")
	private static class ItemMap extends HashMap<String,ItemMeta> {}
	
	public static Class<?> forName(String name) throws DMException {
		return DocumentDescriptor.documentDescriptor(name).documentClass;
	}

	private static Gson gson = new Gson();
	
	private static String unescape(String s){
		try {
			synchronized (gson) {
				return (String)gson.fromJson("\"" + s + "\"", String.class);
			}
		} catch (Exception e) { 
			return s;
		}
		
	}

	private static String escape(String s) {
		synchronized (gson) {
			String x = gson.toJson(s);
			return x.length() < 2 ? "" : x.substring(1, x.length() - 1);
		}
	}

	private static DocumentMeta newMeta(String json) throws DMException {
		try {
			synchronized (gson) {
				return (DocumentMeta)gson.fromJson(json, DocumentMeta.class);
			}
		} catch (Exception e) { 
			throw new DMException("Erreur de syntaxe JSON - Document: _Meta [" + e.getMessage() + "]", e);
		}
	}
	
	private static class DocumentDescriptor {
		private static ArrayList<DocumentDescriptor> ddList = new ArrayList<DocumentDescriptor>();
		
		private static DocumentDescriptor documentDescriptor(Class<?> clazz) throws DMException{
			for(DocumentDescriptor dd : ddList)
				if (dd.documentClass == clazz) return dd;
			throw new DMException("La classe " + clazz.getCanonicalName() + " n'est pas enregistrée comme Document");
		}

		static DocumentDescriptor documentDescriptor(String className) throws DMException{
			for(DocumentDescriptor dd : ddList)
				if (dd.documentClassName.equals(className)) return dd;
			throw new DMException("La classe " + className + " n'est pas enregistrée comme Document");
		}

		private class ItemDescriptor {
			private String itemClassName;
			private Class<?> itemClass;
			private int index;
			private String json;
			
			private boolean isEmpty(String json){
				return this.json.equals(json);
			}
			
			private Document.Item newItem() throws DMException {
				try { 
					return (Document.Item)itemClass.newInstance(); 
				} catch (Exception e) { 
					throw new DMException("Instantiation impossible - Document/Item:" 
							+ documentClassName + "/" + itemClassName + "[" + e.getMessage() + "]", e);
				}
			}

			private Document.Item newItem(String json) throws DMException {
				try {
					synchronized (gson) {
						return (Document.Item)gson.fromJson(json, itemClass);
					}
				} catch (Exception e) { 
					throw new DMException("Erreur de syntaxe JSON - Document/Item:" 
							+ documentClassName + "/" + itemClassName  + "[" + e.getMessage() + "]", e);
				}
			}

			private StringBuffer toJson(Document.Item item, StringBuffer sb){
				synchronized (gson) {
					return sb.append(gson.toJson(item));
				}
			}

		}
		
		private ItemDescriptor itemDescriptor(String className) {
			for(ItemDescriptor itd : idList){
				if (itd.itemClassName.equals(className))
					return itd;
			}
			return null;
		}

		private ItemDescriptor itemDescriptor(Class<?> clazz) {
			for(ItemDescriptor itd : idList){
				if (itd.itemClass == (clazz))
					return itd;
			}
			return null;
		}

		private Document newDocument() throws DMException {
			try { 
				return (Document)documentClass.newInstance(); 
			} catch (Exception e) { 
				throw new DMException("Instantiation impossible - Document:" 
						+ documentClassName + "[" + e.getMessage() + "]", e);
			}
		}

		private Document newDocument(String json) throws DMException {
			try {
				synchronized (gson) {
					return (Document)gson.fromJson(json, documentClass);
				}
			} catch (Exception e) { 
				throw new DMException("Erreur de syntaxe JSON - Document:" 
						+ documentClassName + "[" + e.getMessage() + "]", e);
			}
		}

		private StringBuffer toJson(Document document, StringBuffer sb){
			synchronized (gson) {
				return sb.append(gson.toJson(document));
			}
		}
		
		private String documentClassName;
		private Class<?> documentClass;
		private ArrayList<ItemDescriptor> idList = new ArrayList<ItemDescriptor>();
		private Gson gson = new Gson();
		private String json;
		
	}

	static Document newDocument(Class<?> clazz, String id) throws DMException {
		if (clazz == null || id == null || id.length() == 0)
			throw new DMException("newDocument(), paramètres class ou id absents ou vides");
		DocumentDescriptor dd = DocumentDescriptor.documentDescriptor(clazz);
		DocumentMeta meta = new DocumentMeta();
		meta.documentDescriptor = dd;
		meta.items = new ItemMap[dd.idList.size()];
		meta.document = dd.newDocument();
		meta.document._meta = meta;
		meta._class = dd.documentClassName;
		meta._id = id;
		meta._v = 0;
		return meta.document;
	}

	static Document newDocument(String jsonAll, boolean full) throws DMException {
		if (jsonAll == null || jsonAll.length() == 0) 
			throw new DMException("newDocument(), paramètre jsonAll absent ou vide");
		
		DocumentMeta meta = null;
		DocumentDescriptor dd = null;
		boolean hasDocument = false;
		long now = System.currentTimeMillis();
		
		int i = 2;
		while (true){
			int j = jsonAll.indexOf('\n', i);
			if (j == -1) break;
			int k = jsonAll.indexOf('\"', i);
			String key = unescape(jsonAll.substring(i, k));
			int a = key.indexOf('@');
			String type = a == -1 ? key : key.substring(0, a);
			String idx = a == -1 || a == key.length() - 1 ? null : key.substring(a + 1);
			
			if ("_Meta".equals(type)) {
				DocumentMeta _meta = newMeta(jsonAll.substring(k + 2, j));
				dd = DocumentDescriptor.documentDescriptor(_meta._class);
				meta = new DocumentMeta();
				meta.documentDescriptor = dd;
				meta.items = new ItemMap[dd.idList.size()];
				meta._id = _meta._id;
				meta._v = _meta._v;
				meta._stamp = _meta._stamp;
				meta._class = dd.documentClassName;
			} else {
				if (meta == null)
					throw new DMException("newDocument() : json ne contient pas "
							+ "d'identification \"_Meta\":{...} en tête");
				int virg = jsonAll.lastIndexOf(',', j);
				long v = Long.parseLong(jsonAll.substring(virg + 6, j - 1));
				String json = jsonAll.substring(k + 2, virg) + "}";
				if (dd.documentClassName.equals(type)) {
					hasDocument = true;
					meta.document = dd.newDocument(json);
					meta.document._meta = meta;
					meta.version = v;
					meta.json = json;
				} else {
					if (idx == null) continue;
					if (v < 0 && now + v > MAXINCRMODEINHOURS * 3600000)
						continue;
					ItemMeta itemMeta = new ItemMeta();
					itemMeta.itemDescriptor = dd.itemDescriptor(type);
					if (itemMeta.itemDescriptor == null) continue;
					itemMeta.item = null;
					itemMeta.document = meta;
					itemMeta.status = v >= 0 ? Status.EMPTY : Status.DELETED;
					itemMeta.version = v;
					itemMeta.json = json;
					itemMeta.item = full ? itemMeta.itemDescriptor.newItem(itemMeta.json) : null;
					itemMeta.id = idx;
					int ix = itemMeta.itemDescriptor.index;
					ItemMap im = itemMeta.document.items[ix];
					if (im == null) {
						itemMeta.document.items[ix] = new ItemMap();
						im = itemMeta.document.items[ix];
					}
					im.put(itemMeta.id, itemMeta);
				}
			}
			i = j + 3;
		}
		
		if (!hasDocument) {
			meta.document = dd.newDocument();
			meta.document._meta = meta;
			meta.json = dd.json;
		}
		return meta.document;
	}

	private String _class;
	private String _id;
	private long _v;
	private long _stamp;
	private int _n;
	private int _sz0;
	private int _sz9;

	private transient DocumentDescriptor documentDescriptor;
	private transient long version;
	private transient String jsonAll;
	private transient String json;
	private transient String json2;
	private transient ItemMap[] items;
	private transient boolean hasChanged = false;
	private transient Document document;

	long stamp() { return _stamp; }
	long version() { return _v; }
	String id() { return _id; }
	boolean hasChanged(){ return hasChanged; }
	
	Set<String> getIds(Class<?> clazz) {
		DocumentDescriptor.ItemDescriptor itd = documentDescriptor.itemDescriptor(clazz);
		if (itd != null) {
			ItemMap im = items[itd.index];
			if (im != null) 
				return im.keySet();
		}
		return new HashSet<String>();
	}
	
	Document.Item getItem(Class<?> clazz, String id) throws Exception {
		if (id == null || id.length() == 0) return null;
		DocumentDescriptor.ItemDescriptor itd = documentDescriptor.itemDescriptor(clazz);
		if (itd == null) return null;
		ItemMap im = items[itd.index];
		if (im == null){
			im = new ItemMap();
			items[itd.index] = im;
		}
		ItemMeta itemMeta = im.get(id);
		Document.Item item = null;
		if (itemMeta == null){
			itemMeta = new ItemMeta();
			itemMeta.document = this;
			itemMeta.itemDescriptor = itd;
			itemMeta.id = id;
			im.put(id, itemMeta);
		} else if (itemMeta.status == Status.DELETED)
			return null;
		if (itemMeta.json == null) {
			item = itd.newItem();
			itemMeta.status = Status.EMPTY;
		} else {
			item = itd.newItem(itemMeta.json);
			itemMeta.status = Status.UNCHANGED;
		}
		item._meta = itemMeta;
		itemMeta.item = item;
		return item;
	}

	/*
	 * trStamp : stamp de la transaction et nouvelle version du document s'il a changé
	 * full : force une sérialisation complète
	 */
	String serializeForDB(long trStamp, boolean full){
		return serialize(full || _sz0 == 0 ? 0 : _v, trStamp, true);
	}
	
	/*
	 * _stamp : stamp de la dernière vérification de non évolution (ou d'évolution)
	 * vmin : stamp de la dernière synchronisation
	 */
	String serializeForSync(long vmin){
		return serialize(vmin, _stamp, false);
	}
	/*
	 * 
	 */
	private String serialize(long vmin, long trStamp, boolean forDB){
		hasChanged = false;
		StringBuffer sb = new StringBuffer();
		if (forDB) {
			String json2 = documentDescriptor.toJson(document, new StringBuffer()).toString();
			hasChanged = !json2.equals(json);
			if (hasChanged) {
				version = trStamp;
				json = json2;
			}
		}
		if (forDB || vmin < version) {
			sb.append("\"").append(documentDescriptor.documentClassName).append("\":").append(json);
			sb.setLength(sb.length() - 1);
			sb.append(",\"_v\":").append(version).append("}\n");
		}
		boolean singleTurn = vmin == 0 || !forDB ;
		boolean turn0 = true;
		while (true) {
			for(int i = 0; i < documentDescriptor.idList.size(); i++){
				DocumentDescriptor.ItemDescriptor itd = documentDescriptor.idList.get(i);
				ItemMap im = items[i];
				if (im != null && im.size() != 0) {
					for(String id : im.keySet()){
						ItemMeta itemMeta = im.get(id);
						boolean mod = forDB && (itemMeta.status == Status.CREATED || itemMeta.status == Status.MODIFIED);
						if (mod) {
							itemMeta.version = trStamp;
							hasChanged = true;
						}
						if (singleTurn || (turn0 && vmin < itemMeta.version)) {
							sb.append(",\"")
							.append(escape(itd.itemClassName + "@" + itemMeta.id))
							.append("\":");
							if (itemMeta.status == Status.DELETED)
								sb.append("{\"_v\":").append(-itemMeta.version);
							else {
								sb.append(mod ? itemMeta.json2 : itemMeta.json);
								sb.setLength(sb.length() - 1);
								sb.append(",\"_v\":").append(itemMeta.version);
							}
							sb.append("}\n");
						}
					}
				}
			}
			if (singleTurn || !turn0 || (_sz9 + sb.length() < _sz0)) break;
			turn0 = false;
		}
		sb.append("}");
		if (hasChanged)
			_v = trStamp;
		StringBuffer sb2 = new StringBuffer();
		sb2.append("{\"_Meta\":{");
		sb2.append("\"_class\":\"").append(_class)
		.append("\",\"_id\":\"").append(escape(_id))
		.append("\",\"_v\":").append(_v);
		if (forDB) {
			if (turn0) {
				_n = 0;
				_sz0 = sb.length();
				_sz9 = 0;
			} else {
				_n++;
				_sz9 += sb.length();
			}
			sb2.append(",\"_n\":").append(_n).append(",\"_sz0\":").append(_sz0).append(",\"_sz9\":").append(_sz9);
		} else
			sb2.append(",\"_stamp\":").append(_stamp);
		sb2.append("}\n,");
		sb.insert(0, sb2.toString());
		return sb.toString();
	}
	
	void commit(){
		if (version == _v)
			json = json2;
		json2 = null;
		document = null;
		for(int i = 0; i < documentDescriptor.idList.size(); i++){
			ItemMap im = items[i];
			if (im == null || im.size() == 0){
				items[i] = null;
			} else {
				for(String id : im.keySet()){
					ItemMeta itemMeta = im.get(id);
					if (itemMeta.version == _v)
						itemMeta.json = itemMeta.json2;
					itemMeta.json2 = null;
					itemMeta.item = null;;
				}
			}
		}
	}
	
	DocumentMeta duplicate(){
		DocumentMeta c = new DocumentMeta();
		c.documentDescriptor = documentDescriptor;
		c._id = _id;
		c._v = _v;
		c._class = _class;
		c.jsonAll = jsonAll;
		c.version = version;
		c.json = json;
		c.items = new ItemMap[documentDescriptor.idList.size()];
		for(int i = 0; i < documentDescriptor.idList.size(); i++){
			ItemMap im = items[i];
			if (im != null && im.size() == 0){
				ItemMap cim = new ItemMap();
				c.items[i] = cim;
				for(String id : im.keySet()){
					ItemMeta m = im.get(id);
					ItemMeta x = new ItemMeta();
					cim.put(m.id, x);
					x.document = m.document;
					x.itemDescriptor = m.itemDescriptor;
					x.json = m.json;
					x.status = m.status == Status.DELETED ? Status.DELETED : Status.NODATA;
					x.id = m.id;
					x.version = m.version;
				}
			}
		}
		return c;
	}
	
	public static class ItemMeta {
		private DocumentMeta document;
		private DocumentDescriptor.ItemDescriptor itemDescriptor;
		private String json;
		private String json2;
		private Status status;
		private String id;
		private long version;
		private Document.Item item;
				
		Status status() { return status; }
		long version() { return version; }
		String id() { return id; }
				
		void delete(){
			status = Status.DELETED;
			json2 = null;
		}
		
		boolean save(){
			switch (status) {
			case DELETED : 
			case NODATA : return false;
			case MODIFIED :
			case UNCHANGED : {
				json2 = itemDescriptor.toJson(item, new StringBuffer()).toString();
				if (json2.equals(json)) {
					json2 = null;
					status = Status.UNCHANGED;
					return false;
				}
				status = Status.MODIFIED;
				return true;
			}
			case CREATED :
			case EMPTY : {
				json2 = itemDescriptor.toJson(item, new StringBuffer()).toString();
				if (itemDescriptor.isEmpty(json2)) {
					json2 = null;
					status = Status.EMPTY;
					return false;
				}
				status = Status.CREATED;
				return true;
			}
			}
			return false;
		}
	}
	
}
